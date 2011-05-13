/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.calendar.LocalDate;
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
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the ForwardRateAgreement discounting method.
 */
public class ForwardRateAgreementDiscountingMethodTest {
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
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 10, 9);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ZZZForwardRateAgreement FRA = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, CURVES);
  private static final ForwardRateAgreementDiscountingMethod FRA_METHOD = new ForwardRateAgreementDiscountingMethod();

  @Test
  public void parRate() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    double forward = FRA_METHOD.parRate(FRA, curves);
    double dfForwardCurveStart = curves.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(FRA.getFixingPeriodStartTime());
    double dfForwardCurveEnd = curves.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(FRA.getFixingPeriodEndTime());
    double forwardExpected = (dfForwardCurveStart / dfForwardCurveEnd - 1) / FRA.getFixingYearFraction();
    assertEquals("FRA discounting: par rate", forwardExpected, forward, 1.0E-10);
  }

  @Test
  public void presentValue() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    double forward = FRA_METHOD.parRate(FRA, curves);
    double dfSettle = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(FRA.getPaymentTime());
    double expectedPv = FRA.getNotional() * dfSettle * FRA.getPaymentYearFraction() * (forward - FRA_RATE) / (1 + FRA.getFixingYearFraction() * forward);
    double pv = FRA_METHOD.presentValue(FRA, curves);
    assertEquals("FRA discounting: present value", expectedPv, pv, 1.0E-2);
  }

  @Test
  public void presentValueBuySellParity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    ZZZForwardRateAgreementDefinition fraDefinitionSell = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, -NOTIONAL,
        FIXING_DATE, INDEX, FRA_RATE);
    ZZZForwardRateAgreement fraSell = (ZZZForwardRateAgreement) fraDefinitionSell.toDerivative(REFERENCE_DATE, CURVES);
    double pvBuy = FRA_METHOD.presentValue(FRA, curves);
    double pvSell = FRA_METHOD.presentValue(fraSell, curves);
    assertEquals("FRA discounting: present value - buy/sell parity", pvSell, -pvBuy, 1.0E-2);
  }

  @Test
  public void sensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    // Par rate sensitivity
    PresentValueSensitivity prsFra = FRA_METHOD.parRateCurveSensitivity(FRA, curves);
    PresentValueSensitivity pvsFra = FRA_METHOD.presentValueCurveSensitivity(FRA, curves);
    prsFra.clean();
    double deltaTolerancePrice = 1.0E+2;
    double deltaToleranceRate = 1.0E-7;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-8;
    double forward = FRA_METHOD.parRate(FRA, curves);
    double pv = FRA_METHOD.presentValue(FRA, curves);
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    ZZZForwardRateAgreement fraBumpedForward = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    double[] timeForward = new double[2];
    timeForward[0] = FRA.getFixingPeriodStartTime();
    timeForward[1] = FRA.getFixingPeriodEndTime();
    int nbForwardDate = timeForward.length;
    final double[] yieldsForward = new double[nbForwardDate + 1];
    double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForward[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    List<DoublesPair> sensiForwardForward = prsFra.getSensitivity().get(FORWARD_CURVE_NAME);
    List<DoublesPair> sensiPvForward = pvsFra.getSensitivity().get(FORWARD_CURVE_NAME);
    double[] sensiForwardForwardFD = new double[nbForwardDate];
    double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedForward = FRA_METHOD.parRate(fraBumpedForward, curvesBumpedForward);
      final double bumpedPv = FRA_METHOD.presentValue(fraBumpedForward, curvesBumpedForward);
      sensiForwardForwardFD[i] = (bumpedForward - forward) / deltaShift;
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
      final DoublesPair pairForward = sensiForwardForward.get(i);
      final DoublesPair pairPv = sensiPvForward.get(i);
      assertEquals("Sensitivity forward to forward curve: Node " + i, nodeTimesForward[i + 1], pairForward.getFirst(), 1E-8);
      assertEquals("Sensitivity forward to forward curve: Node " + i, sensiForwardForwardFD[i], pairForward.getSecond(), deltaToleranceRate);
      assertEquals("Sensitivity pv to forward curve: Node " + i, nodeTimesForward[i + 1], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity pv to forward curve: Node " + i, sensiPvForwardFD[i], pairPv.getSecond(), deltaTolerancePrice);
    }
    // 2. Funding curve sensitivity
    String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    ZZZForwardRateAgreement fraBumped = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[2];
    double[] nodeTimesFunding = new double[2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = FRA.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    List<DoublesPair> tempFunding = pvsFra.getSensitivity().get(FUNDING_CURVE_NAME);
    final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[1], deltaShift);
    final YieldCurveBundle curvesBumped = new YieldCurveBundle();
    curvesBumped.addAll(curves);
    curvesBumped.setCurve("Bumped Curve", bumpedCurve);
    final double bumpedPvDsc = FRA_METHOD.presentValue(fraBumped, curvesBumped);
    double resDsc = (bumpedPvDsc - pv) / deltaShift;
    final DoublesPair pair = tempFunding.get(0);
    assertEquals("Sensitivity pv to discounting curve:", nodeTimesFunding[1], pair.getFirst(), 1E-8);
    assertEquals("Sensitivity pv to discounting curve:", resDsc, pair.getSecond(), deltaTolerancePrice);
  }
}