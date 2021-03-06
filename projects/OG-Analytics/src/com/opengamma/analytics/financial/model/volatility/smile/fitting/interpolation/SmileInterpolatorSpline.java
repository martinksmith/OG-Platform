/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NullTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.rootfinding.VectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Fits a set of implied volatilities at gives strikes by interpolating log-moneyness (ln(strike/forward)) against implied volatility using the supplied interpolator (the default
 * is double quadratic). While this will fit any input data, there is no guarantee that the smile is arbitrage free, or indeed always positive, and should therefore be used with
 * care, and only when other smile interpolators fail. The smile is extrapolated in both directions using shifted log-normals set to match the level and slope of the smile at
 * the end point.
 */
public class SmileInterpolatorSpline implements GeneralSmileInterpolator {
  private static final Interpolator1D DEFAULT_INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  private static final VectorRootFinder ROOTFINDER = new BroydenVectorRootFinder();
  private static final NonLinearParameterTransforms TRANSFORMS;

  private final Interpolator1D _interpolator;

  public SmileInterpolatorSpline() {
    _interpolator = DEFAULT_INTERPOLATOR;
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
  }

  static {
    final ParameterLimitsTransform a = new NullTransform();
    final ParameterLimitsTransform b = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    TRANSFORMS = new UncoupledParameterTransforms(new DoubleMatrix1D(0.0, 1.0), new ParameterLimitsTransform[] {a, b }, new BitSet());
  }

  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    final int n = strikes.length;
    ArgumentChecker.isTrue(impliedVols.length == n, "#strikes does not mach #vols");
    final double kL = strikes[0];
    final double kH = strikes[n - 1];
    ArgumentChecker.isTrue(kL <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater that the forward ({})", kL, forward);
    ArgumentChecker.isTrue(kH >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less that the forward ({})", kH, forward);
    final double volL = impliedVols[0];
    final double volH = impliedVols[n - 1];

    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Math.log(strikes[i] / forward);
    }
    final Interpolator1DDataBundle data = _interpolator.getDataBundle(x, impliedVols);

    final Function1D<Double, Double> interpFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        double m = Math.log(k / forward);
        return _interpolator.interpolate(data, m);
      }
    };

    final Function1D<Double, Boolean> domain = new Function1D<Double, Boolean>() {
      @Override
      public Boolean evaluate(Double k) {
        return k >= kL && k <= kH;
      }
    };

    Function1D<Double, Double> dSigmaDx = DIFFERENTIATOR.differentiate(interpFunc, domain);

    double gradL = dSigmaDx.evaluate(kL);
    double gradH = dSigmaDx.evaluate(kH);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> f1 = getDifferenceFunc(forward, kL, expiry, volL, gradL);
    Function1D<DoubleMatrix1D, DoubleMatrix1D> f2 = getDifferenceFunc(forward, kH, expiry, volH, gradH);

    final double[] res1 = TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(f1, TRANSFORMS.transform(new DoubleMatrix1D(0.0, volL)))).getData();
    final double[] res2 = TRANSFORMS.inverseTransform(ROOTFINDER.getRoot(f2, TRANSFORMS.transform(new DoubleMatrix1D(0.0, volH)))).getData();

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double k) {
        if (k < kL) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, res1[0], res1[1]);
        } else if (k > kH) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, res2[0], res2[1]);
        } else {
          return interpFunc.evaluate(k);
        }
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getDifferenceFunc(final double forward, final double strike, final double expiry, final double targetVol, final double targetDvol) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        DoubleMatrix1D y = TRANSFORMS.inverseTransform(x);
        double mu = y.getEntry(0);
        double theta = y.getEntry(1);
        double vol = ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, mu, theta);
        double dvol = ShiftedLogNormalTailExtrapolation.dVdK(forward, strike, expiry, mu, theta); //TODO wasteful as vol is found twice
        return new DoubleMatrix1D(vol - targetVol, forward * (dvol - targetDvol));
      }
    };
  }

}
