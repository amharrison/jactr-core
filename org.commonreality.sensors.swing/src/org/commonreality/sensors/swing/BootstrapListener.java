package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.base.PerceptManager;

/**
 * the bootstrap listener is used as the entry point to finding components that may or may not
 * need to be processed..
 * @author harrison
 *
 */
public class BootstrapListener implements AWTEventListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BootstrapListener.class);
  
  private final PerceptManager _manager;
  
  public BootstrapListener(PerceptManager manager)
  {
    _manager = manager; 
  }
  
  
  public void install()
  {
    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.CONTAINER_EVENT_MASK);
  }
  
  public void uninstall()
  {
    Toolkit.getDefaultToolkit().removeAWTEventListener(this); 
  }

  public void eventDispatched(AWTEvent event)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Received %s", event));
    if(event instanceof ContainerEvent)
    {
      ContainerEvent ce = (ContainerEvent) event;
      _manager.markAsDirty(ce.getChild());
      _manager.markAsDirty(ce.getComponent());
    }
  }

}
