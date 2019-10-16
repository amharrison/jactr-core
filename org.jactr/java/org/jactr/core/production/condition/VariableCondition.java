/*
 * Created on Sep 22, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package org.jactr.core.production.condition;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.model.IModel;
import org.jactr.core.production.VariableBindings;
import org.jactr.core.production.condition.match.GeneralMatchFailure;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.slf4j.LoggerFactory;

/**
 * @author harrison TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class VariableCondition extends AbstractBufferCondition
{
  /**
   * Logger definition
   */

  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(VariableCondition.class);

  private String                                  _variableName;

  private IRequest                                _indirectRequest;

  /**
   * @param bufferName
   */
  public VariableCondition(String bufferName, String variableName)
  {
    super(bufferName);
    _variableName = variableName;
  }

  public String getVariableName()
  {
    return _variableName;
  }

  public VariableCondition clone(IModel model, VariableBindings bindings)
      throws CannotMatchException
  {
    return new VariableCondition(getBufferName(), _variableName);
  }

  public int bind(IModel model, VariableBindings variableBindings,
      boolean isIterative) throws CannotMatchException
  {
    int unresolved = 0;

    String variableName = getVariableName();
    String bufferVariable = "=" + getBufferName();

    /**
     * <code>
     * =buffer>
     *  =buffer
     * </code>
     */
    if (variableName.equals(bufferVariable)) return 0;

    Object resolvedValue = variableBindings.get(variableName);

    if (resolvedValue == null && !isIterative)
      throw new CannotMatchException(new GeneralMatchFailure(this,
          String.format("%s is undefined", variableName)));

    try
    {
      if (resolvedValue == null)
        unresolved = getRequest().bind(model, variableBindings, isIterative)
            + 1;
      else if (resolvedValue instanceof IChunk)
      {
        IChunk resolvedChunk = (IChunk) resolvedValue;
        unresolved = getRequest().bind(model,
            resolvedChunk.getSymbolicChunk().getName(),
            resolvedChunk.getSymbolicChunk(), variableBindings, isIterative);
      }
      else if (resolvedValue instanceof IRequest)
      {
        /**
         * indirect request, where meta is a request <code>
         * =meta>
         *  =meta
         * 
         * =retrieval>
         *  =meta
         * </code>
         */
        if (_indirectRequest == null)
          _indirectRequest = ((IRequest) resolvedValue).clone();

        if (_indirectRequest instanceof ChunkTypeRequest)
        {
          IChunk testChunk = (IChunk) variableBindings
              .get(bufferVariable);

          unresolved = ((ChunkTypeRequest) _indirectRequest).bind(testChunk,
              model.getActivationBuffer(getBufferName()),
              variableBindings, isIterative);
        }
        else
          unresolved = _indirectRequest.bind(model, variableBindings,
              isIterative);
      }
      else
        throw new CannotMatchException(new GeneralMatchFailure(this,
            String.format("%s is %s. Don't know how to proceed.",
                variableName, resolvedValue.getClass().getSimpleName())));
    }
    catch (CannotMatchException cme)
    {
      cme.getMismatch().setCondition(this);
      throw cme;
    }

    return unresolved;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + (_variableName == null ? 0 : _variableName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    VariableCondition other = (VariableCondition) obj;
    if (_variableName == null)
    {
      if (other._variableName != null) return false;
    }
    else if (!_variableName.equals(other._variableName)) return false;
    return true;
  }

}
