/*
 * Created on Mar 15, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.content;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.ICompilationUnitListener;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ACTRContentProvider extends AbstractACTRContentProvider implements
    ITreeContentProvider, ICompilationUnitListener
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRContentProvider.class);

  private ICompilationUnit           _input;

  Viewer                             _viewer;

  ACTRContentSorter                  _sorter;

  public ACTRContentProvider()
  {
    this(false);
  }

  public ACTRContentProvider(boolean sort)
  {
    super();
    if (sort) _sorter = new ACTRContentSorter();
  }

  @Override
  public void dispose()
  {
    clear();
  }

  @Override
  public Object[] getChildren(Object parentElement)
  {
    Object[] rtn = super.getChildren(parentElement);
    if (_sorter != null && _viewer != null) _sorter.sort(_viewer, rtn);
    return rtn;
  }

  @Override
  public Object[] getElements(Object inputElement)
  {
    CommonTree root = getRoot();
    if (root != null) return new Object[] { root };

    ASTSupport support = new ASTSupport();
    return new Object[] { support.createModelTree("invalid model") };
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("new " + newInput + " old " + oldInput);

    if (oldInput != null && oldInput instanceof ICompilationUnit)
      ((ICompilationUnit) oldInput).removeListener(this);

    _viewer = viewer;
    _input = (ICompilationUnit) newInput;
    if (_input != null)
    {
      setRoot(_input.getModelDescriptor());
      _input.addListener(this);
    }
  }

  public void updated(ICompilationUnit compUnit)
  {
    setRoot(_input.getModelDescriptor());
    if (_viewer != null)
      _viewer.getControl().getDisplay().asyncExec(new Runnable() {

        public void run()
        {
          if (!_viewer.getControl().isDisposed()) _viewer.refresh();
        }
      });
  }
}
