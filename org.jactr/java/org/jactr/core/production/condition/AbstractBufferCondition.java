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

import java.util.Objects;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.model.IModel;
import org.jactr.core.utils.ModelerException;
import org.slf4j.LoggerFactory;

/**
 * @bug why is this not an interface with an abstract implementation like
 *      IBufferAction?
 */
public abstract class AbstractBufferCondition extends AbstractSlotCondition
    implements IBufferCondition
{
  /**
   * Logger definition
   */
  static final private org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(AbstractBufferCondition.class);

  private String                        _bufferName;

  public AbstractBufferCondition(String bufferName)
  {
    super();
    _bufferName = bufferName;
  }

  public String getBufferName()
  {
    return _bufferName;
  }

  @Override
  protected String createToString()
  {
    return "[" + _bufferName + ":" + super.createToString() + "]";
  }

  protected IActivationBuffer getActivationBuffer(IModel model)
  {
    IActivationBuffer buffer = model.getActivationBuffer(_bufferName);
    if (buffer == null)
    {
      LOGGER.error("IActivationBuffer named " + _bufferName + " was not found");
      throw new ModelerException(_bufferName + " is not a valid buffer.", null,
          "Are you sure " + _bufferName
              + " is correct? Has the buffer been installed?");
    }
    return buffer;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(_bufferName);
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    AbstractBufferCondition other = (AbstractBufferCondition) obj;
    return Objects.equals(_bufferName, other._bufferName);
  }

}
