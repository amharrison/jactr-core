package org.commonreality.sensors.base;

/*
 * default logging
 */
import org.commonreality.object.identifier.ISensoryIdentifier;

/**
 * basic key used for linking and tracking arbitrary objects and their percepts
 * @author harrison
 *
 */
public interface IObjectKey
{

  public void setIdentifier(ISensoryIdentifier identifier);
  public ISensoryIdentifier getIdentifier();
  
  public IObjectCreator<? extends IObjectKey> getCreator();

  public Object getObject();

  /**
   * used internally if the object represents an updated value, even though its
   * {@link #hashCode()} and {@link #equals(Object)} methods return identical
   * values. This is used when the key object is actually immutable
   * 
   * @param object
   */
  public void replaceObject(Object object);

  public boolean isObjectImmutable();
}
