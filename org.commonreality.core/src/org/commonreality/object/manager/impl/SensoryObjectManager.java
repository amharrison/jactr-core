/*
 * Created on May 14, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.object.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.event.IObjectListener;

/**
 * @author developer
 */
public class SensoryObjectManager<O extends ISensoryObject, L extends IObjectListener<O>>
    extends GeneralObjectManager<O, L>
{

  protected Map<IIdentifier, Collection<IIdentifier>> _bySensor;

  protected Map<IIdentifier, Collection<IIdentifier>> _byAgent;

  /**
   * 
   */
  public SensoryObjectManager()
  {
    super();
    _bySensor = new HashMap<IIdentifier, Collection<IIdentifier>>();
    _byAgent = new HashMap<IIdentifier, Collection<IIdentifier>>();
  }

  protected Collection<IIdentifier> getIdentifierBy(
      Map<IIdentifier, Collection<IIdentifier>> map, IIdentifier identifier,
      boolean create)
  {
    Collection<IIdentifier> rtn = null;
    synchronized (map)
    {
      rtn = map.get(identifier);
      if (rtn == null) if (create)
      {
        rtn = new ArrayList<IIdentifier>();
        map.put(identifier, rtn);
      }
      else
        rtn = Collections.EMPTY_LIST;
    }
    return rtn;
  }

  /**
   * @see org.commonreality.object.manager.IEfferentObjectManager#getIdentifiersByAgent(org.commonreality.identifier.IIdentifier)
   */
  public Collection<IIdentifier> getIdentifiersByAgent(IIdentifier agentId)
  {
    Collection<IIdentifier> ids = getIdentifierBy(_byAgent, agentId, false);
    return Collections.unmodifiableCollection(ids);
  }

  /**
   * @see org.commonreality.object.manager.IEfferentObjectManager#getIdentifiersBySensor(org.commonreality.identifier.IIdentifier)
   */
  public Collection<IIdentifier> getIdentifiersBySensor(IIdentifier sensorId)
  {
    Collection<IIdentifier> ids = getIdentifierBy(_bySensor, sensorId, false);
    return Collections.unmodifiableCollection(ids);
  }

  protected void addBy(ISensoryIdentifier id)
  {
    Collection<IIdentifier> ids = getIdentifierBy(_bySensor, id.getSensor(), true);
    synchronized (ids)
    {
      ids.add(id);
    }
    ids = getIdentifierBy(_byAgent, id.getAgent(), true);
    synchronized (ids)
    {
      ids.add(id);
    }
  }

  protected void removeBy(ISensoryIdentifier id)
  {
    Collection<IIdentifier> ids = getIdentifierBy(_bySensor, id.getSensor(), false);
    synchronized (ids)
    {
      ids.remove(id);
    }
    ids = getIdentifierBy(_byAgent, id.getAgent(), false);
    synchronized (ids)
    {
      ids.remove(id);
    }
  }

  /**
   * add the specific simulation object and fire the add event
   */
  @Override
  public boolean add(O object)
  {
    boolean rtn = super.add(object);
    if (rtn) addBy(object.getIdentifier());
    return rtn;
  }

  @Override
  public boolean add(Collection<O> objects)
  {
    boolean rtn = super.add(objects);
    if (rtn) for (O object : objects)
      addBy(object.getIdentifier());
    return rtn;
  }

  /**
   * remove said object and fire event
   * 
   * @param identifier
   */
  @Override
  public boolean remove(IIdentifier identifier)
  {
    boolean rtn = super.remove(identifier);
    if (identifier instanceof ISensoryIdentifier && rtn)
      removeBy((ISensoryIdentifier) identifier);
    return rtn;
  }

  @Override
  public boolean remove(Collection<IIdentifier> ids)
  {
    boolean rtn = super.remove(ids);
    if (rtn) for (IIdentifier id : ids)
      if (id instanceof ISensoryIdentifier) removeBy((ISensoryIdentifier) id);
    return rtn;
  }

}
