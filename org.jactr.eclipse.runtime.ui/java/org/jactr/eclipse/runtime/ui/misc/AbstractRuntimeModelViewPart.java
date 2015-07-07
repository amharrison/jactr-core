package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.ISessionListener;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.ui.concurrent.SWTExecutor;
import org.jactr.eclipse.ui.generic.view.AbstractModelViewPart;

/**
 * provides session control and a folder for displaying session/model data.
 * 
 * @author harrison
 */
public abstract class AbstractRuntimeModelViewPart extends
    AbstractModelViewPart implements ISessionSelectionProvider
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER           = LogFactory
                                                               .getLog(AbstractRuntimeModelViewPart.class);

  private ISessionManagerListener         _sessionManagerListener;

  private BasicSessionSelectionProvider   _selectionProvider;

  private TerminateAction                 _terminateAction;

  private PlayPauseAction                 _playPauseAction;

  private SelectSessionAction             _sessionSelector;

  private IDebugEventSetListener          _debugListener;

  private final ISessionSelectionListener _selectionListener;

  private ISessionListener                _sessionListener = new ISessionListener() {

                                                             public void sessionClosed(
                                                                 ISession session)
                                                             {

                                                             }

                                                             public void sessionDestroyed(
                                                                 ISession session)
                                                             {

                                                             }

                                                             public void newSessionData(
                                                                 final ISessionData sessionData)
                                                             {
                                                               AbstractRuntimeModelViewPart.this
                                                                   .newSessionData(sessionData);
                                                             }

                                                             public void newSessionDataStream(
                                                                 ISessionData sessionData,
                                                                 ISessionDataStream sessionDataStream)
                                                             {
                                                               AbstractRuntimeModelViewPart.this
                                                                   .newSessionDataStream(
                                                                       sessionData,
                                                                       sessionDataStream);

                                                             }
                                                           };

  private Set<ISessionData>               _allSessionData;

  public AbstractRuntimeModelViewPart()
  {

    _selectionProvider = new BasicSessionSelectionProvider();

    _selectionListener = new ISessionSelectionListener() {

      public void sessionSelected(ISession session)
      {

        try
        {
          /*
           * if this session is the same as the selected tab, do nothing.
           */
          ISession currentlySelected = getSelectedSession();
          if (currentlySelected == session) return;

          /*
           * otherwise, find the first tab with a matching session
           */
          CTabFolder folder = getTabFolder();
          for (CTabItem item : folder.getItems())
          {
            ISessionData sessionData = (ISessionData) item.getData();
            if (sessionData.getSession() == session)
            {
              setSelectedTab(item);
              return;
            }
          }
        }
        finally
        {
          modifyActionAvailability();
          modifyClosability();
        }
      }
    };

    addListener(_selectionListener, new SWTExecutor());

    _allSessionData = new HashSet<ISessionData>();
    _debugListener = new IDebugEventSetListener() {

      public void handleDebugEvents(DebugEvent[] events)
      {
        boolean shouldUpdate = false;
        for (DebugEvent event : events)
        {
          int kind = event.getKind();
          shouldUpdate |= kind == DebugEvent.RESUME
              || kind == DebugEvent.SUSPEND || kind == DebugEvent.TERMINATE;
          if (shouldUpdate) break;
        }

        if (shouldUpdate) Display.getDefault().asyncExec(new Runnable() {

          public void run()
          {
            modifyActionAvailability();
            modifyClosability();
          }

        });
      }

    };

    _sessionManagerListener = new ISessionManagerListener() {

      public void sessionAdded(ISession session)
      {
        addSession(session);
      }

      public void sessionRemoved(ISession session)
      {
        removeSession(session);
      }

    };
  }

  @Override
  public void init(IViewSite site) throws PartInitException
  {
    super.init(site);

    RuntimePlugin.getDefault().getSessionManager()
        .addListener(_sessionManagerListener, null);
  }

  @Override
  protected void partControlCreated()
  {
    addExistingSessionData();
  }

  public void addListener(ISessionSelectionListener listener, Executor executor)
  {
    _selectionProvider.addListener(listener, executor);
  }

  public void removeListener(ISessionSelectionListener listener)
  {
    _selectionProvider.removeListener(listener);
  }

  public void select(ISession session)
  {
    _selectionProvider.select(session);
  }

  public ISession getSelection()
  {
    return _selectionProvider.getSelection();
  }

  protected void addExistingSessionData()
  {
    /*
     * now we should prepopulate with known models
     */
    Set<ISession> sessions = new HashSet<ISession>();
    RuntimePlugin.getDefault().getSessionManager().getSessions(sessions);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("(%s) Addingin %d existing sessions",
          getClass().getName(), sessions.size()));

    for (ISession session : sessions)
      addSession(session);
  }

  protected void addSession(ISession session)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("(%s) session added %s", getClass().getName(),
          session));

    /*
     * it is actually possible for the session not to be fully open yet, as it
     * waits for the runtime to connect..
     */
    // if (session.isOpen())
    // {
      session.addListener(_sessionListener, null);

      for (String model : session.getKeys(new TreeSet<String>()))
      {
        ISessionData data = session.getData(model);
        if (data != null && !_allSessionData.contains(data))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("(%s) Adding %s", getClass().getName(),
                model));

          addModelData(model, data);
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("(%s) No data for %s, ignoring",
              getClass().getName(), model));
      }
    // }
    // else //not open? it's already closed?
    // if (LOGGER.isWarnEnabled())
    // LOGGER.warn(String.format("Session has already closed? %s", session));
  }

  protected void removeSession(ISession session)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Removing session %s", session));

    session.removeListener(_sessionListener);

    for (String model : session.getKeys(new TreeSet<String>()))
    {
      ISessionData data = session.getData(model);
      if (data != null && _allSessionData.contains(data))
        removeModelData(data);
    }

  }

  @Override
  protected void modelDataAdded(Object modelData)
  {
    _allSessionData.add((ISessionData) modelData);

    select(((ISessionData) modelData).getSession());
  }

  @Override
  protected void modelDataRemoved(Object modelData)
  {
    _allSessionData.remove(modelData);
    ISessionData sessionData = (ISessionData) modelData;
    if (sessionData.getSession() == getSelection())
    {
      ISession newSession = getSelectedSession();
      select(newSession);
    }
  }

  @Override
  public String addModelData(String modelName, Object modelData)
  {
    if (_allSessionData.contains(modelData))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("(%s) already has sessionData for %s",
            getClass().getName(), modelData));
      return null;
    }
    return super.addModelData(modelName, modelData);
  }

  /**
   * called when new session data is available for a session we are tracking
   * 
   * @param sessionData
   */
  protected void newSessionData(ISessionData sessionData)
  {

  }

  protected void newSessionDataStream(ISessionData sessionData,
      ISessionDataStream sessionDataStream)
  {

  }

  @Override
  protected void createModelViewsFolder(Composite parent)
  {
    super.createModelViewsFolder(parent);
    configureModelControl();
  }

  protected void configureModelControl()
  {
    // add debug listener
    DebugPlugin.getDefault().addDebugEventListener(_debugListener);

    CTabFolder folder = getTabFolder();

    folder.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e)
      {
        tabSelected(getSelectedTab());
      }

      public void widgetSelected(SelectionEvent e)
      {
        tabSelected(getSelectedTab());
      }

    });

    createActions();
    createToolbar();
  }

  protected void tabSelected(CTabItem item)
  {
    ISession session = getSelectedSession();

    /*
     * send out the notification
     */
    select(session);

  }

  @Override
  public void setFocus()
  {
    // super.setFocus();
    modifyActionAvailability();
    modifyClosability();
  }

  @Override
  public void dispose()
  {
    DebugPlugin.getDefault().removeDebugEventListener(_debugListener);
    RuntimePlugin.getDefault().getSessionManager()
        .removeListener(_sessionManagerListener);
    super.dispose();
  }

  /**
   * called when a tab is closed. Currently this will trigger a session wide
   * close/destroy
   * 
   * @param sessionData
   */
  @Override
  protected void tabClosed(Object modelData)
  {
    ISessionData sessionData = (ISessionData) modelData;

    if (sessionData == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("No session data available?"));
      return;
    }

    ISession session = sessionData.getSession();

    if (session == null) return;

    if (session.isOpen())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Closing session"));
      session.close();
    }

    if (!session.hasBeenDestroyed())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Destroying session", session));
      session.destroy();
    }

    if (session == getSelection()) select(null);
  }

  protected void modifyClosability()
  {
    for (CTabItem item : getItems())
      if (!item.isDisposed())
      {
        ISessionData sessionData = (ISessionData) item.getData();
        if (sessionData == null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("No session data to interogate : %s",
                item.getText()));
          item.setShowClose(true);
        }
        else
          item.setShowClose(!sessionData.getSession().isOpen());
      }
  }

  protected ISessionData getSelectedSessionData()
  {
    CTabItem tab = getSelectedTab();
    if (tab == null || tab.isDisposed()) return null;
    return (ISessionData) tab.getData();
  }

  /**
   * returns the session for the currently selected tab
   * 
   * @return
   */
  protected ISession getSelectedSession()
  {
    ISessionData data = getSelectedSessionData();
    if (data != null) return data.getSession();
    return null;
  }

  protected void modifyActionAvailability()
  {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    _playPauseAction.refresh();
    _terminateAction.refresh();
    mgr.update(true);
  }

  private void createActions()
  {
    _playPauseAction = new PlayPauseAction();
    _terminateAction = new TerminateAction();

    _sessionSelector = new SelectSessionAction(this);

    addListener(_playPauseAction, new SWTExecutor());
    addListener(_terminateAction, new SWTExecutor());
  }

  private void createToolbar()
  {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(_sessionSelector);
    mgr.add(_terminateAction);
    mgr.add(_playPauseAction);
  }

}
