package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractRollingSessionDataStream;

public class ModelProbeSessionDataStream extends
    AbstractRollingSessionDataStream<ModelProbeData, ModelProbeData> implements
    IModelProbeSessionDataStream
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ModelProbeSessionDataStream.class);

  ModelProbeData                     _root;

  public ModelProbeSessionDataStream(ISessionData sessionData, int windowSize)
  {
    super("probe", sessionData, windowSize);
  }

  @Override
  public void clear()
  {
    super.clear();
    if (_root != null) _root.clear();
    _root = null;
  }

  @Override
  public void append(ModelProbeData data)
  {
    super.append(data);
    _root = data;
  }

  public ModelProbeData getRoot()
  {
    return _root;
  }

  @Override
  protected double getTime(ModelProbeData data)
  {
    return data.getEndTime();
  }

  public void fireChange(Set<ModelProbeData> changed)
  {
    _eventManager.notify(new Object[] { Collections.EMPTY_LIST, changed,
        Collections.EMPTY_LIST });
  }

  @Override
  protected Collection<ModelProbeData> toOutputData(ModelProbeData input)
  {
    return Collections.singleton(input);
  }

}
