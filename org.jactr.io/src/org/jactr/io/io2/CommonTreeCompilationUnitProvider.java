package org.jactr.io.io2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.IOUtilities;
import org.jactr.io.parser.ModelParserFactory;
import org.jactr.io2.compilation.ICompilationUnit;
import org.jactr.io2.compilation.ICompilationUnitProvider;

public class CommonTreeCompilationUnitProvider
    implements ICompilationUnitProvider
{

  private final Set<String> _validExtensions;

  public CommonTreeCompilationUnitProvider()
  {
    _validExtensions = new TreeSet<>(ModelParserFactory.getValidExtensions());
  }

  @Override
  public boolean handles(URI modelToLoad)
  {
    String extension = modelToLoad.getPath();
    extension = extension.substring(extension.lastIndexOf('.') + 1,
        extension.length());

    return _validExtensions.contains(extension);
  }

  @Override
  public ICompilationUnit get(URI modelToLoad) throws Exception
  {
    // TODO Auto-generated method stub
    Collection<Exception> warnings = new ArrayList<>();
    Collection<Exception> errors = new ArrayList<>();
    CommonTree modelDesc = IOUtilities.loadModelFile(modelToLoad.toURL(),
        warnings, errors);

    if (errors.size() > 0) throw errors.iterator().next();

    return new CommonTreeCompilationUnit(modelToLoad, modelDesc);

  }

}
