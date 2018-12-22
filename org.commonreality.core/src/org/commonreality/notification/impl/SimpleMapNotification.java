package org.commonreality.notification.impl;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;

import org.commonreality.identifier.IIdentifier;

public class SimpleMapNotification<K, V> extends AbstractNotification
{
  /**
   * 
   */
  private static final long serialVersionUID = 493883923152564848L;
  
  private final Map<K, V> _data;

  public SimpleMapNotification(IIdentifier notificationId, Map<K, V> data)
  {
    super(notificationId);
    _data = Collections.unmodifiableMap(data);
  }
  public Map<K, V> getData()
  {
    return _data;
  }
}
