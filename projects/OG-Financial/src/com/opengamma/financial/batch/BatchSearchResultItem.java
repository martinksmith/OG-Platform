/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * The status of a batch.
 */
@BeanDefinition
public class BatchSearchResultItem extends DirectBean {

  /**
   * The batch date, should not be null.
   */
  @PropertyDefinition
  private LocalDate _observationDate;
  /**
   * The batch time, such as LDN_CLOSE, should not be null.
   */
  @PropertyDefinition
  private String _observationTime;
  /**
   * Is the batch running at the moment?
   */
  @PropertyDefinition
  private BatchStatus _status;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BatchSearchResultItem}.
   * @return the meta-bean, not null
   */
  public static BatchSearchResultItem.Meta meta() {
    return BatchSearchResultItem.Meta.INSTANCE;
  }

  @Override
  public BatchSearchResultItem.Meta metaBean() {
    return BatchSearchResultItem.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 950748666:  // observationDate
        return getObservationDate();
      case 951232793:  // observationTime
        return getObservationTime();
      case -892481550:  // status
        return getStatus();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 950748666:  // observationDate
        setObservationDate((LocalDate) newValue);
        return;
      case 951232793:  // observationTime
        setObservationTime((String) newValue);
        return;
      case -892481550:  // status
        setStatus((BatchStatus) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the batch date, should not be null.
   * @return the value of the property
   */
  public LocalDate getObservationDate() {
    return _observationDate;
  }

  /**
   * Sets the batch date, should not be null.
   * @param observationDate  the new value of the property
   */
  public void setObservationDate(LocalDate observationDate) {
    this._observationDate = observationDate;
  }

  /**
   * Gets the the {@code observationDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> observationDate() {
    return metaBean().observationDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the batch time, such as LDN_CLOSE, should not be null.
   * @return the value of the property
   */
  public String getObservationTime() {
    return _observationTime;
  }

  /**
   * Sets the batch time, such as LDN_CLOSE, should not be null.
   * @param observationTime  the new value of the property
   */
  public void setObservationTime(String observationTime) {
    this._observationTime = observationTime;
  }

  /**
   * Gets the the {@code observationTime} property.
   * @return the property, not null
   */
  public final Property<String> observationTime() {
    return metaBean().observationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets is the batch running at the moment?
   * @return the value of the property
   */
  public BatchStatus getStatus() {
    return _status;
  }

  /**
   * Sets is the batch running at the moment?
   * @param status  the new value of the property
   */
  public void setStatus(BatchStatus status) {
    this._status = status;
  }

  /**
   * Gets the the {@code status} property.
   * @return the property, not null
   */
  public final Property<BatchStatus> status() {
    return metaBean().status().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BatchSearchResultItem}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code observationDate} property.
     */
    private final MetaProperty<LocalDate> _observationDate = DirectMetaProperty.ofReadWrite(this, "observationDate", LocalDate.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(this, "observationTime", String.class);
    /**
     * The meta-property for the {@code status} property.
     */
    private final MetaProperty<BatchStatus> _status = DirectMetaProperty.ofReadWrite(this, "status", BatchStatus.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("observationDate", _observationDate);
      temp.put("observationTime", _observationTime);
      temp.put("status", _status);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public BatchSearchResultItem createBean() {
      return new BatchSearchResultItem();
    }

    @Override
    public Class<? extends BatchSearchResultItem> beanType() {
      return BatchSearchResultItem.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code observationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> observationDate() {
      return _observationDate;
    }

    /**
     * The meta-property for the {@code observationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> observationTime() {
      return _observationTime;
    }

    /**
     * The meta-property for the {@code status} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BatchStatus> status() {
      return _status;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
