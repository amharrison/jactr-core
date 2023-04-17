package org.jactr.io2.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ASTGeneratorManager
{

  static private ASTGeneratorManager _instance = new ASTGeneratorManager();

  static public ASTGeneratorManager get()
  {
    return _instance;
  }

  private List<IASTGenerator> _generators = new ArrayList<>();

  private ASTGeneratorManager()
  {

  }

  public void add(IASTGenerator generator)
  {
    _generators.add(generator);
  }

  public Optional<IASTGenerator> getASTGenerator(String extension)
  {
    return _generators.stream().filter(g -> {
      return g.generates(extension);
    }).findFirst();
  }



}
