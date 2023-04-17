package org.jactr.io2.source;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SourceGeneratorManager
{

  static private SourceGeneratorManager _instance = new SourceGeneratorManager();

  static public SourceGeneratorManager get()
  {
    return _instance;
  }

  private List<ISourceGenerator> _generators = new ArrayList<>();

  private SourceGeneratorManager()
  {

  }

  public void add(ISourceGenerator generator)
  {
    _generators.add(generator);
  }

  public Optional<ISourceGenerator> getSourceGenerator(
      Object astNode, String extension)
  {
    return _generators.stream().filter(g -> {
      return g.canGenerate(astNode, extension);
    }).findFirst();
  }

  public Optional<ISourceGenerator> getSourceGenerator(
      Object astNode, URI resource)
  {
    return _generators.stream().filter(g -> {
      return g.canSave(astNode, resource);
    }).findFirst();
  }

}
