/*
 * Created on Mar 23, 2007
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
package org.jactr.eclipse.ui.wizards.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.pde.ui.IPluginContentWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


public class ACTRWizardSelectionPage extends WizardSelectionPage
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
      .getLog(ACTRWizardSelectionPage.class);
  
  List<IWizardNode> _wizardNodes;
  
  public ACTRWizardSelectionPage(String pageName, Collection<IWizardNode> wizards)
  {
    super(pageName);
    _wizardNodes = new ArrayList<IWizardNode>(wizards);
  }
  

  public void createControl(Composite parent)
  {
    initializeDialogUnits(parent);
    
    Composite container = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    final ListViewer listViewer = new ListViewer(container, SWT.VERTICAL | SWT.BORDER);
    listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
    listViewer.setContentProvider(new IStructuredContentProvider(){

      public void dispose()
      {
        
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
      {
        
      }

      public Object[] getElements(Object inputElement)
      {
        return _wizardNodes.toArray();
      }
      
    });
    
    listViewer.setInput(_wizardNodes);
    listViewer.addSelectionChangedListener(new ISelectionChangedListener(){
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("selection changed ");
        if(selection==null)
          {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("wizard node is null");
            setSelectedNode(null);
          }
        else
        {
          IWizardNode node = (IWizardNode)selection.getFirstElement();
          if (LOGGER.isDebugEnabled()) LOGGER.debug("wizard node "+node);
          setSelectedNode(node);
        }
      }
    });
    
    setControl(container);
  }
  
  public IPluginContentWizard getSelectedWizard()
  {
    IWizardNode node = getSelectedNode();
    if(node==null)
      return null;
    if(node.getWizard() instanceof IPluginContentWizard)
      return (IPluginContentWizard) node.getWizard();
    return null;
  }
  
  public boolean isPageComplete()
  {
    return getSelectedNode()==null || !isCurrentPage();
  }
  
  public boolean canFlipToNextPage()
  {
    boolean canFlip = super.canFlipToNextPage();
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Can flip "+canFlip);
    return canFlip;
  }

  public IWizardPage getNextPage()
  {
    IWizardPage page = super.getNextPage();
    if (LOGGER.isDebugEnabled()) LOGGER.debug("next page "+page);
    return page;
  }
  
}


