/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.component;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.core.io.Resource;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.SingletonMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.examples.marketdata.ExampleMarketDataProvider;
import com.opengamma.examples.marketdata.SimulatedMarketDataGenerator;
import com.opengamma.master.security.SecurityMaster;

/**
 * Component factory for market data
 */
@BeanDefinition
public class ExampleMarketDataComponentFactory extends AbstractComponentFactory {

  private static final String SIMULATED_LIVE_SOURCE_NAME = "Simulated live market data";
  
  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;
  /**
   * The market data file to use in the simulation.
   */
  @PropertyDefinition(validate = "notNull")
  private Resource _marketDataFile;
  /**
   * The security master; market data will be simulated for any suitable entries that aren't in the market data file.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _generatedSecurities;
  /**
   * The time series source; market data will be simulated for suitable entries from the security master that have a price series. The price series is generated randomly when the security is
   * generated.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _generatedTimeSeries;

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initLiveMarketDataProviderFactory(repo);
    initNamedMarketDataSpecificationRepository(repo);
  }
  
  protected MarketDataProviderFactory initLiveMarketDataProviderFactory(ComponentRepository repo) {
    ExampleMarketDataProvider mdProvider = new ExampleMarketDataProvider(getSecuritySource());
    MarketDataProviderFactory mdProviderFactory = new SingletonMarketDataProviderFactory(mdProvider);
    
    SimulatedMarketDataGenerator mdGenerator = new SimulatedMarketDataGenerator(mdProvider, getMarketDataFile(), getGeneratedSecurities(), getGeneratedTimeSeries());
    repo.registerLifecycle(mdGenerator);
    
    ComponentInfo info = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, mdProviderFactory);
    return mdProviderFactory;
  }
  
  protected NamedMarketDataSpecificationRepository initNamedMarketDataSpecificationRepository(ComponentRepository repo) {
    InMemoryNamedMarketDataSpecificationRepository specRepository = new InMemoryNamedMarketDataSpecificationRepository();
    specRepository.addSpecification(SIMULATED_LIVE_SOURCE_NAME, new LiveMarketDataSpecification(SIMULATED_LIVE_SOURCE_NAME));
    
    ComponentInfo info = new ComponentInfo(NamedMarketDataSpecificationRepository.class, getClassifier());
    repo.registerComponent(info, specRepository);
    
    return specRepository;
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExampleMarketDataComponentFactory}.
   * @return the meta-bean, not null
   */
  public static ExampleMarketDataComponentFactory.Meta meta() {
    return ExampleMarketDataComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ExampleMarketDataComponentFactory.Meta.INSTANCE);
  }

  @Override
  public ExampleMarketDataComponentFactory.Meta metaBean() {
    return ExampleMarketDataComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -702456965:  // securitySource
        return getSecuritySource();
      case 842625186:  // marketDataFile
        return getMarketDataFile();
      case -1479375155:  // generatedSecurities
        return getGeneratedSecurities();
      case 2021015187:  // generatedTimeSeries
        return getGeneratedTimeSeries();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -702456965:  // securitySource
        setSecuritySource((SecuritySource) newValue);
        return;
      case 842625186:  // marketDataFile
        setMarketDataFile((Resource) newValue);
        return;
      case -1479375155:  // generatedSecurities
        setGeneratedSecurities((SecurityMaster) newValue);
        return;
      case 2021015187:  // generatedTimeSeries
        setGeneratedTimeSeries((HistoricalTimeSeriesSource) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_securitySource, "securitySource");
    JodaBeanUtils.notNull(_marketDataFile, "marketDataFile");
    JodaBeanUtils.notNull(_generatedSecurities, "generatedSecurities");
    JodaBeanUtils.notNull(_generatedTimeSeries, "generatedTimeSeries");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExampleMarketDataComponentFactory other = (ExampleMarketDataComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getMarketDataFile(), other.getMarketDataFile()) &&
          JodaBeanUtils.equal(getGeneratedSecurities(), other.getGeneratedSecurities()) &&
          JodaBeanUtils.equal(getGeneratedTimeSeries(), other.getGeneratedTimeSeries()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataFile());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGeneratedSecurities());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGeneratedTimeSeries());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security source.
   * @return the value of the property, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source.
   * @param securitySource  the new value of the property, not null
   */
  public void setSecuritySource(SecuritySource securitySource) {
    JodaBeanUtils.notNull(securitySource, "securitySource");
    this._securitySource = securitySource;
  }

  /**
   * Gets the the {@code securitySource} property.
   * @return the property, not null
   */
  public final Property<SecuritySource> securitySource() {
    return metaBean().securitySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data file to use in the simulation.
   * @return the value of the property, not null
   */
  public Resource getMarketDataFile() {
    return _marketDataFile;
  }

  /**
   * Sets the market data file to use in the simulation.
   * @param marketDataFile  the new value of the property, not null
   */
  public void setMarketDataFile(Resource marketDataFile) {
    JodaBeanUtils.notNull(marketDataFile, "marketDataFile");
    this._marketDataFile = marketDataFile;
  }

  /**
   * Gets the the {@code marketDataFile} property.
   * @return the property, not null
   */
  public final Property<Resource> marketDataFile() {
    return metaBean().marketDataFile().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security master; market data will be simulated for any suitable entries that aren't in the market data file.
   * @return the value of the property, not null
   */
  public SecurityMaster getGeneratedSecurities() {
    return _generatedSecurities;
  }

  /**
   * Sets the security master; market data will be simulated for any suitable entries that aren't in the market data file.
   * @param generatedSecurities  the new value of the property, not null
   */
  public void setGeneratedSecurities(SecurityMaster generatedSecurities) {
    JodaBeanUtils.notNull(generatedSecurities, "generatedSecurities");
    this._generatedSecurities = generatedSecurities;
  }

  /**
   * Gets the the {@code generatedSecurities} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> generatedSecurities() {
    return metaBean().generatedSecurities().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time series source; market data will be simulated for suitable entries from the security master that have a price series. The price series is generated randomly when the security is
   * generated.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesSource getGeneratedTimeSeries() {
    return _generatedTimeSeries;
  }

  /**
   * Sets the time series source; market data will be simulated for suitable entries from the security master that have a price series. The price series is generated randomly when the security is
   * generated.
   * @param generatedTimeSeries  the new value of the property, not null
   */
  public void setGeneratedTimeSeries(HistoricalTimeSeriesSource generatedTimeSeries) {
    JodaBeanUtils.notNull(generatedTimeSeries, "generatedTimeSeries");
    this._generatedTimeSeries = generatedTimeSeries;
  }

  /**
   * Gets the the {@code generatedTimeSeries} property.
   * generated.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> generatedTimeSeries() {
    return metaBean().generatedTimeSeries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExampleMarketDataComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", ExampleMarketDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", ExampleMarketDataComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code marketDataFile} property.
     */
    private final MetaProperty<Resource> _marketDataFile = DirectMetaProperty.ofReadWrite(
        this, "marketDataFile", ExampleMarketDataComponentFactory.class, Resource.class);
    /**
     * The meta-property for the {@code generatedSecurities} property.
     */
    private final MetaProperty<SecurityMaster> _generatedSecurities = DirectMetaProperty.ofReadWrite(
        this, "generatedSecurities", ExampleMarketDataComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code generatedTimeSeries} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _generatedTimeSeries = DirectMetaProperty.ofReadWrite(
        this, "generatedTimeSeries", ExampleMarketDataComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "securitySource",
        "marketDataFile",
        "generatedSecurities",
        "generatedTimeSeries");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -702456965:  // securitySource
          return _securitySource;
        case 842625186:  // marketDataFile
          return _marketDataFile;
        case -1479375155:  // generatedSecurities
          return _generatedSecurities;
        case 2021015187:  // generatedTimeSeries
          return _generatedTimeSeries;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExampleMarketDataComponentFactory> builder() {
      return new DirectBeanBuilder<ExampleMarketDataComponentFactory>(new ExampleMarketDataComponentFactory());
    }

    @Override
    public Class<? extends ExampleMarketDataComponentFactory> beanType() {
      return ExampleMarketDataComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code marketDataFile} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> marketDataFile() {
      return _marketDataFile;
    }

    /**
     * The meta-property for the {@code generatedSecurities} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> generatedSecurities() {
      return _generatedSecurities;
    }

    /**
     * The meta-property for the {@code generatedTimeSeries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> generatedTimeSeries() {
      return _generatedTimeSeries;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
