package org.jactr.eclipse.runtime.probe3;

/*
 * default logging
 */
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

public interface IModelProbeSessionDataStream extends
    ISessionDataStream<ModelProbeData2>
{

  public ModelProbeData2 getRoot();
}
