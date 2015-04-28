/*
 * Created on Jun 11, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.debug.elements;

import java.text.NumberFormat;
import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.tools.async.shadow.ShadowController;

public class ACTRStackFrame extends ACTRDebugElement implements IStackFrame
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRStackFrame.class);

  double                             _time;

  String                             _productionName;

  int                                _line;

  ACTRThread                         _thread;

  String                             _displayText;

  public ACTRStackFrame(ACTRThread thread, double time, String productionName,
      int lineNumber)
  {
    _time = time;
    _productionName = productionName;
    _line = lineNumber;
    _thread = thread;

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(NumberFormat.getNumberInstance().format(_time)).append("] : ");
    sb.append(_productionName);
    _displayText = sb.toString();
    setDebugTarget((ACTRDebugTarget) thread.getDebugTarget());
  }

  public String getProductionName()
  {
    return _productionName;
  }

  public String getSourceName()
  {
    return _thread.getSourceName();
  }

  public int getCharEnd() throws DebugException
  {
    return -1;
  }

  public int getCharStart() throws DebugException
  {
    return -1;
  }

  public int getLineNumber() throws DebugException
  {
    return _line;
  }

  public String getName() throws DebugException
  {
    return _displayText;
  }

  public IRegisterGroup[] getRegisterGroups() throws DebugException
  {
    return new IRegisterGroup[0];
  }

  public IThread getThread()
  {
    return _thread;
  }

  public IVariable[] getVariables() throws DebugException
  {
    if (!isTop()) return new IVariable[0];

    ShadowController sc = _target.getACTRSession().getShadowController();
    CommonTree modelDescriptor = sc.getModelDescriptor(_thread.getName());
    CommonTree breakPoint = sc.getBreakpointData(_thread.getName());

    ArrayList<IVariable> variables = new ArrayList<IVariable>();
    if (modelDescriptor != null)
      variables.add(new ASTVariable(modelDescriptor, "current model state",
          _target));

    if (breakPoint != null)
      variables
          .add(new ASTVariable(breakPoint, "current break point", _target));

    return variables.toArray(new IVariable[0]);
  }

  public boolean hasRegisterGroups() throws DebugException
  {
    return false;
  }

  public boolean hasVariables() throws DebugException
  {
    boolean isTopFrame = isTop();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("is the top frame : " + isTopFrame);
    return isTopFrame;
  }

  public boolean isTop()
  {
    try
    {
      return _thread.getTopStackFrame() == this;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  public boolean canStepInto()
  {
    if (isTop()) return _thread.canStepInto();
    return false;
  }

  public boolean canStepOver()
  {
    if (isTop()) return _thread.canStepOver();
    return false;
  }

  public boolean canStepReturn()
  {
    if (isTop()) return _thread.canStepReturn();
    return false;
  }

  public boolean isStepping()
  {
    return _thread.isStepping();
  }

  public void stepInto() throws DebugException
  {
    _thread.stepInto();
  }

  public void stepOver() throws DebugException
  {
    _thread.stepOver();
  }

  public void stepReturn() throws DebugException
  {
    _thread.stepReturn();
  }

  public boolean canResume()
  {
    return _thread.canResume();
  }

  public boolean canSuspend()
  {
    return _thread.canSuspend();
  }

  public boolean isSuspended()
  {
    return _thread.isSuspended();
  }

  public void resume() throws DebugException
  {
    if (canResume()) _thread.resume();
  }

  public void suspend() throws DebugException
  {
    if (canSuspend()) _thread.suspend();
  }

  public boolean canTerminate()
  {
    return _thread.canTerminate();
  }

  public boolean isTerminated()
  {
    return _thread.isTerminated();
  }

  public void terminate() throws DebugException
  {
    _thread.terminate();
  }

}
