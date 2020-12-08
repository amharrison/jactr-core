package org.jactr.eclipse.runtime.visual;

import java.util.Collection;
import java.util.Collections;

import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractSessionDataStream;

public class ModelVisiconSessionDataStream
    extends AbstractSessionDataStream<VisualDescriptor>
    implements IModelVisiconSessionDataStream
{

  private VisualDescriptor _visualDescriptor;

  public ModelVisiconSessionDataStream(String streamName,
      ISessionData sessionData)
  {
    super(streamName, sessionData);

  }

  @Override
  public long getAmountOfDataAvailable(double startTime, double endTime)
  {

    return 0;
  }

  @Override
  public Collection<VisualDescriptor> getData(double startTime, double endTime,
      Collection<VisualDescriptor> container)
  {
    return Collections.emptyList();
  }

  @Override
  public Collection<VisualDescriptor> getLatestData(double endTime,
      Collection<VisualDescriptor> container)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getStartTime()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getEndTime()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void clear()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public VisualDescriptor getRoot()
  {
    return _visualDescriptor;
  }

  public void setRoot(VisualDescriptor descriptor)
  {
    _visualDescriptor = descriptor;
  }
}
