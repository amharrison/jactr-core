/*
 * Created on May 16, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.executor;

import java.util.concurrent.Executor;

/**
 * @author developer
 */
public class InlineExecutor implements Executor
{

  static private InlineExecutor _executor = new InlineExecutor();

  static public Executor get()
  {
    return _executor;
  }

  /**
   * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
   */
  public void execute(Runnable arg0)
  {
      arg0.run();
  }

}
