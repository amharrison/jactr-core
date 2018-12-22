/*
 * Created on Feb 25, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.identifier.impl;

import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;

/**
 * basic implementation of an identifier
 * 
 * @author developer
 */
public class BasicIdentifier implements IIdentifier, Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = -5821869038992193202L;

  static private long       ID               = 0;

  private String            _name;

  private final Type              _type;

  private final IIdentifier       _owner;

  private final long              _id;

  private String            _toString;

  public BasicIdentifier(String name, Type type, IIdentifier owner)
  {
    _name = name;
    _type = type;
    _owner = owner;

    synchronized (BasicIdentifier.class)
    {
      _id = ++ID;
    }
  }

  /**
   * @see org.commonreality.identifier.IIdentifier#getName()
   */
  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  /**
   * @see org.commonreality.identifier.IIdentifier#getOwner()
   */
  public IIdentifier getOwner()
  {
    return _owner;
  }

  public long getId()
  {
    return _id;
  }
  
  

//  @Override
//  public int hashCode()
//  {
//    final int PRIME = 31;
//    int result = 1;
//    result = PRIME * result + (int) (_id ^ (_id >>> 32));
//    result = PRIME * result + ((_owner == null) ? 0 : _owner.hashCode());
//    return result;
//  }
//
//  @Override
//  public boolean equals(Object obj)
//  {
//    if (this == obj) return true;
//    if (obj == null) return false;
//    if (getClass() != obj.getClass()) return false;
//    final BasicIdentifier other = (BasicIdentifier) obj;
//    if (_id != other._id) return false;
//    if (_owner == null)
//    {
//      if (other._owner != null) return false;
//    }
//    else if (!_owner.equals(other._owner)) return false;
//    return true;
//  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_id ^ (_id >>> 32));
    result = prime * result + ((_owner == null) ? 0 : _owner.hashCode());
    result = prime * result + ((_type == null) ? 0 : _type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final BasicIdentifier other = (BasicIdentifier) obj;
    if (_id != other._id) return false;
    if (_owner == null)
    {
      if (other._owner != null) return false;
    }
    else if (!_owner.equals(other._owner)) return false;
    if (_type == null)
    {
      if (other._type != null) return false;
    }
    else if (!_type.equals(other._type)) return false;
    return true;
  }

  /**
   * @see org.commonreality.identifier.IIdentifier#getType()
   */
  public Type getType()
  {
    return _type;
  }

  @Override
  synchronized public String toString()
  {
    if (_toString == null)
      _toString = "[" + getType() + ":" + getId() + ":" + getName() + ":"+hashCode()+"]";
    return _toString;
  }
}
