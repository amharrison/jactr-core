package org.jactr.eclipse.runtime.visual;

import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

public interface IModelVisiconSessionDataStream
    extends ISessionDataStream<VisualDescriptor>
{

  public VisualDescriptor getRoot();

}
