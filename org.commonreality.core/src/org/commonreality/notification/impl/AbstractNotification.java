package org.commonreality.notification.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.notification.INotification;

public class AbstractNotification implements INotification
{
  /**
   * 
   */
  private static final long          serialVersionUID = -7015176430074399251L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractNotification.class);

  private final IIdentifier          _identifier;

  public AbstractNotification(IIdentifier notificationId)
  {
    _identifier = notificationId;
  }

  public IIdentifier getIdentifier()
  {
    return _identifier;
  }

}
