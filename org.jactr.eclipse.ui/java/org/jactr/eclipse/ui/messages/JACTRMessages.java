/*
 * Created on Jul 8, 2004
 * Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu This library is free
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

package org.jactr.eclipse.ui.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * @author harrison
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JACTRMessages
{

  private static final String BUNDLE_NAME = "org.jactr.eclipse.ui.messages.Messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE =
    ResourceBundle.getBundle(BUNDLE_NAME);

  private JACTRMessages() {
  }
  
  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  static public ResourceBundle getResourceBundle()
  {
    return RESOURCE_BUNDLE;
  }

}
