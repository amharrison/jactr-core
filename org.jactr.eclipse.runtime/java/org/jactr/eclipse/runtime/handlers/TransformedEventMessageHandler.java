/*
 * Created on Mar 7, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.session.ISessionInfo;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.trace.RuntimeTraceManager;
import org.jactr.tools.tracer.transformer.AbstractTransformedEvent;

public class TransformedEventMessageHandler implements
    IMessageHandler<AbstractTransformedEvent>
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(TransformedEventMessageHandler.class);

  ACTRSession _session;
  RuntimeTraceManager _manager;
  
  public TransformedEventMessageHandler(ACTRSession session)
  {
    _session = session;
    _manager = RuntimePlugin.getDefault().getRuntimeTraceManager();
  }
  
  // public void handleMessage(IoSession arg0, ITransformedEvent event) throws
  // Exception
  // {
  // }

  @SuppressWarnings("rawtypes")
  @Override
  public void accept(ISessionInfo t, AbstractTransformedEvent event)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Got transformed event for "+event);
    
    _manager.fireEvent(event, _session.getSession());
  }
  
}
