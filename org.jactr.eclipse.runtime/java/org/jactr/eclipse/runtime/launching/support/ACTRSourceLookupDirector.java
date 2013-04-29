/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM
 * Corporation - initial API and implementation Bjorn Freeman-Benson - initial
 * API and implementation
 ******************************************************************************/
package org.jactr.eclipse.runtime.launching.support;

import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.pde.internal.launching.sourcelookup.PDESourceLookupDirector;

/**
 * PDA source lookup director. For PDA source lookup there is one source lookup
 * participant.
 */
public class ACTRSourceLookupDirector extends PDESourceLookupDirector
{
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
   */
  @Override
  public void initializeParticipants()
  {
    super.initializeParticipants();
    addParticipants(new ISourceLookupParticipant[] { new ACTRSourceLookupParticipant() });
  }
}
