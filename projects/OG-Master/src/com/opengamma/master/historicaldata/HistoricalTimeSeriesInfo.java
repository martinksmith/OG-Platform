/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.util.PublicSPI;

/**
 * The information about a historical time-series.
 * <p>
 * Each historical time-series has a set of associated information describing
 * what the series represents, stored in this class.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesInfo extends DirectBean {

  /**
   * The identifier keys.
   * The key of the specific series, such as the equity identifiers.
   */
  @PropertyDefinition
  private IdentifierBundleWithDates _identifiers;
  /**
   * The data source.
   * The source of the data, typically a major financial data supplier.
   */
  @PropertyDefinition
  private String _dataSource;
  /**
   * The data provider.
   * The underlying data provider, such as an individual exchange.
   */
  @PropertyDefinition
  private String _dataProvider;
  /**
   * The data field.
   * This defines the type of data that the series represents.
   */
  @PropertyDefinition
  private String _dataField;
  /**
   * The descriptive observation time key.
   * This defines, textually, the time of day, such as LONDON_CLOSE.
   */
  @PropertyDefinition
  private String _observationTime;

  /**
   * Creates an instance.
   */
  public HistoricalTimeSeriesInfo() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesInfo}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesInfo.Meta meta() {
    return HistoricalTimeSeriesInfo.Meta.INSTANCE;
  }

  @Override
  public HistoricalTimeSeriesInfo.Meta metaBean() {
    return HistoricalTimeSeriesInfo.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 1368189162:  // identifiers
        return getIdentifiers();
      case 1272470629:  // dataSource
        return getDataSource();
      case 339742651:  // dataProvider
        return getDataProvider();
      case -386794640:  // dataField
        return getDataField();
      case 951232793:  // observationTime
        return getObservationTime();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 1368189162:  // identifiers
        setIdentifiers((IdentifierBundleWithDates) newValue);
        return;
      case 1272470629:  // dataSource
        setDataSource((String) newValue);
        return;
      case 339742651:  // dataProvider
        setDataProvider((String) newValue);
        return;
      case -386794640:  // dataField
        setDataField((String) newValue);
        return;
      case 951232793:  // observationTime
        setObservationTime((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesInfo other = (HistoricalTimeSeriesInfo) obj;
      return JodaBeanUtils.equal(getIdentifiers(), other.getIdentifiers()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getObservationTime(), other.getObservationTime());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifiers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifier keys.
   * The key of the specific series, such as the equity identifiers.
   * @return the value of the property
   */
  public IdentifierBundleWithDates getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets the identifier keys.
   * The key of the specific series, such as the equity identifiers.
   * @param identifiers  the new value of the property
   */
  public void setIdentifiers(IdentifierBundleWithDates identifiers) {
    this._identifiers = identifiers;
  }

  /**
   * Gets the the {@code identifiers} property.
   * The key of the specific series, such as the equity identifiers.
   * @return the property, not null
   */
  public final Property<IdentifierBundleWithDates> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data source.
   * The source of the data, typically a major financial data supplier.
   * @return the value of the property
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data source.
   * The source of the data, typically a major financial data supplier.
   * @param dataSource  the new value of the property
   */
  public void setDataSource(String dataSource) {
    this._dataSource = dataSource;
  }

  /**
   * Gets the the {@code dataSource} property.
   * The source of the data, typically a major financial data supplier.
   * @return the property, not null
   */
  public final Property<String> dataSource() {
    return metaBean().dataSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data provider.
   * The underlying data provider, such as an individual exchange.
   * @return the value of the property
   */
  public String getDataProvider() {
    return _dataProvider;
  }

  /**
   * Sets the data provider.
   * The underlying data provider, such as an individual exchange.
   * @param dataProvider  the new value of the property
   */
  public void setDataProvider(String dataProvider) {
    this._dataProvider = dataProvider;
  }

  /**
   * Gets the the {@code dataProvider} property.
   * The underlying data provider, such as an individual exchange.
   * @return the property, not null
   */
  public final Property<String> dataProvider() {
    return metaBean().dataProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data field.
   * This defines the type of data that the series represents.
   * @return the value of the property
   */
  public String getDataField() {
    return _dataField;
  }

  /**
   * Sets the data field.
   * This defines the type of data that the series represents.
   * @param dataField  the new value of the property
   */
  public void setDataField(String dataField) {
    this._dataField = dataField;
  }

  /**
   * Gets the the {@code dataField} property.
   * This defines the type of data that the series represents.
   * @return the property, not null
   */
  public final Property<String> dataField() {
    return metaBean().dataField().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the descriptive observation time key.
   * This defines, textually, the time of day, such as LONDON_CLOSE.
   * @return the value of the property
   */
  public String getObservationTime() {
    return _observationTime;
  }

  /**
   * Sets the descriptive observation time key.
   * This defines, textually, the time of day, such as LONDON_CLOSE.
   * @param observationTime  the new value of the property
   */
  public void setObservationTime(String observationTime) {
    this._observationTime = observationTime;
  }

  /**
   * Gets the the {@code observationTime} property.
   * This defines, textually, the time of day, such as LONDON_CLOSE.
   * @return the property, not null
   */
  public final Property<String> observationTime() {
    return metaBean().observationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesInfo}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code identifiers} property.
     */
    private final MetaProperty<IdentifierBundleWithDates> _identifiers = DirectMetaProperty.ofReadWrite(
        this, "identifiers", HistoricalTimeSeriesInfo.class, IdentifierBundleWithDates.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", HistoricalTimeSeriesInfo.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", HistoricalTimeSeriesInfo.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", HistoricalTimeSeriesInfo.class, String.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(
        this, "observationTime", HistoricalTimeSeriesInfo.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "identifiers",
        "dataSource",
        "dataProvider",
        "dataField",
        "observationTime");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1368189162:  // identifiers
          return _identifiers;
        case 1272470629:  // dataSource
          return _dataSource;
        case 339742651:  // dataProvider
          return _dataProvider;
        case -386794640:  // dataField
          return _dataField;
        case 951232793:  // observationTime
          return _observationTime;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesInfo> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesInfo>(new HistoricalTimeSeriesInfo());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesInfo> beanType() {
      return HistoricalTimeSeriesInfo.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierBundleWithDates> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code dataSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataSource() {
      return _dataSource;
    }

    /**
     * The meta-property for the {@code dataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataProvider() {
      return _dataProvider;
    }

    /**
     * The meta-property for the {@code dataField} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataField() {
      return _dataField;
    }

    /**
     * The meta-property for the {@code observationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> observationTime() {
      return _observationTime;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}