/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.fra.ZZZForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.fra.ZZZForwardRateAgreement;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests finite difference computation of curve sensitivity.
 */
public class SensitivityFiniteDifferenceTest {
  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Dates : The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 1, 7);
  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ZZZForwardRateAgreementDefinition FRA_DEFINITION = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT,
      NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  // To derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2009, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ZZZForwardRateAgreement FRA = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
  private static final ForwardRateAgreementDiscountingMethod FRA_METHOD = new ForwardRateAgreementDiscountingMethod();

  @Test
  public void curveSensitivityFRA() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final double deltaShift = 1.0E-8;
    final double pv = FRA_METHOD.presentValue(FRA, curves);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final ZZZForwardRateAgreement fraBumpedForward = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final double[] timeForward = new double[2];
    timeForward[0] = FRA.getFixingPeriodStartTime();
    timeForward[1] = FRA.getFixingPeriodEndTime();
    final int nbForwardDate = timeForward.length;
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForward[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedPv = FRA_METHOD.presentValue(fraBumpedForward, curvesBumpedForward);
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
    }

    double[] nodeTimesForwardMethod = new double[] {FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime()};
    double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", sensiPvForwardFD[loopnode], sensiForwardMethod[loopnode]);
    }

    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final ZZZForwardRateAgreement fraBumped = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[2];
    final double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[1], deltaShift);
    final YieldCurveBundle curvesBumped = new YieldCurveBundle();
    curvesBumped.addAll(curves);
    curvesBumped.setCurve("Bumped Curve", bumpedCurve);
    final double bumpedPvDsc = FRA_METHOD.presentValue(fraBumped, curvesBumped);
    double[] resDsc = new double[1];
    resDsc[0] = (bumpedPvDsc - pv) / deltaShift;

    double[] nodeTimesFundingMethod = new double[] {FRA.getPaymentTime()};
    double[] sensiFundingMethod = SensitivityFiniteDifference.curveSensitivity(fraBumped, curves, pv, FUNDING_CURVE_NAME, bumpedCurveName, nodeTimesFundingMethod, deltaShift, FRA_METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiFundingMethod.length);
    for (int loopnode = 0; loopnode < sensiFundingMethod.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: node sensitivity", resDsc[loopnode], sensiFundingMethod[loopnode]);
    }
  }

  @Test
  public void curveSensitivityCentered() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final double deltaShift = 1.0E-8;
    final double pv = FRA_METHOD.presentValue(FRA, curves);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final ZZZForwardRateAgreement fraBumpedForward = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    double[] nodeTimesForwardMethod = new double[] {FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime()};
    double[] sensiForward = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    double[] sensiForwardCentered = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD,
        true);
    double[] sensiForwardCentered2 = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD);
    double[] sensiForwardNonCentered = SensitivityFiniteDifference.curveSensitivity(fraBumpedForward, curves, pv, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForwardMethod, deltaShift, FRA_METHOD,
        false);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForward.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardCentered.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardCentered2.length);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardNonCentered.length);
    for (int loopnode = 0; loopnode < sensiForward.length; loopnode++) {
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForward[loopnode], sensiForwardNonCentered[loopnode], 1.0E-10);
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForwardNonCentered[loopnode], sensiForwardCentered[loopnode], 1.0E-1);
      assertEquals("Sensitivity finite difference method: centered vs non-centered", sensiForwardCentered[loopnode], sensiForwardCentered2[loopnode], 1.0E-10);
    }
  }

}