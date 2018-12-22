package org.commonreality.sensors.base.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.IObjectKey;

public class DefaultObjectKey implements IObjectKey
{
  /**
   * Logger definition
   */
  static private final transient Log                 LOGGER             = LogFactory
                                                                            .getLog(DefaultObjectKey.class);

  private ISensoryIdentifier                         _identifier;

  private boolean                                    _objectIsImmutable = false;

  private Object                                     _object;

  private final IObjectCreator<? extends IObjectKey> _creator;

  public DefaultObjectKey(Object object, boolean isImmutable,
      IObjectCreator<? extends IObjectKey> creator)
  {
    if (object == null)
      throw new IllegalArgumentException("object must not be null");
    _creator = creator;
    _object = object;
    _objectIsImmutable = isImmutable;
  }

  public DefaultObjectKey(Object object,
      IObjectCreator<? extends IObjectKey> creator)
  {
    this(object, false, creator);
  }

  public boolean isObjectImmutable()
  {
    return _objectIsImmutable;
  }

  public ISensoryIdentifier getIdentifier()
  {
    return _identifier;
  }

  public Object getObject()
  {
    return _object;
  }

  public void replaceObject(Object newValue)
  {
    if (!_objectIsImmutable)
      throw new IllegalStateException(
          "Replacing of object keys is only allowed for immutable objects");
    _object = newValue;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (_identifier == null ? 0 : _identifier.hashCode());
    result = prime * result + (_object == null ? 0 : _object.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DefaultObjectKey other = (DefaultObjectKey) obj;
    if (_identifier == null)
    {
      if (other._identifier != null) return false;
    }
    else if (!_identifier.equals(other._identifier)) return false;
    if (_object == null)
    {
      if (other._object != null) return false;
    }
    else if (!_object.equals(other._object)) return false;
    return true;
  }

  public void setIdentifier(ISensoryIdentifier identifier)
  {
    if (identifier == null)
      throw new IllegalArgumentException("identifier must not be null");
    _identifier = identifier;
  }

  public IObjectCreator<? extends IObjectKey> getCreator()
  {
    return _creator;
  }

}
