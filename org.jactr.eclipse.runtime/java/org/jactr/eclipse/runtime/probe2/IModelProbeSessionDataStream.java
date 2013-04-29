package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

public interface IModelProbeSessionDataStream extends
    ISessionDataStream<ModelProbeData>
{

  public ModelProbeData getRoot();
}
