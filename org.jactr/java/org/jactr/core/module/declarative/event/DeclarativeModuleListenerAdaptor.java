package org.jactr.core.module.declarative.event;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.event.IParameterEvent;

public class DeclarativeModuleListenerAdaptor implements
    IDeclarativeModuleListener2
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DeclarativeModuleListenerAdaptor.class);

  public void chunkAdded(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunkCreated(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunkTypeAdded(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunkTypeCreated(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunkTypesMerged(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunksMerged(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  @SuppressWarnings("unchecked")
  public void parameterChanged(IParameterEvent pe)
  {
    // TODO Auto-generated method stub

  }

  public void chunkDisposed(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub
    
  }

  public void chunkTypeDisposed(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub
    
  }

  public void chunkTypeRemoved(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

  public void chunkRemoved(DeclarativeModuleEvent dme)
  {
    // TODO Auto-generated method stub

  }

}
