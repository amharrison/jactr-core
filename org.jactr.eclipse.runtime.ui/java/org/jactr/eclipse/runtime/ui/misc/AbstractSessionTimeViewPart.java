package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.ui.log2.ModelLogView2;
import org.jactr.eclipse.runtime.ui.selection.SessionTimeSelection;

/**
 * an abstract view that is linked to the SessionTimeSelection provider,
 * allowing you to display time-linked (to hte log view) data. Because these
 * selection events can come so quickly, they are queued and only the latest one
 * is processed, when the event thread gets to it.
 * 
 * @author harrison
 */
public abstract class AbstractSessionTimeViewPart extends ViewPart implements
    ISelectionListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(AbstractSessionTimeViewPart.class);

  private List<SessionTimeSelection> _selectionEvents = new ArrayList<SessionTimeSelection>();

  private boolean                    _listenToLiveSessions;

  private SessionTimeSelection       _currentSelection;

  protected AbstractSessionTimeViewPart(boolean listensToLiveSessions)
  {
    _listenToLiveSessions = listensToLiveSessions;
  }

  @Override
  public void init(IViewSite site) throws PartInitException
  {
    super.init(site);
    site.getPage().addSelectionListener(ModelLogView2.ID, this);
  }

  @Override
  public void dispose()
  {
    getViewSite().getPage().removeSelectionListener(ModelLogView2.ID, this);
    super.dispose();
  }

  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    if (selection instanceof SessionTimeSelection)
    {
      SessionTimeSelection sts = (SessionTimeSelection) selection;

      if (!_listenToLiveSessions && sts.getSession() != null)
        if (sts.getSession().isOpen()) return;

      queue(sts);
    }
  }

  private void queue(SessionTimeSelection sts)
  {
    boolean alreadyQueued = false;
    synchronized (_selectionEvents)
    {
      alreadyQueued = _selectionEvents.size() > 0;
      _selectionEvents.add(sts);
    }

    if (!alreadyQueued)
    {
      Runnable runner = new Runnable() {

        public void run()
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("running runner %s", this));

          if (_selectionEvents.size() == 0)
          {
            if (LOGGER.isDebugEnabled())
              LOGGER
                  .debug(String.format("runner has nothing to do, returning"));
            return;
          }

          _currentSelection = null;

          synchronized (_selectionEvents)
          {
            _currentSelection = _selectionEvents
                .get(_selectionEvents.size() - 1);
            _selectionEvents.clear();
          }

          if (_currentSelection.isEmpty())
            noData();
          else
            setData(_currentSelection.getSession(),
                _currentSelection.getModelName(), _currentSelection.getTime(),
                showPostConflictResolution());
        }

      };

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Queuing runner %s", runner));
      getViewSite().getShell().getDisplay().asyncExec(runner);
    }
  }

  public void setSelection(SessionTimeSelection selection)
  {
    queue(selection);
  }

  public SessionTimeSelection getCurrentSelection()
  {
    return _currentSelection;
  }

  /**
   * called when the selection changes to something valid
   * 
   * @param session
   * @param time
   */
  abstract protected void setData(ISession session, String modelName,
      double time, boolean isPostConflictResolution);

  /**
   * called when there is nothing selected
   */
  abstract protected void noData();

  abstract protected boolean showPostConflictResolution();

}
