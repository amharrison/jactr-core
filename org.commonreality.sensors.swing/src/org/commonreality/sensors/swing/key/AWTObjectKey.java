package org.commonreality.sensors.swing.key;

/*
 * default logging
 */
import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.impl.DefaultObjectKey;

public class AWTObjectKey extends DefaultObjectKey
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AWTObjectKey.class);

  public AWTObjectKey(Component component, IObjectCreator<AWTObjectKey> creator)
  {
    super(component, creator);
  }


  public Component getComponent()
  {
    return (Component) getObject();
  }
}
