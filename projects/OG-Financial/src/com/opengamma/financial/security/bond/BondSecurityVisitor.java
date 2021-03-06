/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

/**
 * Visitor for the {@code BondSecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface BondSecurityVisitor<T> {

  T visitCorporateBondSecurity(CorporateBondSecurity security);

  T visitGovernmentBondSecurity(GovernmentBondSecurity security);

  T visitMunicipalBondSecurity(MunicipalBondSecurity security);

}
