package org.commonreality.net.impl;

/*
 * default logging
 */

import org.commonreality.net.session.ISessionInfo;

public abstract class AbstractSessionInfo<T> implements ISessionInfo<T>
{

  private final T _session;
  
  public AbstractSessionInfo(T session)
  {
    _session = session;
  }

 

  @Override
  public T getRawSession()
  {
    return _session;
  }

}
