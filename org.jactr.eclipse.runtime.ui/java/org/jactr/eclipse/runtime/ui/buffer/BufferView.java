package org.jactr.eclipse.runtime.ui.buffer;

/*
 * default logging
 */
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.utils.collections.FastListFactory;
import org.jactr.eclipse.runtime.buffer2.BufferData;
import org.jactr.eclipse.runtime.buffer2.IBufferSessionDataStream;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.ui.simple.IOrientedComponent;
import org.jactr.eclipse.runtime.ui.simple.SimpleConfigurableASTView;
import org.jactr.eclipse.runtime.ui.simple.SimpleHorizontalASTViewComponent;
import org.jactr.eclipse.runtime.ui.simple.SimpleVerticalASTViewComponent;

public class BufferView extends SimpleConfigurableASTView
{
  static public final String         ID     = "org.jactr.eclipse.runtime.ui.buffer.bufferView";

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BufferView.class);

  public BufferView()
  {
    super(true);
  }

  @Override
  protected IOrientedComponent instantiateVertical()
  {
    return new SimpleVerticalASTViewComponent() {

      @Override
      protected void getAST(ISession session, String modelName, double time,
          boolean isPostConflictResolution, Collection<CommonTree> container)
      {
        getASTInternal(session, modelName, time, isPostConflictResolution,
            container);
      }
    };
  }

  @Override
  protected IOrientedComponent instantiateHorizontal()
  {
    return new SimpleHorizontalASTViewComponent() {

      @Override
      protected void getAST(ISession session, String modelName, double time,
          boolean isPostConflictResolution, Collection<CommonTree> container)
      {
        getASTInternal(session, modelName, time, isPostConflictResolution,
            container);

      }
    };
  }

  protected void getASTInternal(ISession session, String modelName,
      double time, boolean isPostConflict, Collection<CommonTree> container)
  {
    ISessionData sessionData = session.getData(modelName);

    if (sessionData == null) return;

    IBufferSessionDataStream bsds = (IBufferSessionDataStream) sessionData
        .getDataStream("buffer");
    if (bsds == null) return;

    List<BufferData> bufferData = FastListFactory.newInstance();
    try
    {
      bsds.getLatestData(time, bufferData);

      if (bufferData.size() == 0) return;

      BufferData bd = bufferData.get(bufferData.size() - 1);
      for (String buffer : bd.getBufferNames())
      {
        CommonTree data = bd.getBufferContents(buffer, isPostConflict);
        if (data != null) container.add(data);
      }
    }
    finally
    {
      FastListFactory.recycle(bufferData);
    }
  }

  @Override
  protected boolean isSensitiveToConflictResolution()
  {
    return true;
  }
}
