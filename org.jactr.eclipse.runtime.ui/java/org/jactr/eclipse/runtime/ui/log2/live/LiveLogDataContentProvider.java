package org.jactr.eclipse.runtime.ui.log2.live;

/*
 * default logging
 */
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jactr.eclipse.runtime.log2.ILogSessionDataStream;
import org.jactr.eclipse.runtime.log2.LogData;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStreamListener;
import org.jactr.eclipse.ui.concurrent.QueueingUIJob;
import org.jactr.eclipse.ui.concurrent.SWTExecutor;

public class LiveLogDataContentProvider implements IStructuredContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log              LOGGER                 = LogFactory
                                                                             .getLog(LiveLogDataContentProvider.class);

  private final static long                       MINIMUM_RESPONSIVENESS = 300;                                        // ms

  private TableViewer                             _viewer;

  private ILogSessionDataStream                   _logData;

  private ILiveSessionDataStreamListener<LogData> _liveListener;

  private ReentrantLock                           _lock                  = new ReentrantLock();

  private FastList<LogData>                       _pendingRemovals;

  private FastList<LogData>                       _pendingAdds;

  private FastList<LogData>                       _pendingUpdates;

  private Set<String>                             _knownColumns;

  private QueueingUIJob                           _updater;
  
  private List<ColumnListener>					  _columnListeners = new LinkedList<ColumnListener>();

  public LiveLogDataContentProvider(TableViewer viewer)
  {
    _viewer = viewer;
    _knownColumns = new TreeSet<String>();
    _pendingRemovals = FastList.newInstance();
    _pendingAdds = FastList.newInstance();
    _pendingUpdates = FastList.newInstance();

    _liveListener = new ILiveSessionDataStreamListener<LogData>() {

      public void dataChanged(ILiveSessionDataStream stream,
          Collection<LogData> added, Collection<LogData> modified,
          Collection<LogData> removed)
      {
        /*
         * we queue up the remove and adds
         */
        _lock.lock();
        try
        {
          _pendingRemovals.addAll(removed);
          _pendingAdds.addAll(added);
          _pendingUpdates.addAll(modified);
        }
        finally
        {
          _lock.unlock();
        }

        _updater.queue(MINIMUM_RESPONSIVENESS);
      }

    };

    _updater = new QueueingUIJob("Log Populator") {

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor)
      {
        processChanges(monitor);
        return Status.OK_STATUS;
      }
    };

    // hide it from the user
    _updater.setSystem(true);
  }

  public void dispose()
  {
    FastList.recycle(_pendingAdds);
    FastList.recycle(_pendingRemovals);
    FastList.recycle(_pendingUpdates);
  }

  @SuppressWarnings("unchecked")
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if (newInput != null)
    {
      _logData = (ILogSessionDataStream) newInput;
      if (_logData instanceof ILiveSessionDataStream)
        ((ILiveSessionDataStream) _logData).addListener(_liveListener,
            new SWTExecutor());
    }
    else /*
          * remove
          */
    if (_logData instanceof ILiveSessionDataStream)
      ((ILiveSessionDataStream) _logData).removeListener(_liveListener);
  }

  public Object[] getElements(Object inputElement)
  {
    FastList<LogData> data = FastList.newInstance();
    try
    {
      _logData.getData(_logData.getStartTime(), _logData.getEndTime(), data);
      return data.toArray();
    }
    finally
    {
      FastList.recycle(data);
    }
  }

  private void snagData(Collection<LogData> source, Collection<LogData> dest)
  {
    _lock.lock();
    try
    {
      dest.addAll(source);
      source.clear();
    }
    finally
    {
      _lock.unlock();
    }
  }

  protected void processChanges(IProgressMonitor monitor)
  {
    FastList<LogData> pending = FastList.newInstance();

    LogData lastData = null;
    try
    {
      int total = _pendingAdds.size() + _pendingRemovals.size()
          + _pendingUpdates.size();

      monitor.beginTask("Updating log", total);

      snagData(_pendingAdds, pending);
      if (pending.size() > 0)
      {
        monitor.subTask(String.format("Adding %d rows", pending.size()));
        lastData = pending.getLast();
        verifyColumns(lastData);
        _viewer.add(pending.toArray());
        monitor.worked(pending.size());
        pending.clear();
      }

      snagData(_pendingUpdates, pending);
      if (pending.size() > 0)
      {
        monitor.subTask(String.format("Updating %d rows", pending.size()));
        _viewer.update(pending.toArray(), null);
        monitor.worked(pending.size());
        pending.clear();
      }

      snagData(_pendingRemovals, pending);
      if (pending.size() > 0)
      {
        monitor.subTask(String.format("Removing %d rows", pending.size()));
        _viewer.remove(pending.toArray());
        monitor.worked(pending.size());
      }

    }
    finally
    {
      FastList.recycle(pending);
      if (lastData != null)
      {
        Table table = _viewer.getTable();
        int item = table.getItemCount() - 1;
        // disable automatic selection for performance reasons
        // table.select(item);
        // table.showSelection();
        table.showItem(table.getItem(item));
      }

      monitor.done();
    }
  }

  /**
   * make sure we have columns for everyone
   * 
   * @param data
   */
  protected void verifyColumns(LogData data)
  {
    if (_knownColumns.size() == 0)
      for (TableColumn column : _viewer.getTable().getColumns())
        _knownColumns.add(column.getText());

    Set<String> streamsInData = data.getStreamNames();
    for (String stream : streamsInData)
      if (!_knownColumns.contains(stream))
      {
        /*
         * new column
         */
        Table table = _viewer.getTable();
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(stream);
        _knownColumns.add(stream);
        for(ColumnListener listener: _columnListeners) {
        	listener.added(column);
        }
      }

  }
  
  public void addColumnListener(ColumnListener listener)
  {
	  _columnListeners.add(listener);
  }
  
  public void removeColumnListener(ColumnListener listener) {
	  _columnListeners.remove(listener);
  }
}
