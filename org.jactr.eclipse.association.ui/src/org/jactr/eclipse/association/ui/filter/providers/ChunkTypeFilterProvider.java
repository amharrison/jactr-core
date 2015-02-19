package org.jactr.eclipse.association.ui.filter.providers;

/*
 * default logging
 */
import java.util.Collection;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.jactr.eclipse.association.ui.filter.IFilterProvider;
import org.jactr.eclipse.association.ui.filter.impl.ChunkTypeFilter;
import org.jactr.eclipse.association.ui.views.AssociationViewer;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.generic.dialog.ChunkTypeSelectionDialog;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

/**
 * @author harrison
 */
public class ChunkTypeFilterProvider implements IFilterProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ChunkTypeFilterProvider.class);

  public ChunkTypeFilterProvider()
  {

  }

  @Override
  public String getLabel()
  {
    return "ChunkType";
  }

  @Override
  public ViewerFilter[] getFilters()
  {
    CommonTree modelData = getModelData();
    if (modelData != null)
    {
      Collection<CommonTree> chunkTypes = ASTSupport.getAllDescendantsWithType(
          modelData, JACTRBuilder.CHUNK_TYPE);
      ChunkTypeSelectionDialog ctsd = new ChunkTypeSelectionDialog(Display
          .getCurrent().getActiveShell(), "Exclude types",
          "Select chunktypes to exclude", chunkTypes);
      ctsd.setBlockOnOpen(true);

      ViewerFilter rtn = null;

      ctsd.create();
      if (ctsd.open() == Window.OK)
      {
        FastList<String> toFilterOut = FastList.newInstance();
        for (Object selected : ctsd.getCheckedItems())
        {
          CommonTree filterOut = (CommonTree) selected;
          toFilterOut.add(ASTSupport.getName(filterOut));
        }

        rtn = new ChunkTypeFilter(toFilterOut);
      }

      if (rtn != null)
        return new ViewerFilter[] { rtn };
      else
        return new ViewerFilter[0];

    }
    else
    {

    }

    return new ViewerFilter[0];
  }

  /**
   * will try to get model data from the active associative viewer first, then
   * the active editor
   * 
   * @return
   */
  static public CommonTree getModelData()
  {
    CommonTree modelDescriptor = null;
    AssociationViewer viewer = AssociationViewer.getActiveViewer();
    if (viewer != null)
      modelDescriptor = viewer.getInput().getModelDescriptor();

    if (modelDescriptor == null)
    {
      ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
      if (editor != null)
      {
        ICompilationUnit compUnit = editor.getCompilationUnit();
        if (compUnit != null) modelDescriptor = compUnit.getModelDescriptor();
      }
    }

    return modelDescriptor;
  }

}
