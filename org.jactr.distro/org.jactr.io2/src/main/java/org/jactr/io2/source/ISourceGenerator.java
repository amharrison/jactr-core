package org.jactr.io2.source;

import java.io.IOException;
import java.net.URI;

public interface ISourceGenerator
{

  public boolean canGenerate(Object astNode, String format);

  public boolean canSave(Object astNode, URI resource);

  public String generate(Object astNode, String format);

  public void save(Object astNode, URI resource)
      throws IOException;
}
