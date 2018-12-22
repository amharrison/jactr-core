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
package org.commonreality.object.manager.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.IObjectDelta;

/**
 * @author developer
 */
public class ObjectEvent<O extends ISimulationObject, L extends IObjectListener<O>>
    implements IObjectEvent<O, L>
{
  /**
   * logger definition
   */
  static private final Log                    LOGGER   = LogFactory
                                                           .getLog(ObjectEvent.class);

  private Type                                _type;

  private long                                _systemTime;

  private Collection<O>                       _objects = Collections.EMPTY_LIST;

  private Collection< ? extends IObjectDelta> _deltas  = Collections.EMPTY_LIST;

  public ObjectEvent(Type type)
  {
    _type = type;
    _systemTime = System.currentTimeMillis();
  }

  public ObjectEvent(Type type, Collection<O> objects)
  {
    this(type);
    _objects = new ArrayList<O>(objects);
  }

  public ObjectEvent(Collection< ? extends IObjectDelta> deltas)
  {
    this(Type.UPDATED);
    _deltas = new ArrayList<IObjectDelta>(deltas);
  }

  /**
   * @see org.commonreality.event.ICommonRealityEvent#getSystemTime()
   */
  final public long getSystemTime()
  {
    return _systemTime;
  }

  /**
   * @see org.commonreality.object.manager.event.IObjectEvent#getType()
   */
  final public org.commonreality.object.manager.event.IObjectEvent.Type getType()
  {
    return _type;
  }

  public void fire(L listener)
  {
    switch (getType())
    {
      case ADDED:
        listener.objectsAdded(this);
        break;
      case REMOVED:
        listener.objectsRemoved(this);
        break;
      case UPDATED:
        listener.objectsUpdated(this);
        break;
    }
  }

  /**
   * @see org.commonreality.object.manager.event.IObjectEvent#getObjects()
   */
  public Collection<O> getObjects()
  {
    return Collections.unmodifiableCollection(_objects);
  }

  public Collection<IObjectDelta> getDeltas()
  {
    return Collections.unmodifiableCollection(_deltas);
  }
}
