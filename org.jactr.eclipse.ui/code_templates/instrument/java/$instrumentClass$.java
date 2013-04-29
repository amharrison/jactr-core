/*
 * Created on Mar 23, 2007
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
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
package $packageName$;

import org.apache.commons.logging.Log;  //standard logging support
import org.apache.commons.logging.LogFactory;
import org.jactr.core.model.IModel;
import org.jactr.instrument.IInstrument;

/**
 * all instruments follow this general pattern
 * @author developer
 *
 */
public class $instrumentClass$ implements IInstrument
{
 /**
 * Logger definition
 */
 static private final transient Log LOGGER = LogFactory
    .getLog($instrumentClass$.class);

 /**
  * initialize is called after install but before the model run starts
  * @see org.jactr.instrument.IInstrument#initialize()
  */
  public void initialize()
  {

  }

  public void install(IModel model)
  {

  }

  /**
   * unlike modules, instruments can be uninstalled, this is a good time
   * to release or flush resources
   * @see org.jactr.instrument.IInstrument#uninstall(org.jactr.core.model.IModel)
   */
  public void uninstall(IModel model)
  {

  }

}


