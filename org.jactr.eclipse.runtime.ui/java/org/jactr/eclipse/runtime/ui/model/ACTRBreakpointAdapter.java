/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM
 * Corporation - initial API and implementation Bjorn Freeman-Benson - initial
 * API and implementation
 ******************************************************************************/
package org.jactr.eclipse.runtime.ui.model;

import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentUtil;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.runtime.debug.marker.ACTRBreakpoint;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io2.jactr.modelFragment.Production;

import com.google.common.collect.Iterables;

/**
 * Adapter to create breakpoints in jactr files.
 */
public class ACTRBreakpointAdapter implements IToggleBreakpointsTarget
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
      .getLog(ACTRBreakpointAdapter.class);

  public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException
  {
    IResource resource = getResource(part);
    if (isOldIO(resource))
      toggleLineBreakpointsAST(part, selection);
    else
      toggeleLineBreakpointsIO2(part, selection);
  }

  private boolean isOldIO(IResource resource)
  {
    return CompilationUnitManager.isJACTRModel(resource);
  }

  public void toggeleLineBreakpointsIO2(IWorkbenchPart part,
      ISelection selection) throws CoreException
  {
    IResource eclipseResource = getResource(part);
    XtextEditor editor = (XtextEditor) part;
    ITextSelection textSelection = (ITextSelection) selection;
    XtextDocumentUtil util = new XtextDocumentUtil();

    IXtextDocument document = util.getXtextDocument(editor);
    document.readOnly((resource) -> {
      return productionAt(resource, textSelection, eclipseResource);
    });
  }

  protected Object productionAt(XtextResource xtextResource,
      ITextSelection textSelection, IResource eclipseResource)
  {
    EObjectAtOffsetHelper helper = new EObjectAtOffsetHelper();
    EObject object = helper.resolveContainedElementAt(xtextResource,
        textSelection.getOffset());
    Production production = null;

    if (object instanceof Production) production = (Production) object;
    /*
     * find the production container, then create the breakpoint
     */
    if (production == null) production = (Production) Iterables
        .getFirst(Iterables.filter(EcoreUtil2.getAllContainers(object), obj -> {
          return obj instanceof Production;
        }), null);

    if (production == null) return null;

    INode objectNode = NodeModelUtils.findActualNodeFor(production);

    int lineNumber = objectNode.getStartLine();

    String type = ACTRBreakpoint.PRODUCTION;
    String name = production.getName();

    IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
        .getBreakpoints(ACTRSession.ACTR_DEBUG_MODEL);
    try
    {
      for (IBreakpoint breakpoint : breakpoints)
        if (eclipseResource.equals(breakpoint.getMarker().getResource()))
        {
          ACTRBreakpoint bp = (ACTRBreakpoint) breakpoint;
          if (bp.getLineNumber() == lineNumber
              && bp.getBreakpointName().equals(name)
              && bp.getBreakpointType().equals(type))
          {
            // remove
            if (LOGGER.isDebugEnabled()) LOGGER.debug("removing");
            breakpoint.delete();
            return null;
          }
        }

      // create line breakpoint (doc line numbers start at 0)
      ACTRBreakpoint lineBreakpoint = new ACTRBreakpoint(eclipseResource, type,
          name, lineNumber);

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding breakpoint");

      DebugPlugin.getDefault().getBreakpointManager()
          .addBreakpoint(lineBreakpoint);
    }
    catch (CoreException ce)
    {

    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints
   * (org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  public void toggleLineBreakpointsAST(IWorkbenchPart part,
      ISelection selection) throws CoreException
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to toggle breakpoint at " + selection);

    ITextEditor textEditor = getEditor(part);
    if (textEditor != null)
    {
      IResource resource = textEditor.getEditorInput()
          .getAdapter(IResource.class);
      ITextSelection textSelection = (ITextSelection) selection;
      int lineNumber = textSelection.getStartLine();

      ICompilationUnit compUnit = CompilationUnitManager.acquire(resource);

      if (compUnit == null) return;

      CommonTree modelDescriptor = compUnit.getModelDescriptor();
      CompilationUnitManager.release(compUnit);

      // compilation errors - can't set break point
      if (modelDescriptor == null)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug(resource.getName()
            + " has compilation errors, can't mark breakpoints");
        return;
      }

      CommonTree topLevel = findNearestTopLevelElement(modelDescriptor,
          lineNumber);

      // nothing was found.. bummer
      if (topLevel == null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("No top level element was found near " + lineNumber);
        return;
      }

      if (LOGGER.isDebugEnabled()) LOGGER.debug(topLevel.toStringTree()
          + " is the closest element near " + lineNumber);

      lineNumber = getLowestValidLineNumber(topLevel);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(lineNumber + " is the nearest valid line number near "
            + topLevel.toStringTree());

      String type = getTypeString(topLevel);
      String name = topLevel.getFirstChildWithType(JACTRBuilder.NAME).getText();

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("break point type: " + type + " name:" + name);

      IBreakpoint[] breakpoints = DebugPlugin.getDefault()
          .getBreakpointManager().getBreakpoints(ACTRSession.ACTR_DEBUG_MODEL);
      for (IBreakpoint breakpoint : breakpoints)
        if (resource.equals(breakpoint.getMarker().getResource()))
        {
          ACTRBreakpoint bp = (ACTRBreakpoint) breakpoint;
          if (bp.getLineNumber() == lineNumber
              && bp.getBreakpointName().equals(name)
              && bp.getBreakpointType().equals(type))
          {
            // remove
            if (LOGGER.isDebugEnabled()) LOGGER.debug("removing");
            breakpoint.delete();
            return;
          }
        }

      // create line breakpoint (doc line numbers start at 0)
      ACTRBreakpoint lineBreakpoint = new ACTRBreakpoint(resource, type, name,
          lineNumber);

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding breakpoint");

      DebugPlugin.getDefault().getBreakpointManager()
          .addBreakpoint(lineBreakpoint);
    }
  }

  protected CommonTree findNearestTopLevelElement(CommonTree modelDescriptor,
      int referenceLine)
  {
    Collection<CommonTree> topLevels = ASTSupport
        .getAllDescendantsWithType(modelDescriptor, JACTRBuilder.PRODUCTION);
    // topLevels.addAll(ASTSupport.getAllDescendantsWithType(modelDescriptor,
    // JACTRBuilderTreeParser.CHUNK));
    // topLevels.addAll(ASTSupport.getAllDescendantsWithType(modelDescriptor,
    // JACTRBuilderTreeParser.CHUNK_TYPE));
    // topLevels.addAll(ASTSupport.getAllDescendantsWithType(modelDescriptor,
    // JACTRBuilderTreeParser.BUFFER));
    int bestLine = Integer.MAX_VALUE;
    CommonTree bestNode = null;

    for (CommonTree node : topLevels)
    {
      int currentLine = getLowestValidLineNumber(node);
      if (Math.abs(referenceLine - currentLine) < Math
          .abs(bestLine - referenceLine))
      {
        bestNode = node;
        bestLine = currentLine;
      }
    }

    return bestNode;
  }

  protected int getLowestValidLineNumber(CommonTree commonTree)
  {
    int rtn = Integer.MAX_VALUE;

    if (commonTree.getLine() != 0) rtn = commonTree.getLine();

    // zip through the childre
    for (int i = 0; i < commonTree.getChildCount(); i++)
      rtn = Math.min(
          getLowestValidLineNumber((CommonTree) commonTree.getChild(i)), rtn);

    return rtn;
  }

  protected String getTypeString(CommonTree nearest)
  {
    switch (nearest.getType())
    {
      case JACTRBuilder.PRODUCTION:
        return ACTRBreakpoint.PRODUCTION;
      // case JACTRBuilderTreeParser.CHUNK:
      // return org.jactr.eclipse.runtime.debug.marker.CHUNK;
      // case JACTRBuilderTreeParser.CHUNK_TYPE:
      // return org.jactr.eclipse.runtime.debug.marker.CHUNK_TYPE;
      // case JACTRBuilderTreeParser.BUFFER:
      // return org.jactr.eclipse.runtime.debug.marker.BUFFER;
    }
    return "unknown";
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#
   * canToggleLineBreakpoints (org.eclipse.ui.IWorkbenchPart,
   * org.eclipse.jface.viewers.ISelection)
   */
  public boolean canToggleLineBreakpoints(IWorkbenchPart part,
      ISelection selection)
  {
    return getEditor(part) != null;
  }

  /**
   * Returns the editor being used to edit a PDA file, associated with the given
   * part, or <code>null</code> if none.
   * 
   * @param part
   *          workbench part
   * @return the editor being used to edit a PDA file, associated with the given
   *         part, or <code>null</code> if none
   */
  private ITextEditor getEditor(IWorkbenchPart part)
  {
    if (part instanceof ITextEditor)
    {
      ITextEditor editorPart = (ITextEditor) part;
      IResource resource = editorPart.getEditorInput()
          .getAdapter(IResource.class);
      if (resource != null) if (CompilationUnitManager.isJACTRModel(resource)
          || "jactr".equals(resource.getFileExtension()))
        return editorPart;
    }
    return null;
  }

  private IResource getResource(IWorkbenchPart part)
  {
    if (part instanceof XtextEditor) return ((XtextEditor) part).getResource();

    if (part instanceof ITextEditor)
    {
      ITextEditor editorPart = (ITextEditor) part;
      IResource resource = editorPart.getEditorInput()
          .getAdapter(IResource.class);
      return resource;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#
   * toggleMethodBreakpoints (org.eclipse.ui.IWorkbenchPart,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException
  {
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.debug.ui.actions.IToggleBreakpointsTarget#
   * canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
   * org.eclipse.jface.viewers.ISelection)
   */
  public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
      ISelection selection)
  {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints
   * (org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
      throws CoreException
  {
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints
   * (org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection)
  {
    return false;
  }
}
