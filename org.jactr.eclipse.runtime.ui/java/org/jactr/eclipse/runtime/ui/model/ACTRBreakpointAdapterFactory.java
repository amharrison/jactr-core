/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM
 * Corporation - initial API and implementation Bjorn Freeman-Benson - initial
 * API and implementation
 ******************************************************************************/
package org.jactr.eclipse.runtime.ui.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;

/**
 * Creates a toggle breakpoint adapter
 */
public class ACTRBreakpointAdapterFactory implements IAdapterFactory
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
      .getLog(ACTRBreakpointAdapterFactory.class);

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
   * java.lang.Class)
   */
  public Object getAdapter(Object adaptableObject, Class adapterType)
  {
    try
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Breakpoint adapter requested");
      IResource resource = null;
      if (adaptableObject instanceof ACTRModelEditor)
      {
        ACTRModelEditor editorPart = (ACTRModelEditor) adaptableObject;
        resource = editorPart.getEditorInput().getAdapter(IResource.class);
      }
      else if (adaptableObject instanceof XtextEditor)
        resource = ((XtextEditor) adaptableObject).getResource();

      if (resource != null)
      {
        String extension = resource.getFileExtension();
        if (extension != null && CompilationUnitManager.isJACTRModel(resource)
            || "jactr".equals(extension))
          return new ACTRBreakpointAdapter();
      }
    }
    catch (Exception e)
    {

    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  public Class[] getAdapterList()
  {
    return new Class[] { IToggleBreakpointsTarget.class };
  }
}
