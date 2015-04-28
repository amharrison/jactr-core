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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;

public class ACTRDebugElement extends PlatformObject implements IDebugElement
{
  ACTRDebugTarget _target;

  public ACTRDebugElement()
  {
  }

  protected void setDebugTarget(ACTRDebugTarget target)
  {
    _target = target;
  }

  public ACTRDebugTarget getACTRDebugTarget()
  {
    return _target;
  }

  public IDebugTarget getDebugTarget()
  {
    return _target;
  }

  public ILaunch getLaunch()
  {
    return _target.getLaunch();
  }

  public String getModelIdentifier()
  {
    return ACTRSession.ACTR_DEBUG_MODEL;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter)
  {
    if (adapter == IDebugElement.class) return this;
    return super.getAdapter(adapter);
  }

  /**
   * Fires a debug event
   * 
   * @param event
   *          the event to be fired
   */
  static public void fireEvent(DebugEvent event)
  {
    DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
  }

  public void fireCreationEvent()
  {
    fireCreationEvent(this);
  }

  /**
   * Fires a <code>CREATE</code> event for this element.
   */
  static public void fireCreationEvent(Object source)
  {
    fireEvent(new DebugEvent(source, DebugEvent.CREATE));
  }

  public void fireResumeEvent(int detail)
  {
    fireResumeEvent(this, detail);
  }

  /**
   * Fires a <code>RESUME</code> event for this element with the given detail.
   * 
   * @param detail
   *          event detail code
   */
  static public void fireResumeEvent(Object source, int detail)
  {
    fireEvent(new DebugEvent(source, DebugEvent.RESUME, detail));
  }

  public void fireSuspendEvent(int detail)
  {
    fireSuspendEvent(this, detail);
  }

  /**
   * Fires a <code>SUSPEND</code> event for this element with the given detail.
   * 
   * @param detail
   *          event detail code
   */
  static public void fireSuspendEvent(Object source, int detail)
  {
    fireEvent(new DebugEvent(source, DebugEvent.SUSPEND, detail));
  }

  public void fireTerminateEvent()
  {
    fireTerminateEvent(this);
  }

  /**
   * Fires a <code>TERMINATE</code> event for this element.
   */
  static public void fireTerminateEvent(Object source)
  {
    fireEvent(new DebugEvent(source, DebugEvent.TERMINATE));
  }

  public void fireChangeEvent()
  {
    fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
  }
}
