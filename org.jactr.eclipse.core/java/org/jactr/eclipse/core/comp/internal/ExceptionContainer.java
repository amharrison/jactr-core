package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.collections.impl.factory.Lists;

public class ExceptionContainer
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ExceptionContainer.class);

  final private List<Exception>      _info;

  final private List<Exception>      _warnings;

  final private List<Exception>      _errors;

  public ExceptionContainer()
  {
    _info = Lists.mutable.empty();

    _warnings = Lists.mutable.empty();
    _errors = Lists.mutable.empty();
  }

  protected void dispose()
  {
    _info.clear();
    _warnings.clear();
    _errors.clear();
  }
  
  /**
   * true if there are no errors.
   * 
   * @return
   */
  public boolean isClean()
  {
    return _errors.size() == 0;
  }

  public Collection<Exception> getInfo(Collection<Exception> container)
  {
    if (container == null) container = Lists.mutable.empty();
    container.addAll(_info);
    return container;
  }

  public Collection<Exception> getWarnings(Collection<Exception> container)
  {
    if (container == null) container = Lists.mutable.empty();
    container.addAll(_warnings);
    return container;
  }

  public Collection<Exception> getErrors(Collection<Exception> container)
  {
    if (container == null) container = Lists.mutable.empty();
    container.addAll(_errors);
    return container;
  }

  public void addInfo(Collection<Exception> info)
  {
    _info.addAll(info);
  }

  public void addWarnings(Collection<Exception> warnings)
  {
    _warnings.addAll(warnings);
  }

  public void addErrors(Collection<Exception> errors)
  {
    _errors.addAll(errors);
  }
  
  public void clear()
  {
    _info.clear();
    _warnings.clear();
    _errors.clear();
  }

  public void clearInfo()
  {
    _info.clear();
  }

  public void clearWarnings()
  {
    _warnings.clear();
  }

  public void clearErrors()
  {
    _errors.clear();
  }
}
