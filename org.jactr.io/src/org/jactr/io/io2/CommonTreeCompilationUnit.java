package org.jactr.io.io2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.model.IModel;
import org.jactr.io.IOUtilities;
import org.jactr.io2.compilation.ICompilationUnit;

public class CommonTreeCompilationUnit implements ICompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
      .getLog(CommonTreeCompilationUnit.class);

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

}
