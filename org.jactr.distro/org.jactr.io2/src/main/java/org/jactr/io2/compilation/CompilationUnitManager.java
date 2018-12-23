package org.jactr.io2.compilation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompilationUnitManager
{

  static public CompilationUnitManager get()
  {
    return _instance;
  }

  static private CompilationUnitManager  _instance  = new CompilationUnitManager();

  private List<ICompilationUnitProvider> _providers = new ArrayList<>();

  private CompilationUnitManager()
  {

  }

  public void addProvider(ICompilationUnitProvider provider)
  {
    _providers.add(provider);
  }

  public ICompilationUnit get(final URI resource) throws Exception
  {
    Optional<ICompilationUnitProvider> provider = _providers.stream()
        .filter((p) -> p.handles(resource)).findFirst();
    if (!provider.isPresent()) throw new RuntimeException(
        "Could not load " + resource + ". No provider available");
    return provider.get().get(resource);
  }
}
