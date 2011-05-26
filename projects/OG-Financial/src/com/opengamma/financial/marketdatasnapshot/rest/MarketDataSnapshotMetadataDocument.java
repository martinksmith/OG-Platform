/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.util.PublicSPI;

/**
 *   A document used to contain the meta data of  a snapshot.
 *   This is cheaper to pass over the wire, but is sufficient for interacting with lists of snapshots
 *   @see MarketDataSnapshotDocument
 */
@PublicSPI
@BeanDefinition
public class MarketDataSnapshotMetadataDocument extends AbstractDocument {

  /**
   * The snapshot document's unique identifier.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  
  /**
   * The snapshot document's name
   */
  @PropertyDefinition
  private String _name;
  
  public MarketDataSnapshotMetadataDocument() {
  }

  public MarketDataSnapshotMetadataDocument(MarketDataSnapshotDocument inner) {
    
    setCorrectionFromInstant(inner.getCorrectionFromInstant());
    setCorrectionToInstant(inner.getCorrectionToInstant());
    setVersionFromInstant(inner.getVersionFromInstant());
    setVersionToInstant(inner.getVersionToInstant());
    
    setUniqueId(inner.getUniqueId());
    setName(inner.getSnapshot().getName());
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataSnapshotMetadataDocument}.
   * @return the meta-bean, not null
   */
  public static MarketDataSnapshotMetadataDocument.Meta meta() {
    return MarketDataSnapshotMetadataDocument.Meta.INSTANCE;
  }

  @Override
  public MarketDataSnapshotMetadataDocument.Meta metaBean() {
    return MarketDataSnapshotMetadataDocument.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 3373707:  // name
        return getName();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshot document's unique identifier.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the snapshot document's unique identifier.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshot document's name
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the snapshot document's name
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataSnapshotMetadataDocument}.
   */
  public static class Meta extends AbstractDocument.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("uniqueId", _uniqueId);
      temp.put("name", _name);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public MarketDataSnapshotMetadataDocument createBean() {
      return new MarketDataSnapshotMetadataDocument();
    }

    @Override
    public Class<? extends MarketDataSnapshotMetadataDocument> beanType() {
      return MarketDataSnapshotMetadataDocument.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}