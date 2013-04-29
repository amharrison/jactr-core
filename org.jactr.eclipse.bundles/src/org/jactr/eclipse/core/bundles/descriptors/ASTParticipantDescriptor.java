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
package org.jactr.eclipse.core.bundles.descriptors;

import org.eclipse.core.runtime.IConfigurationElement;

public class ASTParticipantDescriptor extends CommonExtensionDescriptor
{

  private final String _contributingClass;

  private final String _contentLocation;

  public ASTParticipantDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
    _contributingClass = descriptor.getAttribute("contributingClass");
    _contentLocation = descriptor.getAttribute("content");
  }

  public ASTParticipantDescriptor(String contributor, String participantClass,
      String contributingClass, String content)
  {
    super("org.jactr.io.astparticipants", contributor, "", participantClass, "");
    _contributingClass = contributingClass;
    _contentLocation = content;
  }
  

  public String getContributingClassName()
  {
    return _contributingClass;
  }

  public String getContentLocation()
  {
    return _contentLocation;
  }

}
