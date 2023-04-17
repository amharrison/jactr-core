package org.jactr.io.io2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
/*
 * default logging
 */
import org.jactr.core.model.IModel;
import org.jactr.io.IOUtilities;
import org.jactr.io2.compilation.ICompilationUnit;
import org.slf4j.LoggerFactory;

public class CommonTreeCompilationUnit implements ICompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(CommonTreeCompilationUnit.class);

  private final URI                  _uri;

  private final CommonTree           _modelDescriptor;

  public CommonTreeCompilationUnit(URI uri, CommonTree modelDesc)
  {
    _uri = uri;
    _modelDescriptor = modelDesc;
  }

  @Override
  public URI getURI()
  {
    return _uri;
  }

  @Override
  public Object getAST()
  {
    return _modelDescriptor;
  }

  @Override
  public IModel build() throws Exception
  {
    Collection<Exception> warnings = new ArrayList<>();
    Collection<Exception> errors = new ArrayList<>();

    IOUtilities.compileModelDescriptor(_modelDescriptor, warnings, errors);
    if (errors.size() > 0) throw errors.iterator().next();
    warnings.clear();
    errors.clear();

    IModel model = IOUtilities.constructModel(_modelDescriptor, warnings,
        errors);
    if (errors.size() > 0) throw errors.iterator().next();

    return model;
  }

  @Override
  public Object getNormalizedAST()
  {
    return getAST();
  }

}
