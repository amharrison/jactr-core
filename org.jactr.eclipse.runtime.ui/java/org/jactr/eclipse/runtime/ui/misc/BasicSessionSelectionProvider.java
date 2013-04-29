package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager.INotifier;

public class BasicSessionSelectionProvider implements ISessionSelectionProvider
{
  /**
   * Logger definition
   */
  static private final transient Log                                       LOGGER       = LogFactory
                                                                                            .getLog(BasicSessionSelectionProvider.class);

  protected final GeneralEventManager<ISessionSelectionListener, ISession> _eventManager;

  private volatile boolean                                                 _isSelecting = false;

  private ISession                                                         _currentSession;

  public BasicSessionSelectionProvider()
  {
    _eventManager = new GeneralEventManager<ISessionSelectionListener, ISession>(
        new INotifier<ISessionSelectionListener, ISession>() {

          public void notify(ISessionSelectionListener listener, ISession event)
          {
            listener.sessionSelected(event);
          }
        });
  }

  public void addListener(ISessionSelectionListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public void removeListener(ISessionSelectionListener listener)
  {
    _eventManager.removeListener(listener);
  }

  public ISession getSelection()
  {
    return _currentSession;
  }

  public boolean isSelecting()
  {
    return _isSelecting;
  }

  public void select(ISession session)
  {
    if (_isSelecting) return;
    try
    {
      _isSelecting = true;
      _currentSession = session;
      _eventManager.notify(_currentSession);
    }
    finally
    {
      _isSelecting = false;
    }

  }

}
