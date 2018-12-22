package org.commonreality.notification.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;

public class SimpleStringNotification extends AbstractNotification
{
  /**
   * 
   */
  private static final long          serialVersionUID = -6096212488946030873L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SimpleStringNotification.class);

  private final String               _message;

  public SimpleStringNotification(IIdentifier notificationId, String message)
  {
    super(notificationId);
    _message = message;
  }

  public String getMessage()
  {
    return _message;
  }
}
