/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Swap generator for the EUR Annual 30/360 vs Euribor 3M.
 */
public class EUR1YEURIBOR3M extends GeneratorSwap {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EUR1YEURIBOR3M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), IndexIborTestsMaster.getInstance().getIndex("EURIBOR3M", calendar));
  }

}
