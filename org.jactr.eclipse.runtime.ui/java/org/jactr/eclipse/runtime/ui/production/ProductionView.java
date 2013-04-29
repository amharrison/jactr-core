package org.jactr.eclipse.runtime.ui.production;

/*
 * default logging
 */
import java.util.Collection;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.production2.ConflictResolutionData;
import org.jactr.eclipse.runtime.production2.IConflictResolutionDataStream;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.ui.simple.IOrientedComponent;
import org.jactr.eclipse.runtime.ui.simple.SimpleConfigurableASTView;
import org.jactr.eclipse.runtime.ui.simple.SimpleHorizontalASTViewComponent;
import org.jactr.eclipse.runtime.ui.simple.SimpleVerticalASTViewComponent;

public class ProductionView extends SimpleConfigurableASTView
{
  static public final String         ID     = "org.jactr.eclipse.runtime.ui.production.productionView";
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProductionView.class);

  public ProductionView()
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

    IConflictResolutionDataStream bsds = (IConflictResolutionDataStream) sessionData
        .getDataStream("conflict");
    if (bsds == null) return;

    FastList<ConflictResolutionData> conflictData = FastList.newInstance();
    try
    {
      bsds.getData(time, time, conflictData);

      if (conflictData.size() == 0) return;

      container.addAll(conflictData.getFirst().getConflictSet());
    }
    finally
    {
      FastList.recycle(conflictData);
    }
  }

  @Override
  protected boolean isSensitiveToConflictResolution()
  {
    return false;
  }

}
