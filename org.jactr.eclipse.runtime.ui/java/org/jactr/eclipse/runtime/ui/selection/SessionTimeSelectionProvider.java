package org.jactr.eclipse.runtime.ui.selection;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.List;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class SessionTimeSelectionProvider implements ISelectionProvider
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER = LogFactory
                                                     .getLog(SessionTimeSelectionProvider.class);

  private List<ISelectionChangedListener> _listeners;

  private List<ISelectionChangedListener> _cachedListeners;

  private ISelection                      _selection;

  public SessionTimeSelectionProvider()
  {
    _listeners = new ArrayList<ISelectionChangedListener>();
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    synchronized (_listeners)
    {
      _listeners.add(listener);
      _cachedListeners = null;
    }
  }

  public ISelection getSelection()
  {
    return _selection;
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    synchronized (_listeners)
    {
      _listeners.remove(listener);
      _cachedListeners = null;
    }

  }

  public void setSelection(ISelection selection)
  {
    _selection = selection;

    List<ISelectionChangedListener> listeners = null;
    synchronized (_listeners)
    {
      if (_listeners.size() == 0) return;

      if (_cachedListeners == null)
        _cachedListeners = new FastList<ISelectionChangedListener>(_listeners);

      listeners = _cachedListeners;
    }

    SelectionChangedEvent event = new SelectionChangedEvent(this,
        getSelection());
    fireSelectionEvent(listeners, event);
  }

  private void fireSelectionEvent(
      final List<ISelectionChangedListener> listeners,
      final SelectionChangedEvent event)
  {

    // Job fireLater = new Job("selecion propogation") {
    //
    // @Override
    // protected IStatus run(IProgressMonitor monitor)
    // {
    for (ISelectionChangedListener listener : listeners)
      try
      {
        listener.selectionChanged(event);
      }
      catch (Exception e)
      {
        // silently swallow or log?
        LOGGER.error("Failed to propogate selection change event", e);
      }
    // return Status.OK_STATUS;
    // }
    //
    // };
    //
    //
    // fireLater.schedule();
  }

}
