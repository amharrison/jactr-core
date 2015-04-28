/*
 * Created on Jun 9, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.debug.marker.ACTRBreakpoint;
import org.jactr.eclipse.runtime.debug.util.Utilities;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;

public class ACTRThread extends ACTRDebugElement implements IThread
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER      = LogFactory
                                                     .getLog(ACTRThread.class);

  String                             _modelName;

  boolean                            _isRunning;

  boolean                            _isSuspended;

  IBreakpoint[]                      _breakpoint = new IBreakpoint[0];

  int                                _maxHistory;

  int                                _cullHistoryTo;

  ACTRStackFrame                     _breakpointStackFrame;

  List<ACTRStackFrame>               _stackFrames;

  public ACTRThread(ACTRDebugTarget target, String modelName)
  {
    setDebugTarget(target);
    _modelName = modelName;
    _isRunning = true;
    _isSuspended = false;
    _stackFrames = new ArrayList<ACTRStackFrame>();
    _cullHistoryTo = RuntimePlugin.getDefault().getPluginPreferences().getInt(
        RuntimePreferences.DEBUG_STACK_PREF);
    _maxHistory = _cullHistoryTo * 3 / 2;
  }

  public String getSourceName()
  {
    ICompilationUnit compUnit = Utilities.getCompilationUnitForAlias(
        getLaunch(), _modelName);

    if (compUnit == null || !(compUnit instanceof IProjectCompilationUnit))
      return null;

    IResource resource = ((IProjectCompilationUnit) compUnit).getResource();

    CompilationUnitManager.release(compUnit);

    return resource.getProjectRelativePath().toString();
  }

  public IBreakpoint[] getBreakpoints()
  {
    return _breakpoint;
  }

  public String getName() throws DebugException
  {
    return _modelName;
  }

  @Override
  public String toString()
  {
    return _modelName;
  }

  public int getPriority() throws DebugException
  {
    return 0;
  }

  public IStackFrame[] getStackFrames() throws DebugException
  {
    synchronized (_stackFrames)
    {
      return _stackFrames.toArray(new IStackFrame[0]);
    }
  }

  public IStackFrame getTopStackFrame() throws DebugException
  {
    synchronized (_stackFrames)
    {
      if (_stackFrames.size() > 0)
        return _stackFrames.get(_stackFrames.size() - 1);
      else
        return null;
    }
  }

  /**
   * add a stack frame, and possible fire suspend event if it is a breakpoint
   * 
   * @param frame
   * @param isBreakpoint
   */
  public void addStackFrame(ACTRStackFrame frame, boolean isBreakpoint)
  {
    synchronized (_stackFrames)
    {
      if (_breakpointStackFrame != null)
        _stackFrames.remove(_breakpointStackFrame);

      if (_stackFrames.size() >= _maxHistory)
        _stackFrames.subList(0, _maxHistory - _cullHistoryTo).clear();

      _stackFrames.add(frame);
    }

    if (isBreakpoint)
    {
      _breakpointStackFrame = frame;
      setSuspended(true, frame.getProductionName());
    }
  }

  public boolean hasStackFrames() throws DebugException
  {
    synchronized (_stackFrames)
    {
      return _stackFrames.size() > 0;
    }
  }

  public void setSuspended(boolean suspended, String details)
  {
    if (isTerminated()) return;

    if (suspended == _isSuspended)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(_modelName + " Redundant suspend command - ignoring");
      return;
    }

    _isSuspended = suspended;
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(this + " suspended " + _isSuspended + " at " + details);

    _breakpoint = new IBreakpoint[0];

    int code = DebugEvent.UNSPECIFIED;

    if (_isSuspended)
    {
      if (details.length() != 0)
      {
        // try to find the exact break point
        IBreakpointManager mgr = DebugPlugin.getDefault()
            .getBreakpointManager();
        IBreakpoint[] breakpoints = mgr.getBreakpoints(getModelIdentifier());
        for (IBreakpoint breakpoint : breakpoints)
          if (breakpoint.getMarker() != null)
          {
            IMarker marker = breakpoint.getMarker();
            // check its name
            String name = marker.getAttribute(ACTRBreakpoint.BREAKPOINT_NAME,
                "");
            if (name.equals(details))
            {
              code = DebugEvent.BREAKPOINT;
              _breakpoint = new IBreakpoint[] { breakpoint };
              break;
            }
          }
      }
      fireSuspendEvent(code);
    }
    else
      fireResumeEvent(code);
  }

  public void setTerminated(boolean terminated)
  {
    _isRunning = !terminated;
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(this + " terminated " + _isRunning);

    if (!_isRunning) fireTerminateEvent();
  }

  public boolean canResume()
  {
    return getACTRDebugTarget().canResume();
  }

  public boolean canSuspend()
  {
    // if (isTerminated()) return false;

    // return !isSuspended();
    return getACTRDebugTarget().canSuspend();
  }

  public boolean isSuspended()
  {
    return getACTRDebugTarget().isSuspended();
  }

  public void resume() throws DebugException
  {
    getACTRDebugTarget().resume();
  }

  public void suspend() throws DebugException
  {
    getACTRDebugTarget().suspend();
  }

  public boolean canStepInto()
  {
    return false;
  }

  public boolean canStepOver()
  {
    return false;
  }

  public boolean canStepReturn()
  {
    // return isSuspended();
    return false;
  }

  public boolean isStepping()
  {
    return false;
  }

  public void stepInto() throws DebugException
  {

  }

  public void stepOver() throws DebugException
  {

  }

  public void stepReturn() throws DebugException
  {
    // resume();
  }

  public boolean canTerminate()
  {
    return getACTRDebugTarget().canTerminate();
  }

  public boolean isTerminated()
  {
    return getACTRDebugTarget().isTerminated();
  }

  public void terminate() throws DebugException
  {
    getACTRDebugTarget().terminate();
  }

}
