/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.core.production.six;

import java.util.Collection;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.IModel;
import org.jactr.core.production.CannotInstantiateException;
import org.jactr.core.production.ISubsymbolicProduction;
import org.jactr.core.production.ISymbolicProduction;
import org.jactr.core.production.VariableBindings;
import org.jactr.core.production.basic.AbstractInstantiation;
import org.jactr.core.production.basic.AbstractProduction;
import org.jactr.core.production.basic.BasicSymbolicProduction;
import org.jactr.core.production.condition.ICondition;

/*
 * IInstantiation is not thread safe - only one thread should work with a given
 * instantiation at a time
 */
/**
 * Description of the Class
 * 
 * @author harrison
 * @created February 5, 2003
 */
public class DefaultInstantiation6 extends AbstractInstantiation
{

  private static transient org.slf4j.Logger LOGGER = LoggerFactory
                                          .getLogger(DefaultInstantiation6.class
                                              .getName());

  public DefaultInstantiation6(AbstractProduction parent,
      Collection<ICondition> boundConditions,
 VariableBindings variableBindings)
      throws CannotInstantiateException
  {
    super(parent, boundConditions, variableBindings);
    
    /*
     * snag the values from the parent or the subsymbolic
     */
    ISubsymbolicProduction6 pssp = (ISubsymbolicProduction6) parent.getSubsymbolicProduction();
    ISubsymbolicProduction6 ssp = (ISubsymbolicProduction6) getSubsymbolicProduction();
    
    ssp.setCreationTime(parent.getModel().getAge());
    ssp.setExpectedUtility(pssp.getExpectedUtility());
    ssp.setUtility(pssp.getUtility());
    ssp.setFiringTime(pssp.getFiringTime());
    ssp.setReward(pssp.getReward());
  }
  
  
  @Override
  public void dispose()
  {
    getSubsymbolicProduction().dispose();
    super.dispose();
  }
  
  /**
   * overriden since the default version returns the subsymbolic of the instantiating
   * production
   * @return
   * @see org.jactr.core.production.basic.AbstractInstantiation#getSubsymbolicProduction()
   */
  @Override
  public ISubsymbolicProduction getSubsymbolicProduction()
  {
    return _subsymbolicProduction;
  }

  /**
   * we use the parent productions subsymbolic, so return null
   * 
   * @see org.jactr.core.production.basic.AbstractProduction#createSubsymbolicProduction(org.jactr.core.production.basic.AbstractProduction,
   *      org.jactr.core.model.IModel)
   */
  @Override
  protected ISubsymbolicProduction createSubsymbolicProduction(
      AbstractProduction production, IModel model)
  {
    return new DefaultSubsymbolicProduction6(production, model);
  }

  @Override
  protected ISymbolicProduction createSymbolicProduction(
      AbstractProduction production, IModel model)
  {
    return new BasicSymbolicProduction(production, model);
  }

}