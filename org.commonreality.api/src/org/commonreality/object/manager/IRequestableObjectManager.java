/*
 * Created on May 12, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
import org.commonreality.object.manager.event.IObjectListener;

/**
 * The requestable object manager is the only object manager that is able to
 * create new objects. It does this by requesting a new identifier of the
 * appropriate type (as defined by the type of simulation object managed) from
 * CR. This is usually a bulk, cached operation to avoid costly blocking. Once
 * it has the identifier, it is free to create the object and return it. <br>
 * <br>
 * At some point in the future, the object will be added to the object manager
 * which will then trigger the appropriate events allowing CR to notify the
 * respective parties.<br>
 * 
 * @author developer
 */
public interface IRequestableObjectManager<O extends ISimulationObject, L extends IObjectListener<O>>
    extends IMutableObjectManager<O, L>
{
  /**
   * request than an object be created. The meaning of identifier is going to be
   * specific to the implementation. May block
   * 
   * @param identifier
   * @return
   */
  public O request(IIdentifier identifier);
  
  /**
   * request a new unique id. May block.
   * 
   * @param identifier
   * @return
   */
  public IIdentifier requestIdentifier(IIdentifier identifier);

  // /**
  // * explicitly asynch version of {@link #request(IIdentifier)}, supporting
  // bulk
  // * requests
  // *
  // * @param identifier
  // * @param numberOfInstances
  // * @return
  // */
  // public CompletableFuture<Collection<O>> request(IIdentifier identifier,
  // int numberOfInstances);
  //
  // public CompletableFuture<IIdentifier> requestIdentifiers(IIdentifier
  // agentId,
  // int numberOfIds);

  /**
   * When a new block of identifiers is made available from CR, they are added
   * to the object manager
   * 
   * @param freeIdentifiers
   */
  public void addFreeIdentifiers(Collection<IIdentifier> freeIdentifiers);
}
