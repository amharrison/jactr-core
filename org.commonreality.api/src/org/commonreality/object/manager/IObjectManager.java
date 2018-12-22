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
import java.util.concurrent.Executor;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.manager.event.IObjectListener;

/**
 * An object manager allows a set of simulation objects to be tracked and events
 * fired when they are changed. specific interface exist for all of the
 * simulation objects <br>
 * This interface is for read only. There are also the
 * {@link IMutableObjectManager} and {@link IRequestableObjectManager} which
 * permit the modification, addition, and request of objects
 * 
 * @author developer
 */
public interface IObjectManager<O extends ISimulationObject, L extends IObjectListener<O>>
{
  /**
   * return all the identifiers of simulation objects that this manager is
   * tracking
   * 
   * @return
   */
  public Collection<IIdentifier> getIdentifiers();

  /**
   * return the simulation object refered to by the identifier
   * 
   * @param affId
   * @return
   */
  public O get(IIdentifier affId);

  /**
   * add an event listener
   * 
   * @param listener
   * @param exector
   */
  public void addListener(L listener, Executor exector);

  /**
   * remove said listener
   * 
   * @param listener
   */
  public void removeListener(L listener);

  /**
   * are there any event listeners?
   * 
   * @return
   */
  public boolean hasListeners();

  /**
   * return all the installed listeners
   * 
   * @return
   */
  public Collection<L> getListeners();
  
  /**
   * wait until something has been added, removed, updated
   * @throws InterruptedException
   */
  public void waitForChange() throws InterruptedException;
}
