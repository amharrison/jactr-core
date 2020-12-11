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
package org.jactr.core.production.five;

 
import org.jactr.core.model.IModel;
import org.jactr.core.production.basic.AbstractProduction;
import org.jactr.core.production.basic.BasicSymbolicProduction;
import org.jactr.core.production.event.ProductionEvent;
import org.jactr.core.production.four.ISubsymbolicProduction4;
import org.jactr.core.production.four.ISymbolicProduction4;
import org.slf4j.LoggerFactory;

/*
 * Not thread safe this once added to memory, the production cannot be changed.
 */
/**
 * Description of the Class
 * 
 * @author harrison
 * @created February 5, 2003
 */
public class DefaultSymbolicProduction5 extends BasicSymbolicProduction
    implements ISymbolicProduction4
{


  private static transient org.slf4j.Logger LOGGER      = LoggerFactory
                                               .getLogger(DefaultSymbolicProduction5.class
                                                   .getName());

  /**
   * Description of the Field
   * 
   * @since
   */
  public boolean               _successful = true;

  /**
   * Description of the Field
   * 
   * @since
   */
  public boolean               _failure;

  public DefaultSymbolicProduction5(AbstractProduction prod, IModel parentModel)
  {
    super(prod, parentModel);
  }

  public boolean isSuccessful()
  {
    return _successful;
  }

  public boolean isFailure()
  {
    return _failure;
  }

  
  public void setSuccessful(boolean s)
  {
    Boolean oldValue = Boolean.valueOf(_successful);
    Boolean newValue = Boolean.valueOf(s);
    _successful = s;
    if (_production.hasListeners())
      _production.dispatch(new ProductionEvent(_production,
          ISubsymbolicProduction4.SUCCESS, newValue, oldValue));
  }

  
  public void setFailure(boolean f)
  {
    Boolean oldValue = Boolean.valueOf(_failure);
    Boolean newValue = Boolean.valueOf(f);
    _failure = f;
    if (_production.hasListeners()) _production.dispatch(new ProductionEvent(_production,
        ISubsymbolicProduction4.FAILURE,
        newValue, oldValue));
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_failure ? 1231 : 1237);
    result = prime * result + (_successful ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    DefaultSymbolicProduction5 other = (DefaultSymbolicProduction5) obj;
    if (_failure != other._failure) return false;
    if (_successful != other._successful) return false;
    return true;
  }

}