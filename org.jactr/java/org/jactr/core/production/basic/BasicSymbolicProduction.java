/*
 * Created on Oct 24, 2006 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.jactr.core.production.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jactr.core.model.IModel;
import org.jactr.core.production.ISymbolicProduction;
import org.jactr.core.production.IllegalProductionStateException;
import org.jactr.core.production.action.IAction;
import org.jactr.core.production.condition.ICondition;
import org.jactr.core.production.event.ProductionEvent;
import org.jactr.core.utils.DefaultAdaptable;
import org.jactr.core.utils.collections.CachedCollection;
import org.slf4j.LoggerFactory;

/**
 * this implementation is not thread safe for the add/remove of
 * conditions/actions
 * 
 * @author developer
 */
public class BasicSymbolicProduction extends DefaultAdaptable implements
    ISymbolicProduction
{
  /**
   * logger definition
   */
  static private final transient org.slf4j.Logger         LOGGER = LoggerFactory
                                              .getLogger(BasicSymbolicProduction.class);

  protected Collection<IAction>    _actions;

  protected Collection<ICondition> _conditions;

  protected AbstractProduction     _production;

  protected String                 _name;

  public BasicSymbolicProduction(AbstractProduction production, IModel model)
  {
    _production = production;
    _actions = new CachedCollection<IAction>(new ArrayList<IAction>(3));
    _conditions = new CachedCollection<ICondition>(new ArrayList<ICondition>(3));
  }

  public void addAction(IAction cons)
  {
    if (_production.isEncoded())
      throw new IllegalProductionStateException(
          "Cannot add actions to an encoded production");

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding " + cons);

    _actions.add(cons);

    if (_production.hasListeners())
      _production.dispatch(new ProductionEvent(_production,
          ProductionEvent.Type.ACTION_ADDED, cons));
  }

  public void addCondition(ICondition cond)
  {
    if (_production.isEncoded())
      throw new IllegalProductionStateException(
          "Cannot add conditions to an encoded production");

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding " + cond);

    _conditions.add(cond);

    if (_production.hasListeners())
      _production.dispatch(new ProductionEvent(_production,
          ProductionEvent.Type.CONDITION_ADDED, cond));
  }

  public void dispose()
  {
    for (IAction action : _actions)
      action.dispose();
    _actions.clear();
//    _actions = null;

    for (ICondition condition : _conditions)
      condition.dispose();
    _conditions.clear();
//    _conditions = null;
    
    _production = null;
  }

  public Collection<IAction> getActions()
  {
    return Collections.unmodifiableCollection(_actions);
  }

  public Collection<ICondition> getConditions()
  {
    return Collections.unmodifiableCollection(_conditions);
  }

  public String getName()
  {
    return _name;
  }

  public int getNumberOfActions()
  {
    return _actions.size();
  }

  public int getNumberOfConditions()
  {
    return _conditions.size();
  }

  public void removeAction(IAction cons)
  {
    if (_production.isEncoded())
      throw new IllegalProductionStateException(
          "Cannot remove actions from an encoded production");

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing " + cons);

    _actions.remove(cons);

    if (_production.hasListeners())
      _production.dispatch(new ProductionEvent(_production,
          ProductionEvent.Type.ACTION_REMOVED, cons));

  }

  public void removeCondition(ICondition retr)
  {
    if (_production.isEncoded())
      throw new IllegalProductionStateException(
          "Cannot remove conditions from an encoded production");

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing " + retr);

    _conditions.remove(retr);

    if (_production.hasListeners())
      _production.dispatch(new ProductionEvent(_production,
          ProductionEvent.Type.CONDITION_REMOVED, retr));

  }

  public void setName(String str)
  {
    if (_production.isEncoded())
      throw new IllegalProductionStateException(
          "Cannot change the name of an encoded production");

    _name = str;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_actions == null ? 0 : _actions.hashCode());
    result = prime * result
        + (_conditions == null ? 0 : _conditions.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BasicSymbolicProduction other = (BasicSymbolicProduction) obj;
    if (_actions == null)
    {
      if (other._actions != null) return false;
    }
    else if (!_actions.equals(other._actions)) return false;
    if (_conditions == null)
    {
      if (other._conditions != null) return false;
    }
    else if (!_conditions.equals(other._conditions)) return false;
    return true;
  }

  public void encode()
  {
    // noop
  }

}
