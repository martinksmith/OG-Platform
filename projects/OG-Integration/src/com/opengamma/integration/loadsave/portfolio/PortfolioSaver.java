/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.loadsave.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.SingleSheetPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.ZippedPortfolioWriter;

/**
 * Provides portfolio saving functionality
 */
public class PortfolioSaver {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioSaver.class);
  
  public void run(String portfolioName, String fileName, String[] securityTypes, boolean persist, ToolContext toolContext) {
                
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        fileName, 
        securityTypes,
        persist,
        toolContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        portfolioName, 
        toolContext);
    
    // Load in and write the securities, positions and trades
    portfolioReader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master & close
    portfolioWriter.flush();
    portfolioWriter.close();
    
  }
  
  private static PortfolioWriter constructPortfolioWriter(String filename, String[] securityTypes, boolean write, ToolContext toolContext) {
    if (write) {  
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }
      
      s_logger.info("Write option specified, will persist to file '" + filename + "'");
 
      String extension = filename.substring(filename.lastIndexOf('.'));

      if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
        
        return new SingleSheetPortfolioWriter(filename, securityTypes, toolContext);
            
      // Multi-asset ZIP file extension
      } else if (extension.equalsIgnoreCase(".zip")) {
        // Create zipped multi-asset class loader
        return new ZippedPortfolioWriter(filename, toolContext);
      } else {
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
      }

    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to file");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }
  }

  private static PortfolioReader constructPortfolioReader(String portfolioName, ToolContext toolContext) {
    return new MasterPortfolioReader(portfolioName, toolContext);
  }

}