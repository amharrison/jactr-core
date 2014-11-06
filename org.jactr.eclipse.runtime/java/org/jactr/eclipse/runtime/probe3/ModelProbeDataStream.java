package org.jactr.eclipse.runtime.probe3;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStreamListener;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;

public class ModelProbeDataStream extends
    AbstractSessionDataStream<ModelProbeData2> implements
    IModelProbeSessionDataStream, ILiveSessionDataStream<ModelProbeData2>
{
  /**
   * Logger definition
   */
  static private final transient Log                                                             LOGGER = LogFactory
                                                                                                            .getLog(ModelProbeDataStream.class);

  private ModelProbeData2                                                                        _probeData;

  protected final GeneralEventManager<ILiveSessionDataStreamListener<ModelProbeData2>, Object[]> _eventManager;

  public ModelProbeDataStream(ISessionData sessionData)
  {
    super("probe", sessionData);

    _eventManager = new GeneralEventManager<ILiveSessionDataStreamListener<ModelProbeData2>, Object[]>(
        new GeneralEventManager.INotifier<ILiveSessionDataStreamListener<ModelProbeData2>, Object[]>() {

          public void notify(
              ILiveSessionDataStreamListener<ModelProbeData2> listener,
              Object[] event)
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("notifying"));
            ModelProbeDataStream.this.notify(listener, (Collection) event[0],
                (Collection) event[1], (Collection) event[2]);
          }
        });
  }

  public void setRoot(ModelProbeData2 mpd)
  {
    _probeData = mpd;
  }

  public ModelProbeData2 getRoot()
  {
    return _probeData;
  }

  public long getAmountOfDataAvailable(double startTime, double endTime)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public Collection<ModelProbeData2> getData(double startTime, double endTime,
      Collection<ModelProbeData2> container)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<ModelProbeData2> getLatestData(double endTime,
      Collection<ModelProbeData2> container)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public double getStartTime()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public double getEndTime()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public void clear()
  {

  }

  protected void notify(
      ILiveSessionDataStreamListener<ModelProbeData2> listener,
      Collection<ModelProbeData2> added, Collection<ModelProbeData2> changed,
      Collection<ModelProbeData2> removed)
  {
    listener.dataChanged(this, added, changed, removed);
  }

  public void fireChange(Set<ModelProbeData2> changed)
  {
    _eventManager.notify(new Object[] { Collections.EMPTY_LIST, changed,
        Collections.EMPTY_LIST });
  }

  @Override
  public void addListener(
      ILiveSessionDataStreamListener<ModelProbeData2> listener,
      Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  @Override
  public void removeListener(
      ILiveSessionDataStreamListener<ModelProbeData2> listener)
  {
    _eventManager.removeListener(listener);
  }

}
