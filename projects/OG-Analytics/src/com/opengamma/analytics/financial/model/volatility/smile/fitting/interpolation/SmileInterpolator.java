/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.ParameterizedFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.ArgumentChecker;

/**
 * Interpolate a smile, i.e. fit every data point (market volatility/price), by fitting a smile model (e.g. SABR) through consecutive sets of 3 strikes, so for
 * N data points (prices) there will be N-2 3-point fits. In the interior where smile fits overlap, a weighting between the two smiles is taken, which varies
 * from giving 100% weight to the left smile at the mid point of that fit, down to 0% at the mid point of the right fit.
 * @param <T> The type of the smile model data
 */
public abstract class SmileInterpolator<T extends SmileModelData> implements GeneralSmileInterpolator {
  /**
   * Random number generator with default seed
   */
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double FIT_ERROR = 1e-4; //1bps
  private static final double LARGE_ERROR = 0.1;
  private static final WeightingFunction DEFAULT_WEIGHTING_FUNCTION = SineWeightingFunction.getInstance();

  /**
   * The logger
   */
  protected static final Logger s_logger = LoggerFactory.getLogger(SmileInterpolator.class);
  private final VolatilityFunctionProvider<T> _model;
  private final WeightingFunction _weightingFunction;

  public SmileInterpolator(VolatilityFunctionProvider<T> model) {
    ArgumentChecker.notNull(model, "model");
    _model = model;
    _weightingFunction = DEFAULT_WEIGHTING_FUNCTION;
  }

  public List<T> getFittedModelParameters(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "implied volatilities");
    final int n = strikes.length;
    ArgumentChecker.isTrue(n > 2, "cannot fit less than three points; have {}", n);
    ArgumentChecker.isTrue(impliedVols.length == n, "#strikes != # vols; have {} and {}", impliedVols.length, n);
    validateStrikes(strikes);

    final List<T> modelParameters = new ArrayList<T>(n);

    double[] errors = new double[n];
    Arrays.fill(errors, FIT_ERROR);

    final SmileModelFitter<T> globalFitter = getFitter(forward, strikes, expiry, impliedVols, errors);
    // DoubleMatrix1D gStart = getGlobalStart(forward, strikes, expiry, impliedVols);
    BitSet gFixed = getGlobalFixedValues();
    LeastSquareResultsWithTransform gBest = null;
    double chiSqr = Double.POSITIVE_INFINITY;

    //TODO set these in sub classes
    int tries = 0;
    int count = 0;
    while (chiSqr > 100.0 * n && count < 5) { //10bps average error
      DoubleMatrix1D gStart = getGlobalStart(forward, strikes, expiry, impliedVols);
      try {
        LeastSquareResultsWithTransform glsRes = globalFitter.solve(gStart, gFixed);
        if (glsRes.getChiSq() < chiSqr) {
          gBest = glsRes;
          chiSqr = gBest.getChiSq();
        }
        count++;
      } catch (MathException e) {
      }
      tries++;
      if (tries > 20) {
        throw new MathException("Cannot fit data");
      }
    }

    if (n == 3) {
      if (gBest.getChiSq() / n > 1.0) {
        s_logger.warn("chi^2 on fit to ", +n + " points is " + gBest.getChiSq());
      }
      modelParameters.add(toSmileModelData(gBest.getModelParameters()));
    } else {
      final BitSet lFixed = getLocalFixedValues();
      DoubleMatrix1D lStart = gBest.getModelParameters();
      for (int i = 0; i < n - 2; i++) {
        double[][] temp = getStrikesVolsAndErrors(i, strikes, impliedVols, errors);
        double[] tStrikes = temp[0];
        double[] tVols = temp[1];
        double[] tErrors = temp[2];
        SmileModelFitter<T> localFitter = getFitter(forward, tStrikes, expiry, tVols, tErrors);
        LeastSquareResultsWithTransform lRes = localFitter.solve(lStart, lFixed);
        LeastSquareResultsWithTransform best = lRes;

        count = 0;
        while (lRes.getChiSq() > 3.0 && count < 10) {
          lStart = getGlobalStart(forward, strikes, expiry, impliedVols);
          lRes = localFitter.solve(lStart, lFixed);
          if (lRes.getChiSq() < best.getChiSq()) {
            best = lRes;
          }
          count++;
        }

        if (best.getChiSq() > 3.0) {
          s_logger.warn("chi^2 on 3-point fit #" + i + " is " + best.getChiSq());
        }
        modelParameters.add(toSmileModelData(best.getModelParameters()));
      }
    }
    return modelParameters;
  }

  protected double[][] getStrikesVolsAndErrors(final int index, final double[] strikes, final double[] impliedVols, final double[] errors) {
    return getStrikesVolsAndErrorsForThreePoints(index, strikes, impliedVols, errors);
  }

  /**
   * Use this for models that can be expressed as having 3 parameters (e.g. SABR with beta fixed). It picks out 3 consecutive strike-volatility pairs for the 3
   * parameter fit (so the chi^2 should be zero if the model is capable of fitting the data)
   * @param index Index of first strike
   * @param strikes Array of all strikes
   * @param impliedVols Array of all vols
   * @param errors Array of all errors
   * @return array containing the 3 strikes, vols and errors
   */
  protected static double[][] getStrikesVolsAndErrorsForThreePoints(final int index, final double[] strikes, final double[] impliedVols, final double[] errors) {
    double[] tStrikes = new double[3];
    double[] tVols = new double[3];
    double[] tErrors = new double[3];
    tStrikes = Arrays.copyOfRange(strikes, index, index + 3);
    tVols = Arrays.copyOfRange(impliedVols, index, index + 3);
    tErrors = Arrays.copyOfRange(errors, index, index + 3);
    double[][] res = new double[][] {tStrikes, tVols, tErrors };
    return res;
  }

  /**
   * Use this for models that cannot be easily expressed as having 3 parameters (e.g. mixed log-normal). It picks out 3 consecutive strikes and gives them a small error
   * (1bps by default), while the rest of the data has a relatively large error (100bps by default). The fit is then made to all data (n > 3) which allows more than 3
   * parameters to be fitted (recall, the start position is set from a true global fit). The  chi^2 should be close to zero if the model is capable of fitting the data.
   * @param index Index of first strike
   * @param strikes Array of all strikes
   * @param impliedVols Array of all vols
   * @param errors Array of all errors
   * @return array containing the 3 strikes, vols and errors
   */
  protected static double[][] getStrikesVolsAndErrorsForAllPoints(final int index, final double[] strikes, final double[] impliedVols, final double[] errors) {
    final int n = errors.length;
    double[] lErrors = new double[n];
    Arrays.fill(lErrors, LARGE_ERROR);
    System.arraycopy(errors, index, lErrors, index, 3); //copy the original errors for the points we really want to fit
    double[][] res = new double[][] {strikes, impliedVols, lErrors };
    return res;
  }

  protected abstract DoubleMatrix1D getGlobalStart(final double forward, final double[] strikes, final double expiry, final double[] impliedVols);

  protected BitSet getGlobalFixedValues() {
    return new BitSet();
  }

  protected BitSet getLocalFixedValues() {
    return new BitSet();
  }

  protected abstract T toSmileModelData(final DoubleMatrix1D modelParameters); //TODO have the same thing in SmileModelFitter - could combine

  protected abstract SmileModelFitter<T> getFitter(final double forward, final double[] strikes, final double expiry, final double[] impliedVols, final double[] errors);

  public VolatilityFunctionProvider<T> getModel() {
    return _model;
  }

  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {

    final List<T> modelParams = getFittedModelParameters(forward, strikes, expiry, impliedVols);
    final int n = strikes.length;

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
        Function1D<T, Double> volFunc = _model.getVolatilityFunction(option, forward);
        final int index = SurfaceArrayUtils.getLowerBoundIndex(strikes, strike);
        if (index == 0) {
          return volFunc.evaluate(modelParams.get(0));
        }
        if (index >= n - 2) {
          return volFunc.evaluate(modelParams.get(n - 3));
        }
        final double w = _weightingFunction.getWeight(strikes, index, strike);
        if (w == 1) {
          return volFunc.evaluate(modelParams.get(index - 1));
        } else if (w == 0) {
          return volFunc.evaluate(modelParams.get(index));
        } else {
          return w * volFunc.evaluate(modelParams.get(index - 1)) + (1 - w) * volFunc.evaluate(modelParams.get(index));
        }
      }
    };
  }

  protected DoubleMatrix1D getPolynomialFit(double forward, double[] strikes, double[] impliedVols) {
    final int n = strikes.length;
    double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Math.log(strikes[i] / forward);
    }

    ParameterizedFunction<Double, DoubleMatrix1D, Double> func = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(Double x1, DoubleMatrix1D parameters) {
        double a = parameters.getEntry(0);
        double b = parameters.getEntry(1);
        double c = parameters.getEntry(2);
        return a + b * x1 + c * x1 * x1;
      }
    };

    //TODO replace this with an explicit polynomial fitter
    NonLinearLeastSquare ls = new NonLinearLeastSquare();
    LeastSquareResults lsRes = ls.solve(new DoubleMatrix1D(x), new DoubleMatrix1D(impliedVols), func, new DoubleMatrix1D(0.1, 0.0, 0.0));
    DoubleMatrix1D fitP = lsRes.getFitParameters();
    return fitP;
  }

  private void validateStrikes(final double[] strikes) {
    final int n = strikes.length;
    for (int i = 1; i < n; i++) {
      ArgumentChecker.isTrue(strikes[i] > strikes[i - 1],
          "strikes must be in ascending order; have {} (element {}) and {} (element {})", strikes[i - 1], i - 1, strikes[i], i);
    }
  }

}
