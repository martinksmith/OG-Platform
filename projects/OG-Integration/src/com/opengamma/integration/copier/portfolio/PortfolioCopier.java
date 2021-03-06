/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;

/**
 * Provides the ability to copy portfolios within a master, across masters, between streams/files and masters, and
 * between streams/files.
 */
public abstract interface PortfolioCopier {
 
  void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter);

  void copy(PortfolioReader portfolioReader, PortfolioWriter portfolioWriter, PortfolioCopierVisitor visitor);

}
