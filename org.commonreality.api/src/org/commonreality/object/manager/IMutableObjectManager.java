/*
 * Created on May 11, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.object.manager;

import java.util.Collection;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.manager.event.IObjectListener;

/**
 * The mutable object manager extends the {@link IObjectManager} to support the
 * adding, removing, and updating of simulation objects. <br>
 * <br>
 * add and remove are fairly obvious, update requires a little more finese.<br>
 * <br>
 * Instead of directly updating objects, we use {@link IObjectDelta} which allow
 * you to collect all the changes requested for an object and then apply them at
 * once. The {@link IObjectDelta} is also sent as part of the event notification
 * so that listeners know exactly what has changed.<br>
 * <br>
 * Note, this has methods for the adding and removing of objects - not the
 * creation of them. It is the {@link IRequestableObjectManager} that is able to
 * create new objects since the creation has to go through CR.<br>
 * 
 * @see IRequestableObjectManager
 * @see IObjectManager
 * @author developer
 */
public interface IMutableObjectManager<O extends ISimulationObject, L extends IObjectListener<O>>
    extends IObjectManager<O, L>
{
  /**
   * add the specific simulation object and fire the add event
   */
  public boolean add(O object);

  public boolean add(Collection<O> objects);

  /**
   * remove said object and fire event
   * 
   * @param identifier
   */
  public boolean remove(IIdentifier identifier);

  public boolean remove(Collection<IIdentifier> identifiers);

  /**
   * update the object that's change is desrcibed by the delta and fire the
   * appropriate event
   * 
   * @param delta
   */
  public boolean update(IObjectDelta delta);

  /**
   * update all of these bad boys.
   * 
   * @param deltas
   */
  public boolean update(Collection<IObjectDelta> deltas);
}
