/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexOptionBlackSingleValuedFunction extends ForexOptionBlackFunction {

  public ForexOptionBlackSingleValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .withAny(PROPERTY_PUT_CURVE)
        .withAny(PROPERTY_PUT_FORWARD_CURVE)
        .withAny(PROPERTY_PUT_CURVE_CALCULATION_METHOD)
        .withAny(PROPERTY_CALL_CURVE)
        .withAny(PROPERTY_CALL_FORWARD_CURVE)
        .withAny(PROPERTY_CALL_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String putForwardCurveName, final String putCurveCalculationMethod, final String callCurveName,
      final String callForwardCurveName, final String callCurveCalculationMethod, final String surfaceName, final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, BLACK_METHOD)
        .with(PROPERTY_PUT_CURVE, putCurveName)
        .with(PROPERTY_PUT_FORWARD_CURVE, putForwardCurveName)
        .with(PROPERTY_PUT_CURVE_CALCULATION_METHOD, putCurveCalculationMethod)
        .with(PROPERTY_CALL_CURVE, callCurveName)
        .with(PROPERTY_CALL_FORWARD_CURVE, callForwardCurveName)
        .with(PROPERTY_CALL_CURVE_CALCULATION_METHOD, callCurveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURRENCY, getResultCurrency(target));
  }

  protected static String getResultCurrency(final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    Currency ccy;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      ccy = callCurrency;
    } else {
      ccy = putCurrency;
    }
    return ccy.getCode();
  }
}
