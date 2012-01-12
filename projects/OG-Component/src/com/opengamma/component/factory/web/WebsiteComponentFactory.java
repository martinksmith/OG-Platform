/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.web;

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

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.JerseyRestResourceFactory;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.web.WebHomeResource;
import com.opengamma.web.batch.WebBatchesResource;
import com.opengamma.web.config.WebConfigsResource;
import com.opengamma.web.exchange.WebExchangesResource;
import com.opengamma.web.holiday.WebHolidaysResource;
import com.opengamma.web.portfolio.WebPortfoliosResource;
import com.opengamma.web.region.WebRegionsResource;
import com.opengamma.web.valuerequirementname.WebValueRequirementNamesResource;

/**
 * Component factory for the main website.
 */
@BeanDefinition
public class WebsiteComponentFactory extends AbstractComponentFactory {

  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigMaster _configMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private ExchangeMaster _exchangeMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidayMaster _holidayMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private RegionMaster _regionMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _positionMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _portfolioMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private BatchMaster _batchMaster;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    initBasics(repo);
    initMasters(repo);
    initAnalytics(repo);
  }

  protected void initMasters(ComponentRepository repo) {
    JerseyRestResourceFactory cfg = new JerseyRestResourceFactory(WebConfigsResource.class, getConfigMaster());
    repo.getRestComponents().publishResource(cfg);
    
    JerseyRestResourceFactory exg = new JerseyRestResourceFactory(WebExchangesResource.class, getExchangeMaster());
    repo.getRestComponents().publishResource(exg);
    
    JerseyRestResourceFactory hol = new JerseyRestResourceFactory(WebHolidaysResource.class, getHolidayMaster());
    repo.getRestComponents().publishResource(hol);
    
    JerseyRestResourceFactory reg = new JerseyRestResourceFactory(WebRegionsResource.class, getRegionMaster());
    repo.getRestComponents().publishResource(reg);
    
//    JerseyRestResourceFactory sec = new JerseyRestResourceFactory(WebSecuritiesResource.class, getSecurityMaster());
//    repo.getRestComponents().publishResource(sec);
//    
//    JerseyRestResourceFactory pos = new JerseyRestResourceFactory(WebPositionsResource.class, getPositionMaster());
//    repo.getRestComponents().publishResource(pos);
    
    JerseyRestResourceFactory prt = new JerseyRestResourceFactory(WebPortfoliosResource.class, getPortfolioMaster(), getPositionMaster());
    repo.getRestComponents().publishResource(prt);
    
    JerseyRestResourceFactory bat = new JerseyRestResourceFactory(WebBatchesResource.class, getBatchMaster());
    repo.getRestComponents().publishResource(bat);
  }

  protected void initBasics(ComponentRepository repo) {
    repo.getRestComponents().publishResource(new WebHomeResource());
  }
    
//  protected void initBundle(ComponentRepository repo) {
//    BundleManagerFactoryBean bundleManagerFactory = new BundleManagerFactoryBean();
//    bundleManagerFactory.set
//    
////    repo.getRestComponents().publishResource(new WebHomeResource());
//  }
//    
//    <!-- ============================================================================== -->
//    <!-- Bundles RESTful service -->
//    <bean id="bundleManager" class="com.opengamma.web.spring.BundleManagerFactoryBean">
//      <property name="configResource" value="classpath:${bundle.file}" />
//      <property name="baseDir" value="${bundle.basedir}" />
//    </bean>
//    
//    <bean id="yuiCompressorOptions" class="com.opengamma.web.bundle.YUICompressorOptions">
//      <property name="lineBreakPosition" value="${yuiCompressorOptions.lineBreakPosition}" />
//      <property name="munge" value="${yuiCompressorOptions.munge}" />
//      <property name="preserveAllSemiColons" value="${yuiCompressorOptions.preserveAllSemiColons}" />
//      <property name="optimize" value="${yuiCompressorOptions.optimize}" />
//      <property name="warn" value="${yuiCompressorOptions.warn}" />
//    </bean>
//    
//    <bean id="bundleCompressor" class="com.opengamma.web.bundle.EHCachingBundleCompressor">
//      <constructor-arg>
//        <bean class="com.opengamma.web.bundle.YUIBundleCompressor">
//          <constructor-arg ref="yuiCompressorOptions" />
//        </bean>
//      </constructor-arg>
//      <constructor-arg ref="cacheManager" />
//    </bean>
//    
//    <bean id="webBundlesRestBean" class="com.opengamma.web.bundle.WebBundlesResource" scope="request">
//      <constructor-arg ref="bundleManager" />
//      <constructor-arg ref="bundleCompressor" />
//      <constructor-arg value="${bundle.mode}"/>
//    </bean>

  protected void initAnalytics(ComponentRepository repo) {
    repo.getRestComponents().publishResource(new WebValueRequirementNamesResource());
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebMastersComponentFactory}.
   * @return the meta-bean, not null
   */
  public static WebsiteComponentFactory.Meta meta() {
    return WebsiteComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(WebsiteComponentFactory.Meta.INSTANCE);
  }

  @Override
  public WebsiteComponentFactory.Meta metaBean() {
    return WebsiteComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 10395716:  // configMaster
        return getConfigMaster();
      case -652001691:  // exchangeMaster
        return getExchangeMaster();
      case 246258906:  // holidayMaster
        return getHolidayMaster();
      case -1820969354:  // regionMaster
        return getRegionMaster();
      case -887218750:  // securityMaster
        return getSecurityMaster();
      case -1840419605:  // positionMaster
        return getPositionMaster();
      case -772274742:  // portfolioMaster
        return getPortfolioMaster();
      case -252634564:  // batchMaster
        return getBatchMaster();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 10395716:  // configMaster
        setConfigMaster((ConfigMaster) newValue);
        return;
      case -652001691:  // exchangeMaster
        setExchangeMaster((ExchangeMaster) newValue);
        return;
      case 246258906:  // holidayMaster
        setHolidayMaster((HolidayMaster) newValue);
        return;
      case -1820969354:  // regionMaster
        setRegionMaster((RegionMaster) newValue);
        return;
      case -887218750:  // securityMaster
        setSecurityMaster((SecurityMaster) newValue);
        return;
      case -1840419605:  // positionMaster
        setPositionMaster((PositionMaster) newValue);
        return;
      case -772274742:  // portfolioMaster
        setPortfolioMaster((PortfolioMaster) newValue);
        return;
      case -252634564:  // batchMaster
        setBatchMaster((BatchMaster) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_configMaster, "configMaster");
    JodaBeanUtils.notNull(_exchangeMaster, "exchangeMaster");
    JodaBeanUtils.notNull(_holidayMaster, "holidayMaster");
    JodaBeanUtils.notNull(_regionMaster, "regionMaster");
    JodaBeanUtils.notNull(_securityMaster, "securityMaster");
    JodaBeanUtils.notNull(_positionMaster, "positionMaster");
    JodaBeanUtils.notNull(_portfolioMaster, "portfolioMaster");
    JodaBeanUtils.notNull(_batchMaster, "batchMaster");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      WebsiteComponentFactory other = (WebsiteComponentFactory) obj;
      return JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getExchangeMaster(), other.getExchangeMaster()) &&
          JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          JodaBeanUtils.equal(getRegionMaster(), other.getRegionMaster()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getBatchMaster(), other.getBatchMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBatchMaster());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the underlying master.
   * @param configMaster  the new value of the property, not null
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    JodaBeanUtils.notNull(configMaster, "configMaster");
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Sets the underlying master.
   * @param exchangeMaster  the new value of the property, not null
   */
  public void setExchangeMaster(ExchangeMaster exchangeMaster) {
    JodaBeanUtils.notNull(exchangeMaster, "exchangeMaster");
    this._exchangeMaster = exchangeMaster;
  }

  /**
   * Gets the the {@code exchangeMaster} property.
   * @return the property, not null
   */
  public final Property<ExchangeMaster> exchangeMaster() {
    return metaBean().exchangeMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the underlying master.
   * @param holidayMaster  the new value of the property, not null
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    JodaBeanUtils.notNull(holidayMaster, "holidayMaster");
    this._holidayMaster = holidayMaster;
  }

  /**
   * Gets the the {@code holidayMaster} property.
   * @return the property, not null
   */
  public final Property<HolidayMaster> holidayMaster() {
    return metaBean().holidayMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Sets the underlying master.
   * @param regionMaster  the new value of the property, not null
   */
  public void setRegionMaster(RegionMaster regionMaster) {
    JodaBeanUtils.notNull(regionMaster, "regionMaster");
    this._regionMaster = regionMaster;
  }

  /**
   * Gets the the {@code regionMaster} property.
   * @return the property, not null
   */
  public final Property<RegionMaster> regionMaster() {
    return metaBean().regionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the underlying master.
   * @param securityMaster  the new value of the property, not null
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    JodaBeanUtils.notNull(securityMaster, "securityMaster");
    this._securityMaster = securityMaster;
  }

  /**
   * Gets the the {@code securityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> securityMaster() {
    return metaBean().securityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the underlying master.
   * @param positionMaster  the new value of the property, not null
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    JodaBeanUtils.notNull(positionMaster, "positionMaster");
    this._positionMaster = positionMaster;
  }

  /**
   * Gets the the {@code positionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> positionMaster() {
    return metaBean().positionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the underlying master.
   * @param portfolioMaster  the new value of the property, not null
   */
  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    JodaBeanUtils.notNull(portfolioMaster, "portfolioMaster");
    this._portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the the {@code portfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> portfolioMaster() {
    return metaBean().portfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public BatchMaster getBatchMaster() {
    return _batchMaster;
  }

  /**
   * Sets the underlying master.
   * @param batchMaster  the new value of the property, not null
   */
  public void setBatchMaster(BatchMaster batchMaster) {
    JodaBeanUtils.notNull(batchMaster, "batchMaster");
    this._batchMaster = batchMaster;
  }

  /**
   * Gets the the {@code batchMaster} property.
   * @return the property, not null
   */
  public final Property<BatchMaster> batchMaster() {
    return metaBean().batchMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebMastersComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", WebsiteComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code exchangeMaster} property.
     */
    private final MetaProperty<ExchangeMaster> _exchangeMaster = DirectMetaProperty.ofReadWrite(
        this, "exchangeMaster", WebsiteComponentFactory.class, ExchangeMaster.class);
    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", WebsiteComponentFactory.class, HolidayMaster.class);
    /**
     * The meta-property for the {@code regionMaster} property.
     */
    private final MetaProperty<RegionMaster> _regionMaster = DirectMetaProperty.ofReadWrite(
        this, "regionMaster", WebsiteComponentFactory.class, RegionMaster.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", WebsiteComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", WebsiteComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", WebsiteComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code batchMaster} property.
     */
    private final MetaProperty<BatchMaster> _batchMaster = DirectMetaProperty.ofReadWrite(
        this, "batchMaster", WebsiteComponentFactory.class, BatchMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "configMaster",
        "exchangeMaster",
        "holidayMaster",
        "regionMaster",
        "securityMaster",
        "positionMaster",
        "portfolioMaster",
        "batchMaster");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          return _configMaster;
        case -652001691:  // exchangeMaster
          return _exchangeMaster;
        case 246258906:  // holidayMaster
          return _holidayMaster;
        case -1820969354:  // regionMaster
          return _regionMaster;
        case -887218750:  // securityMaster
          return _securityMaster;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case -252634564:  // batchMaster
          return _batchMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebsiteComponentFactory> builder() {
      return new DirectBeanBuilder<WebsiteComponentFactory>(new WebsiteComponentFactory());
    }

    @Override
    public Class<? extends WebsiteComponentFactory> beanType() {
      return WebsiteComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code exchangeMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeMaster> exchangeMaster() {
      return _exchangeMaster;
    }

    /**
     * The meta-property for the {@code holidayMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayMaster> holidayMaster() {
      return _holidayMaster;
    }

    /**
     * The meta-property for the {@code regionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionMaster> regionMaster() {
      return _regionMaster;
    }

    /**
     * The meta-property for the {@code securityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> securityMaster() {
      return _securityMaster;
    }

    /**
     * The meta-property for the {@code positionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> positionMaster() {
      return _positionMaster;
    }

    /**
     * The meta-property for the {@code portfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> portfolioMaster() {
      return _portfolioMaster;
    }

    /**
     * The meta-property for the {@code batchMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BatchMaster> batchMaster() {
      return _batchMaster;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}