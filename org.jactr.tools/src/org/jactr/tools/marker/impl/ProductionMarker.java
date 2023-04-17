package org.jactr.tools.marker.impl;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.IProduction;

public class ProductionMarker extends DefaultMarker
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ProductionMarker.class);

  static public final String         TYPE            = ProductionMarker.class
                                                         .getName();

  static public final String         PRODUCTION_NAME = "productionName";

  public ProductionMarker(String name, IProduction production)
  {
    this(name, TYPE, production);
  }

  public ProductionMarker(String name, String type, IProduction production)
  {
    super(production.getModel(), name, type);
    setProperty(PRODUCTION_NAME, production.getSymbolicProduction().getName());
  }

  public String getProductionName()
  {
    return getProperty(PRODUCTION_NAME);
  }
}
