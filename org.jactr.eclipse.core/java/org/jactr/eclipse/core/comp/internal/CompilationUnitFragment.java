/**
 * Copyright (C) 1999-2007, Anthony Harrison anh23@pitt.edu This library is free
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
package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.antlr3.serialization.Serializer;

class CompilationUnitFragment implements Externalizable
{
  private CommonTree _modelDescriptor;

  private long       _modificationTime;

  private URI        _resourceLocation;

  public CompilationUnitFragment()
  {

  }

  public CompilationUnitFragment(URI location, CommonTree descriptor,
      long modTime)
  {
    _modelDescriptor = descriptor;
    _resourceLocation = location;
    _modificationTime = modTime;
  }

  public CommonTree getModelDescriptor()
  {
    return _modelDescriptor;
  }

  public long getModificationTime()
  {
    return _modificationTime;
  }

  public URI getURI()
  {
    return _resourceLocation;
  }

  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    _resourceLocation = (URI) in.readObject();
    _modificationTime = in.readLong();
    if (in.readBoolean()) _modelDescriptor = Serializer.read(in);
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(_resourceLocation);
    out.writeLong(_modificationTime);
    out.writeBoolean(_modelDescriptor != null);
    if (_modelDescriptor != null) Serializer.write(_modelDescriptor, out);
  }
}