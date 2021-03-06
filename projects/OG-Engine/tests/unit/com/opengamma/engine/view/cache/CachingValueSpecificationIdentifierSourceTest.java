/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@Test
public class CachingValueSpecificationIdentifierSourceTest {
  
  @Test
  public void simpleOperation() {
    final AtomicBoolean shouldFail = new AtomicBoolean(false);
    
    final ValueSpecification[] valueSpec = new ValueSpecification[6];
    final Map<ValueSpecification, Long> realIdentifiers = new HashMap<ValueSpecification, Long> ();
    for (int i = 0; i < valueSpec.length; i++) {
      valueSpec[i] = new ValueSpecification(new ValueRequirement("value" + i, new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("scheme", "fibble"))), "mockFunctionId");
      realIdentifiers.put (valueSpec[i], (long)i);
    }
    
    IdentifierMap underlying = new AbstractIdentifierMap() {
      
      @Override
      public long getIdentifier(ValueSpecification spec) {
        if (shouldFail.get()) {
          AssertJUnit.fail("Should not have called underlying.");
        }
        return realIdentifiers.get (spec);
      }

      @Override
      public ValueSpecification getValueSpecification(long identifier) {
        if (shouldFail.get ()) {
          AssertJUnit.fail ("Should not have called underlying.");
        }
        return valueSpec[(int)identifier];
      }

    };
    
    CachingIdentifierMap cachingSource = new CachingIdentifierMap(underlying);
    
    assertEquals(0L, cachingSource.getIdentifier(valueSpec[0]));
    final Map<ValueSpecification, Long> identifiers1 = new HashMap<ValueSpecification, Long> ();
    identifiers1.put (valueSpec[1], 1L);
    identifiers1.put (valueSpec[2], 2L);
    final Map<ValueSpecification, Long> identifiers2 = new HashMap<ValueSpecification, Long> ();
    identifiers2.put (valueSpec[3], 3L);
    identifiers2.put (valueSpec[4], 4L);
    assertEquals (identifiers1, cachingSource.getIdentifiers (Arrays.asList(valueSpec[1], valueSpec[2])));    
    assertEquals (valueSpec[3], cachingSource.getValueSpecification (3));
    final Map<Long, ValueSpecification> valueSpecs1 = new HashMap<Long, ValueSpecification> ();
    valueSpecs1.put (1L, valueSpec[1]);
    valueSpecs1.put (2L, valueSpec[2]);
    final Map<Long, ValueSpecification> valueSpecs2 = new HashMap<Long, ValueSpecification> ();
    valueSpecs2.put (4L, valueSpec[4]);
    valueSpecs2.put (5L, valueSpec[5]);
    assertEquals (valueSpecs2, cachingSource.getValueSpecifications (Arrays.asList (4L, 5L)));
    
    shouldFail.set(true);
    for (int i = 0; i < valueSpec.length; i++) {
      assertEquals((long)i, cachingSource.getIdentifier(valueSpec[i]));
      assertEquals((long)i, cachingSource.getIdentifier(valueSpec[i]));
      assertEquals (valueSpec[i], cachingSource.getValueSpecification (i));
      assertEquals (valueSpec[i], cachingSource.getValueSpecification (i));
    }
    assertEquals (identifiers1, cachingSource.getIdentifiers (Arrays.asList(valueSpec[1], valueSpec[2])));
    assertEquals (identifiers1, cachingSource.getIdentifiers (Arrays.asList(valueSpec[1], valueSpec[2])));
    assertEquals (valueSpecs1, cachingSource.getValueSpecifications (Arrays.asList (1L, 2L)));
    assertEquals (valueSpecs1, cachingSource.getValueSpecifications (Arrays.asList (1L, 2L)));
    assertEquals (identifiers2, cachingSource.getIdentifiers (Arrays.asList(valueSpec[3], valueSpec[4])));
    assertEquals (identifiers2, cachingSource.getIdentifiers (Arrays.asList(valueSpec[3], valueSpec[4])));
    assertEquals (valueSpecs2, cachingSource.getValueSpecifications (Arrays.asList (4L, 5L)));
    assertEquals (valueSpecs2, cachingSource.getValueSpecifications (Arrays.asList (4L, 5L)));
    
  }

}
