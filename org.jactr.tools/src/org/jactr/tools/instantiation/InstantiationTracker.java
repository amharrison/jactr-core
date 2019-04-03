package org.jactr.tools.instantiation;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.IModel;
import org.jactr.instrument.IInstrument;

@Deprecated
public class InstantiationTracker implements IInstrument
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(InstantiationTracker.class);

  public void initialize()
  {
    // TODO Auto-generated method stub

  }

  public void install(IModel model)
  {
    // TODO Auto-generated method stub

  }

  public void uninstall(IModel model)
  {
    // TODO Auto-generated method stub

  }

}
