package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.manager.ISessionManager;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;
import org.jactr.eclipse.ui.concurrent.QueueingUIJob;
import org.jactr.eclipse.ui.concurrent.SWTExecutor;

/**
 * contribution item that provides a combobox from which any session can be
 * selected.
 * 
 * @author harrison
 */
public class SelectSessionAction extends ControlContribution implements
    ISessionSelectionListener
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER       = LogFactory
                                                           .getLog(SelectSessionAction.class);

  static public final String              NO_SELECTION = "No session";

  private final ISessionManagerListener   _sessionManagerListener;

  private final Map<String, ISession>     _labelToSession;

  private final ISessionSelectionProvider _sessionSelectionProvider;

  private CCombo                          _combo;

  private QueueingUIJob                   _updateJob;

  public SelectSessionAction(ISessionSelectionProvider selectionProvider)
  {
    super("selectSession");

    _sessionSelectionProvider = selectionProvider;

    _labelToSession = new TreeMap<String, ISession>();

    _sessionManagerListener = new ISessionManagerListener() {

      public void sessionRemoved(ISession session)
      {
        updateContents();
      }

      public void sessionAdded(ISession session)
      {
        updateContents();
      }
    };

    _updateJob = new QueueingUIJob("session update") {

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor)
      {
        populateCombo();

        return Status.OK_STATUS;
      }
    };

    RuntimePlugin.getDefault().getSessionManager()
        .addListener(_sessionManagerListener, null);

    _sessionSelectionProvider.addListener(this, new SWTExecutor());
  }

  @Override
  public void dispose()
  {
    RuntimePlugin.getDefault().getSessionManager()
        .removeListener(_sessionManagerListener);
    super.dispose();
  }

  protected void updateContents()
  {
    _updateJob.queue(250);
  }

  protected void populateCombo()
  {

    if (_combo.isDisposed()) return;

    ISessionManager manager = RuntimePlugin.getDefault().getSessionManager();
    Set<ISession> allSessions = manager.getSessions(new HashSet<ISession>());
    _combo.removeAll();

    _labelToSession.clear();
    boolean anyOpen = false;

    for (ISession session : allSessions)
      if (session.isOpen())
      {
        anyOpen = true;
        String labelName = generateLabel(session);
        _labelToSession.put(labelName, session);
        _combo.add(labelName);
      }

    // add the no selection
    if (!anyOpen) _combo.add(NO_SELECTION);

    _combo.pack(true);

    _combo.setEnabled(anyOpen);

    // this is a problem..
    if (!_combo.isEnabled()) _combo.select(0);
  }

  @Override
  protected Control createControl(Composite parent)
  {
    _combo = new CCombo(parent, SWT.READ_ONLY | SWT.FLAT);
    _combo.setTextLimit(15);

    _combo.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e)
      {
        //
        String selection = _combo.getItem(_combo.getSelectionIndex());
        ISession session = _labelToSession.get(selection);

        if (session != null) _sessionSelectionProvider.select(session);
      }

      public void widgetDefaultSelected(SelectionEvent e)
      {

      }

    });
    populateCombo();
    return _combo;
  }

  private String generateLabel(ISession session)
  {
    Date startOfSession = session.getTimeOfExecution();

    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(System.currentTimeMillis());

    Calendar then = Calendar.getInstance();
    then.setTime(startOfSession);

    DateFormat instance = null;
    if (now.get(Calendar.DAY_OF_YEAR) > then.get(Calendar.DAY_OF_YEAR))
      instance = DateFormat.getDateTimeInstance(DateFormat.SHORT,
          DateFormat.SHORT);
    else
      instance = DateFormat.getTimeInstance(DateFormat.SHORT);

    return instance.format(startOfSession);
  }

  /**
   * runs in SWT thread
   */
  public void sessionSelected(ISession session)
  {
    if (_combo == null || _combo.isDisposed()) return;

    if (session == null)
      _combo.select(_combo.getItemCount() - 1);
    else
    {
      int index = _combo.indexOf(generateLabel(session));
      if (index == -1)
      {
        populateCombo();
        index = _combo.indexOf(generateLabel(session));
      }
      if (index >= 0) _combo.select(index);
    }

  }
}
