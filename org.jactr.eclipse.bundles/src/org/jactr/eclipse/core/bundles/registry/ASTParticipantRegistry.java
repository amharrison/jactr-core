/*
 * Created on Mar 30, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.core.bundles.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.jactr.eclipse.core.bundles.descriptors.ASTParticipantDescriptor;

public class ASTParticipantRegistry extends
    AbstractExtensionPointRegistry<ASTParticipantDescriptor>
{
  /**
   * Logger definition
   */

  static private final transient Log          LOGGER   = LogFactory
                                                           .getLog(ASTParticipantRegistry.class);

  static private final ASTParticipantRegistry _default = new ASTParticipantRegistry();

  static public ASTParticipantRegistry getRegistry()
  {
    return _default;
  }

  private ASTParticipantRegistry()
  {
    super("org.jactr.osgi.astparticipants");
  }

  @Override
  protected ASTParticipantDescriptor createDescriptor(
      IPluginElement extPointElement)
  {
    if (extPointElement.getName().equals("astparticipant"))
    {
      String contributingClass = extPointElement.getAttribute(
          "contributingClass")
          .getValue();
      String participantClassName = extPointElement.getAttribute(
          "class")
          .getValue();
      String contentLocation = extPointElement.getAttribute("content")
          .getValue();

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Adding extension of astparticipant from "
            + extPointElement.getPluginBase().getId() + " contributor:"
            + contributingClass
            + " participant:" + participantClassName
            + " content::" + contentLocation);

      return new ASTParticipantDescriptor(extPointElement.getPluginBase()
          .getId(), participantClassName, contributingClass, contentLocation);
    }
    else
      throw new IllegalArgumentException(
          "Was expecting astparticipant tag, got " + extPointElement.getName());

  }

  @Override
  protected ASTParticipantDescriptor createDescriptor(
      IConfigurationElement extPointElement)
  {
    if (extPointElement.getName().equals("astparticipant"))
      return new ASTParticipantDescriptor(extPointElement);
    else
      throw new IllegalArgumentException(
          "Was expecting astparticipant tag, got " + extPointElement.getName());
  }

}
