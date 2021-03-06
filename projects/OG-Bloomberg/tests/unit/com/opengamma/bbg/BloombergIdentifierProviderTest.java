/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;

/**
 * Test BloombergIdentifierProvider.
 */
public class BloombergIdentifierProviderTest {

  private ExternalIdResolver _idProvider = null;
  private CachingReferenceDataProvider _refDataProvider = null;

  @BeforeMethod
  public void setUp(Method m) throws Exception {
    _refDataProvider = BloombergLiveDataServerUtils.getCachingReferenceDataProvider(m);
    BloombergIdentifierProvider provider = new BloombergIdentifierProvider(_refDataProvider);
    _idProvider = provider;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider(_refDataProvider);
    _idProvider = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void equityOption() {
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>();
    ExternalId buid = SecurityUtils.bloombergBuidSecurityId("EO1005552010070180500001");
    ids.add(ExternalIdWithDates.of(buid, null, null));
    ExternalId tickerId = SecurityUtils.bloombergTickerSecurityId("FMCC US 07/17/10 C2.5 Equity");
    ids.add(ExternalIdWithDates.of(tickerId, null, LocalDate.of(2010, MonthOfYear.JULY, 17)));
    ExternalIdBundleWithDates expected = new ExternalIdBundleWithDates(ids);
    
    Collection<ExternalIdBundleWithDates> identifiers = _idProvider.getExternalIds(Collections.singleton(tickerId)).values();
    assertFalse(identifiers.isEmpty());
    assertEquals(expected, identifiers.iterator().next());
  }
  
  @Test
  public void bondFuture() {
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>();
    ids.add(ExternalIdWithDates.of(SecurityUtils.bloombergBuidSecurityId("IX1562358-0"), null, null));
    ids.add(ExternalIdWithDates.of(SecurityUtils.bloombergTickerSecurityId("USH02 Comdty"), LocalDate.of(2000, MonthOfYear.DECEMBER, 20), LocalDate.of(2002, MonthOfYear.MARCH, 19)));
    ids.add(ExternalIdWithDates.of(SecurityUtils.cusipSecurityId("USH02"), LocalDate.of(2000, MonthOfYear.DECEMBER, 20), LocalDate.of(2002, MonthOfYear.MARCH, 19)));
    ExternalIdBundleWithDates expectedIds = new ExternalIdBundleWithDates(ids);
    
    Collection<ExternalIdBundleWithDates> bbgIds = _idProvider.getExternalIds(Collections.singleton(SecurityUtils.bloombergTickerSecurityId("USH02 Comdty"))).values();
    assertFalse(bbgIds.isEmpty());
    assertEquals(expectedIds, bbgIds.iterator().next());
  }

}
