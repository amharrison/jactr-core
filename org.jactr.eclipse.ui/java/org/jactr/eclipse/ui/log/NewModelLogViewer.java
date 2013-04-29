/*
 * Created on Jun 8, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.log;

import org.apache.commons.logging.Log;  //standard logging support
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
public class NewModelLogViewer extends ViewPart
{

  private Composite top = null;
  private SashForm sashForm = null;

  @Override
  public void createPartControl(Composite parent)
  {
        top = new Composite(parent, SWT.NONE);
    // TODO Auto-generated method stub

        top.setLayout(new FillLayout());
        createSashForm();
  }

  @Override
  public void setFocus()
  {
    // TODO Auto-generated method stub

  }

  /**
   * This method initializes sashForm	
   *
   */
  private void createSashForm()
  {
    sashForm = new SashForm(top, SWT.NONE);
  }

}


