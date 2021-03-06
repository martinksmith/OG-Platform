/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.io.PrintStream;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.PiecewiseSABRFitter;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.PiecewiseSABRSurfaceFitter;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class PiecewiseSABRSurfaceFitterTest {
  DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();

  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new LinearExtrapolator1D(INTERPOLATOR_1D));

  //Instrument used for Vega/Greeks Reports
  private static final double EXAMPLE_EXPIRY = 0.5;
  private static final double EXAMPLE_STRIKE = 1.4;

  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  // private static final double[] FORWARDS;
  //  private static final double SPOT = 1.34;
  //  private static double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE;
  @SuppressWarnings("unused")
  private static final String[] TENORS = new String[] {"1W", "2W", "3W", "1M", "3M", "6M", "9M", "1Y", "5Y", "10Y" };
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
      {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] BUTT = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
      {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  //  private static final double[] ATM;
  // private static final double[][] RR;
  //private static final double[][] BUTT;
  private static final double[][] STRIKES;
  private static final double[][] VOLS;
  private static final int N;

  private static final PiecewiseSABRSurfaceFitter SURFACE_FITTER;
  private static final LocalVolatilityPDEGreekCalculator CAL;

  static {

    N = EXPIRIES.length;
    //  FORWARDS = new double[N];
    // Arrays.fill(FORWARDS, 1.0);
    //    ATM = new double[N];
    //    Arrays.fill(ATM, 0.3);
    // RR = new double[2][N];
    //  BUTT = new double[2][N];

    STRIKES = new double[N][];
    VOLS = new double[N][];
    for (int i = 0; i < N; i++) {
      //  FORWARDS[i] = SPOT * Math.exp(DRIFT * EXPIRIES[i]);
      final SmileDeltaParameter cal = new SmileDeltaParameter(EXPIRIES[i], ATM[i], DELTAS,
          new double[] {RR[0][i], RR[1][i] }, new double[] {BUTT[0][i], BUTT[1][i] });
      STRIKES[i] = cal.getStrike(FORWARDS[i]);
      VOLS[i] = cal.getVolatility();
    }

    FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, EXTRAPOLATOR_1D));
    SURFACE_FITTER = new PiecewiseSABRSurfaceFitter(DELTAS, FORWARDS, EXPIRIES, ATM, RR, BUTT);
    CAL = new LocalVolatilityPDEGreekCalculator(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, true);
  }

  //For each expiry, print the expiry and the strikes and implied volatilities
  @Test
      (enabled = false)
      public void printMarketData() {

    for (int i = 0; i < N; i++) {
      System.out.println(EXPIRIES[i]);
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(STRIKES[i][j] + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      final int m = STRIKES[i].length;
      for (int j = 0; j < m; j++) {
        System.out.print(VOLS[i][j] + "\t");
      }
      System.out.print("\n");
    }
  }

  //Fit the market data at each time slice and print the smiles and a functions of both strike and delta
  @Test
      (enabled = false)
      public void fitMarketData() {
    final PiecewiseSABRFitter fitter = new PiecewiseSABRFitter();
    @SuppressWarnings("unchecked")
    final Function1D<Double, Double>[] smiles = new Function1D[N];
    //  final PiecewiseSABRFitter[] fitters = new PiecewiseSABRFitter[N];
    System.out.println("Fitted smiles by strike");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      final double k = 0.5 + j * 4.5 / 99.;
      System.out.print(k + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      smiles[i] = fitter.getVolatilityFunction(FORWARDS[i], STRIKES[i], EXPIRIES[i], VOLS[i]);
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        final double k = 0.5 + j * 4.5 / 99.;
        final double vol = smiles[i].evaluate(k);
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }
    System.out.print("\n");
    System.out.println("Fitted smiles by delta");
    System.out.print("\t");
    for (int j = 0; j < 100; j++) {
      final double d = 0.001 + j * 0.998 / 99.;
      System.out.print(d + "\t");
    }
    System.out.print("\n");
    for (int i = 0; i < N; i++) {
      System.out.print(EXPIRIES[i] + "\t");
      for (int j = 0; j < 100; j++) {
        final double d = 0.05 + j * 0.9 / 99.;
        final Function1D<Double, Double> func = getStrikeForDeltaFunction(FORWARDS[i], EXPIRIES[i], true, smiles[i]);
        final double vol = smiles[i].evaluate(func.evaluate(d));
        System.out.print(vol + "\t");
      }
      System.out.print("\n");
    }

  }

  /**
   * Print the fitted implied vol surface and the derived implied vol
   */
  @Test
      (enabled = false)
      public void printSurface() {
    final BlackVolatilitySurfaceMoneyness surface = SURFACE_FITTER.getImpliedVolatilityMoneynessSurface(true, true, true);
    PDEUtilityTools.printSurface("vol surface", surface.getSurface(), 0, 10, 0.2, 3.0, 200, 100);
    final LocalVolatilitySurfaceMoneyness lv = DUPIRE.getLocalVolatility(surface);
    PDEUtilityTools.printSurface("LV surface", lv.getSurface(), 0, 10, 0.2, 3.0, 200, 100);

  }

  //  @Test
  //  public void testRinging() {
  //    BlackVolatilitySurface surface = SURFACE_FITTER.getImpliedVolatilitySurface(true, false, LAMBDA);
  //    LocalVolatilitySurface lv = DUPIRE.getLocalVolatility(surface, FORWARD_CURVE);
  //    double vol = lv.getVolatility(EXPIRIES[8], 1.5);
  //    System.out.println(vol);
  //    //    double atm = FORWARDS[8];
  //    //    for (int i = 0; i < 21; i++) {
  //    //      double k = atm * (0.95 + 0.1 * i / 20.);
  //    //      double vol = lv.getVolatility(EXPIRIES[8], k);
  //    //      System.out.println(k + "\t" + vol);
  //    //    }
  //  }

  /**
   * Print the fitted implied vol surface and the derived implied vol as a function of moneyness m = log(k/f)/(1+lambda*sqrt(t))
   */
  @Test
      (enabled = false)
      public void printDeltaProxySurface() {
    final double xMin = -0.5;
    final double xMax = 0.5;
    final BlackVolatilitySurfaceMoneyness surface = SURFACE_FITTER.getImpliedVolatilityMoneynessSurface(true, true, true);
    final Surface<Double, Double, Double> moneyNessSurface = toDeltaProxySurface(surface);
    PDEUtilityTools.printSurface("moneyness surface", moneyNessSurface, 0, 10, xMin, xMax, 200, 100);

    final LocalVolatilitySurfaceMoneyness lv = DUPIRE.getLocalVolatility(surface);

    final Surface<Double, Double, Double> lvMoneyNessSurface = toDeltaProxySurface(lv);
    PDEUtilityTools.printSurface("LV moneyness surface", lvMoneyNessSurface, 0.0, 10, xMin, xMax, 200, 100);
  }

  @Test
      (enabled = false)
      public void runPDESolver() {
    final PrintStream ps = System.out;
    CAL.runPDESolver(ps);
  }

  @Test
      (enabled = false)
      public void runBackwardsPDESolver() {
    final PrintStream ps = System.out;
    CAL.runBackwardsPDESolver(ps, EXAMPLE_EXPIRY, EXAMPLE_STRIKE);
  }

  @Test
      (enabled = false)
      public void bucketedVega() {
    final PrintStream ps = System.out;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true);
    CAL.bucketedVegaForwardPDE(ps, option);
    //   CAL.bucketedVegaBackwardsPDE(ps, option);
  }

  @Test
      (enabled = false)
      public void deltaAndGamma() {
    final PrintStream ps = System.out;
    CAL.deltaAndGamma(ps, EXAMPLE_EXPIRY, EXAMPLE_STRIKE);
  }

  @Test
      (enabled = false)
      public void smileDynamic() {
    final PrintStream ps = System.out;
    CAL.smileDynamic(ps, EXAMPLE_EXPIRY, 0.1);
  }

  /**
   * print out vega based greeks
   */
  @Test
      (enabled = false)
      public void vega() {
    final PrintStream ps = System.out;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(EXAMPLE_STRIKE, EXAMPLE_EXPIRY, true);
    CAL.vega(ps, option);
  }

  private Function1D<Double, Double> getStrikeForDeltaFunction(final double forward, final double expiry, final boolean isCall,
      final Function1D<Double, Double> volFunc) {
    final BracketRoot bracketer = new BracketRoot();
    final BisectionSingleRootFinder rootFinder = new BisectionSingleRootFinder(1e-8);

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double delta) {

        final Function1D<Double, Double> deltaFunc = new Function1D<Double, Double>() {

          @Override
          public Double evaluate(final Double strike) {
            final double vol = volFunc.evaluate(strike);
            final double deltaTry = BlackFormulaRepository.delta(forward, strike, expiry, vol, isCall);
            return deltaTry - delta;
          }
        };

        final double[] brackets = bracketer.getBracketedPoints(deltaFunc, 0.5, 1.5, 0, 5);
        return rootFinder.getRoot(deltaFunc, brackets[0], brackets[1]);
      }
    };
  }

  private Surface<Double, Double, Double> toDeltaProxySurface(final LocalVolatilitySurface<?> lv) {

    final Function<Double, Double> func = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = f * Math.exp(-x * Math.sqrt(t));
        return lv.getVolatility(t, k);
      }
    };

    return FunctionalDoublesSurface.from(func);
  }

  private Surface<Double, Double, Double> toDeltaProxySurface(final BlackVolatilitySurface<?> lv) {

    final Function<Double, Double> func = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... tx) {
        final double t = tx[0];
        final double x = tx[1];
        final double f = FORWARD_CURVE.getForward(t);
        final double k = f * Math.exp(-x * Math.sqrt(t));
        return lv.getVolatility(t, k);
      }
    };

    return FunctionalDoublesSurface.from(func);
  }
}
