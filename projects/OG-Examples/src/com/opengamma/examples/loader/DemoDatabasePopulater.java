/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.examples.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Example code to create a demo portfolio and view
 * <p>
 * It is designed to run against the HSQLDB example database.  
 * It should be possible to run this class with no extra command line parameters.
 */
public class DemoDatabasePopulater {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DemoDatabasePopulater.class);

  /**
   * The context.
   */
  @SuppressWarnings("unused")
  private LoaderContext _loaderContext;

  public void setLoaderContext(LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Sets up and loads the context.
   * <p>
   * This loader requires a Spring configuration file that defines the security,
   * position and portfolio masters, together with an instance of this bean
   * under the name "demoEquityPortfolioLoader".
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset(); 
      configurator.doConfigure("src/com/opengamma/examples/server/logback.xml");
      
      // Set the run mode to EXAMPLE so we use the HSQLDB example database.
      PlatformConfigUtils.configureSystemProperties(RunMode.EXAMPLE);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        SelfContainedEquityPortfolioAndSecurityLoader equityLoader = appContext.getBean("selfContainedEquityPortfolioAndSecurityLoader", SelfContainedEquityPortfolioAndSecurityLoader.class);
        System.out.println("Creating example equity portfolio");
        equityLoader.createExamplePortfolio();
        System.out.println("Finished");
        
        SelfContainedSwapPortfolioLoader swapLoader = appContext.getBean("selfContainedSwapPortfolioLoader", SelfContainedSwapPortfolioLoader.class);
        System.out.println("Creating example swap portfolio");
        swapLoader.createExamplePortfolio();
        System.out.println("Finished");
        
        DemoViewsPopulater populator = appContext.getBean("demoViewsPopulater", DemoViewsPopulater.class);
        System.out.println("Creating demo view definition");
        populator.persistViewDefinitions();
        System.out.println("Finished");
        
        SimulatedHistoricalDataGenerator historicalDataGenerator = appContext.getBean("simulatedHistoricalDataGenerator", SimulatedHistoricalDataGenerator.class);
        System.out.println("Creating simulated historical timeseries");
        historicalDataGenerator.run();
        System.out.println("Finished");
        
        TimeSeriesRatingLoader tsConfigLoader = appContext.getBean("timeSeriesRatingLoader", TimeSeriesRatingLoader.class);
        System.out.println("Creating Timeseries configuration");
        tsConfigLoader.saveHistoricalTimeSeriesRatings();
        
        
      } finally {
        appContext.close();
      }
      System.out.println("Finished");
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

}