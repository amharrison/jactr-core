package org.jactr.eclipse.runtime.session.impl;

/*
 * default logging
 */
import java.net.URI;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.ILaunch;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.session.ILocalSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.ISessionController;

public class Session2SessionAdapter extends
    org.jactr.eclipse.runtime.session.impl.AbstractSession implements ISession,
    ILocalSession
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(Session2SessionAdapter.class);

  private final AbstractSession      _oldSession;

  private final ISessionController   _controller;

  public Session2SessionAdapter(AbstractSession session)
  {
    super(session.getId());
    _oldSession = session;
    if (session instanceof ACTRSession)
      _controller = new ACTRControllerWrapper((ACTRSession) session, this);
    else
      _controller = new ControllerWrapper(_oldSession.getLaunch(), this);
  }

  public AbstractSession getOldSession()
  {
    return _oldSession;
  }

  public URI getWorkingDirectory()
  {
    return _oldSession.getAbsoluteWorkingDirectory().toFile().toURI();
  }

  @Override
  public ISessionController getController()
  {
    return _controller;
  }

  public ILaunch getLaunch()
  {
    return _oldSession.getLaunch();
  }

  @Override
  public boolean isOpen()
  {
    return _oldSession.isActive() && super.isOpen();
  }

  @Override
  protected void closeSession()
  {

  }

  @Override
  protected void destroySession()
  {
    _oldSession.destroy();
  }

  public Date getTimeOfExecution()
  {
    return _oldSession.getExecutionStartTime();
  }

}
