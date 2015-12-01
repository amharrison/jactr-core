package org.jactr.eclipse.runtime.playback.internal;

/*
 * default logging
 */
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jactr.eclipse.core.concurrent.QueueingJob;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.elements.ACTRDebugElement;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.trace.RuntimeTraceManager;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class EventPumper extends QueueingJob
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER              = LogFactory
                                                                  .getLog(EventPumper.class);

  private final List<ArchivalIndex.Index> _queue;

  private ISession                        _session;

  private ArchiveController               _controller;

  private volatile boolean                _isFree             = true;

  private double                          _maximumEventWindow = 60;                          // sec

  public EventPumper(String name, ISession session)
  {
    super(name);
    _session = session;
    _queue = new ArrayList<ArchivalIndex.Index>();
  }

  public void setController(ArchiveController controller)
  {
    _controller = controller;
  }

  public void pump(ArchivalIndex.Index dataFile)
  {
    synchronized (_queue)
    {
      _queue.add(dataFile);
      _isFree = false;
    }

    queue(150);
  }

  public boolean isFree()
  {
    synchronized (_queue)
    {
      return _isFree;
    }
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {

    /*
     * collect the records
     */
    FastList<ArchivalIndex.Index> toLoad = FastList.newInstance();
    synchronized (_queue)
    {
      if (_queue.size() == 0) return Status.OK_STATUS;

      double endOfThisEventWindow = _queue.get(0)._span[0]
          + _maximumEventWindow;

      ListIterator<ArchivalIndex.Index> itr = _queue.listIterator();
      while (itr.hasNext())
      {
        ArchivalIndex.Index index = itr.next();
        if (index._span[0] < endOfThisEventWindow)
        {
          toLoad.add(index);
          itr.remove();
        }
        else
          break;
      }
    }

    int blockSize = RuntimePlugin.getDefault().getPreferenceStore()
        .getInt(RuntimePreferences.PLAYBACK_BLOCKSIZE);

    processIndices(new SubProgressMonitor(monitor, toLoad.size()), toLoad,
        blockSize);

    FastList.recycle(toLoad);

    synchronized (_queue)
    {
      _isFree = _queue.size() == 0;
    }

    if (isFree())
      ACTRDebugElement.fireSuspendEvent(_session, 0);
    else
      schedule();

    return Status.OK_STATUS;
  }

  // private void pumpRecord(IProgressMonitor monitor, ArchivalIndex.Index
  // record,
  // Collection<ITransformedEvent> events) throws IOException
  // {
  // File fp = new File(record._data.getRawLocationURI());
  // FileInputStream fis = null;
  //
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug(String.format("Pumping [%.2f, %.2f] %s", record._span[0],
  // record._span[1], record._data));
  //
  // double lastEventTime = 0;
  // try
  // {
  // fis = new FileInputStream(fp);
  // GZIPInputStream giz = new GZIPInputStream(fis);
  // ObjectInputStream ois = new ObjectInputStream(giz);
  //
  // ois.readDouble();
  // ois.readDouble();
  //
  // // while (ois.available() > 0)
  // boolean done = false;
  // while (!done && !monitor.isCanceled() && _session.isOpen())
  // try
  // {
  // ITransformedEvent ite = (ITransformedEvent) ois.readObject();
  // double eventTime = ite.getSimulationTime();
  //
  // lastEventTime = eventTime;
  // if (record._span[0] <= eventTime && eventTime < record._span[1])
  // events.add(ite);
  // else
  // {
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug(String.format(
  // "%s (%.2f) outside of range to pump (%.2f, %.2f)", ite
  // .getClass().getName(), eventTime, record._span[0],
  // record._span[1]));
  //
  // done = eventTime >= record._span[1];
  // }
  // }
  // catch (EOFException e)
  // {
  // done = true;
  // }
  // catch (IOException e)
  // {
  // if (LOGGER.isWarnEnabled())
  // LOGGER.warn(String.format("IOException @ %.2f (%.2f, %.2f) [%s]",
  // lastEventTime, record._span[0], record._span[1], record._data),
  // e);
  // done = true;
  // }
  // catch (Exception e)
  // {
  // LOGGER.error("failed to read record ", e);
  // }
  //
  // if (events.size() == 0)
  // if (LOGGER.isWarnEnabled())
  // LOGGER.warn(String.format(
  // "Was unable to pump any events for (%.2f, %.2f): %s",
  // record._span[0], record._span[1], record._data));
  // }
  // finally
  // {
  // if (fis != null) fis.close();
  // }
  // }

  protected void processIndices(IProgressMonitor monitor,
      List<ArchivalIndex.Index> indices, int eventBlockSize)
  {
    IResource currentResource = null;
    ObjectInputStream inputStream = null;

    monitor.beginTask(String.format("Loading %d indices", indices.size()),
        indices.size());

    try
    {
      for (ArchivalIndex.Index index : indices)
      {
        if (monitor.isCanceled()) break;

        /*
         * file & stream management so we only open any one file, once
         */
        if (!index._data.equals(currentResource))
        {
          if (currentResource != null) try
          {
            inputStream.close();
          }
          catch (Exception e)
          {
            LOGGER.error("failed to close ", e);
          }
          finally
          {
            inputStream = null;
          }

          /*
           * open
           */
          currentResource = index._data;

          try
          {
            FileInputStream fis = new FileInputStream(new File(
                currentResource.getRawLocationURI()));
            GZIPInputStream giz = new GZIPInputStream(fis);
            inputStream = new ObjectInputStream(giz);

            // range header
            inputStream.readDouble();
            inputStream.readDouble();
          }
          catch (Exception e)
          {
            LOGGER.error("failed to open ", e);
            currentResource = null;
            inputStream = null;
          }
        }
        // end stream management

        if (inputStream != null) try
        {
          pumpRecords(monitor, index, eventBlockSize, inputStream);
          monitor.worked(1);
        }
        catch (Exception e)
        {
          RuntimePlugin.error("Failed to pump all records", e);
        }
      }
    }
    finally
    {
      monitor.done();

      // final clean up
      if (inputStream != null) try
      {
        inputStream.close();
      }
      catch (Exception e)
      {

      }
    }
  }

  protected void pumpRecords(IProgressMonitor monitor,
      ArchivalIndex.Index index, int eventBlockSize,
      ObjectInputStream inputStream) throws IOException
  {
    RuntimeTraceManager rtm = RuntimePlugin.getDefault()
        .getRuntimeTraceManager();

    FastList<ITransformedEvent> events = FastList.newInstance();

    boolean done = false;

    long startTime = System.currentTimeMillis();
    int desiredRate = RuntimePlugin.getDefault().getPreferenceStore()
        .getInt(RuntimePreferences.PLAYBACK_RATE);

    try
    {
      double lastKnownTime = 0;
      while (!done && !monitor.isCanceled() && _session.isOpen())
      {
        /*
         * read in eventBlockSize events
         */
        try
        {
          ITransformedEvent ite = (ITransformedEvent) inputStream.readObject();
          double eventTime = ite.getSimulationTime();
          lastKnownTime = eventTime;

          if (index._span[0] <= eventTime && eventTime < index._span[1])
            events.add(ite);
//            RuntimePlugin.info(String.format("read & added %s %.4f", ite
//                .getClass().getSimpleName(), ite.getSimulationTime()));
          else
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format(
                  "%s (%.2f) outside of range to pump (%.2f, %.2f)", ite
                      .getClass().getName(), eventTime, index._span[0],
                  index._span[1]));

            done = eventTime >= index._span[1];

            // RuntimePlugin.info(String.format(
            // "read & skipped %s %.4f (done:%s)", ite.getClass()
            // .getSimpleName(), ite.getSimulationTime(), done));
          }
        }
        catch (EOFException e)
        {
          done = true;
        }
        catch (ClassNotFoundException e)
        {
          LOGGER.error("failed to read record ", e);
        }

        /*
         * fire them off!
         */
        if (events.size() >= eventBlockSize || done)
          try
          {
            SubProgressMonitor pm = new SubProgressMonitor(monitor,
                events.size());

            // RuntimePlugin.info(String.format("firing %d events @ %.4f",
            // events.size(), lastKnownTime));

            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Firing %d events", events.size()));

            pm.beginTask("Propogating Events", events.size());
            rtm.fireEvents(pm, events, _session);
            pm.done();

            if (events.size() > 0) _controller.setCurrentTime(lastKnownTime);
          }
          catch (Exception e)
          {

            events.clear();

            RuntimePlugin.error(String.format("failed firing %d events @ %.4f",
                events.size(), lastKnownTime), e);
          }
          finally
          {
            try
            {
              /*
               * this is all wrong and must be fixed we should put the padding
               * between events
               */
              long actualDelta = System.currentTimeMillis() - startTime;
              long targetDelta = (long) ((double) events.size()
                  / (double) desiredRate * 1000d);

              long sleepTime = targetDelta - actualDelta;

              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "actual %d/%d | target %d/%d sleepTime %d", events.size(),
                    actualDelta, events.size(), targetDelta, sleepTime));

              events.clear();

              // RuntimePlugin.info(String.format("cleared events"));

              if (sleepTime > 0)
                try
                {
                  if (LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("Sleeping %d ms", sleepTime));

                  // RuntimePlugin.info(String.format("sleeping %d",
                  // sleepTime));
                  Thread.sleep(sleepTime);
                }
                catch (InterruptedException e)
                {

                }

              startTime = System.currentTimeMillis();
            }
            catch (Exception e2)
            {
              LOGGER.error("Oops in sleep! ", e2);
            }
          }
        // else
        // /*
        // * not ready to flush
        // */
        // RuntimePlugin.info(String.format("not ready to flush events (%d) (done:%s) @ %.4f",
        // events.size(), done, lastKnownTime));
      }
    }
    finally
    {
      FastList.recycle(events);
    }
  }
}
