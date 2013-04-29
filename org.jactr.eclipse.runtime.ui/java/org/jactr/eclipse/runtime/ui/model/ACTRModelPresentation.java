/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM
 * Corporation - initial API and implementation Bjorn Freeman-Benson - initial
 * API and implementation
 ******************************************************************************/
package org.jactr.eclipse.runtime.ui.model;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.jactr.eclipse.runtime.debug.elements.ASTVariable;
import org.jactr.eclipse.runtime.debug.marker.ACTRBreakpoint;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;

/**
 * Renders PDA debug elements
 */
public class ACTRModelPresentation extends LabelProvider implements
    IDebugModelPresentation
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER         = LogFactory
                                                        .getLog(ACTRModelPresentation.class);

  private final ACTRLabelProvider    _labelProvider = new ACTRLabelProvider();

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String,
   *      java.lang.Object)
   */
  public void setAttribute(String attribute, Object value)
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage(Object element)
  {
    if (element instanceof ASTVariable)
    {
      CommonTree node = ((ASTVariable) element).getCommonTree();
      return _labelProvider.getImage(node);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText(Object element)
  {
    if (element instanceof ASTVariable)
    {
      CommonTree node = ((ASTVariable) element).getCommonTree();
      return _labelProvider.getText(node);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue,
   *      org.eclipse.debug.ui.IValueDetailListener)
   */
  public void computeDetail(IValue value, IValueDetailListener listener)
  {
    /*
     * for some reason this code is producing a NPE..
     */
    // try
    // {
    // String detail = "";
    // if (value != null)
    // {
    // detail = value.getValueString();
    // listener.detailComputed(value, detail);
    // }
    // }
    // catch (Exception e)
    // {
    // LOGGER.error("Failed while computing detail for " + value, e);
    // }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
   */
  public IEditorInput getEditorInput(Object element)
  {
    if (element instanceof IFile)
    {
      return new FileEditorInput((IFile) element);
    }
    if (element instanceof ILineBreakpoint)
    {
      IMarker marker = ((ACTRBreakpoint) element).getMarker();
      if (marker == null) return null;
      return new FileEditorInput((IFile) marker.getResource());
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput,
   *      java.lang.Object)
   */
  public String getEditorId(IEditorInput input, Object element)
  {
    if (element instanceof IFile || element instanceof ACTRBreakpoint)
    {
      return "org.jactr.eclipse.ui.editor.JACTRModelEditor";
    }
    return null;
  }
}
