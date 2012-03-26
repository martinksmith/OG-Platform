/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.forex.method.YieldCurveWithCcyBundle;
import com.opengamma.financial.forex.method.YieldCurveWithFXBundle;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Calculator of the present value curve sensitivity for Forex derivatives with all the results converted in the currency of the curve using the relevant exchange rates.
 * The relevant exchange rates should be available in the data (YieldCurveWithFXBundle).
 */
public class PresentValueCurveSensitivityConveretedForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityConveretedForexCalculator INSTANCE = new PresentValueCurveSensitivityConveretedForexCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityConveretedForexCalculator getInstance() {
    return INSTANCE;
  }

  private final PresentValueCurveSensitivityForexCalculator _pvcsc;

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityConveretedForexCalculator() {
    _pvcsc = PresentValueCurveSensitivityForexCalculator.getInstance();
  }

  /**
   * Constructor.
   */
  public PresentValueCurveSensitivityConveretedForexCalculator(final PresentValueCurveSensitivityForexCalculator pvcsc) {
    _pvcsc = pvcsc;
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithCcyBundle, "FX Conversion can be operated only when the curve currency is indicated.");
    YieldCurveWithCcyBundle curvesCcy = (YieldCurveWithCcyBundle) curves;
    MultipleCurrencyInterestRateCurveSensitivity pvcsMulti = _pvcsc.visit(derivative, curvesCcy);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    for (Currency ccy : pvcsMulti.getCurrencies()) {
      InterestRateCurveSensitivity pvcs = pvcsMulti.getSensitivity(ccy);
      for (String curve : pvcs.getCurves()) {
        if (curvesCcy.getCcyMap().get(curve) == ccy) { // Identical currencies: no changes
          result = result.plus(curve, pvcs.getSensitivities().get(curve));
        } else { // Different currencies: exchange rate multiplication.
          ArgumentChecker.isTrue(curves instanceof YieldCurveWithFXBundle, "FX Conversion can be operated only if exchange rates are available.");
          YieldCurveWithFXBundle curveFx = (YieldCurveWithFXBundle) curvesCcy;
          double fxRate = curveFx.getFxRate(curvesCcy.getCcyMap().get(curve), ccy);
          result = result.plus(curve, InterestRateCurveSensitivityUtils.multiplySensitivity(pvcs.getSensitivities().get(curve), fxRate));
        }
      }
    }
    return result;
  }

}
