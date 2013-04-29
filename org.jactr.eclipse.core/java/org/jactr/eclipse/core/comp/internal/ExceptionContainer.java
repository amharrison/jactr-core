package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.util.Collection;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExceptionContainer
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ExceptionContainer.class);

  final private FastList<Exception>  _info;

  final private FastList<Exception>  _warnings;

  final private FastList<Exception>  _errors;

  public ExceptionContainer()
  {
    _info = FastList.newInstance();
    _warnings = FastList.newInstance();
    _errors = FastList.newInstance();
  }

  protected void dispose()
  {
    FastList.recycle(_info);
    FastList.recycle(_warnings);
    FastList.recycle(_errors);
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
    if (container == null) container = FastList.newInstance();
    container.addAll(_info);
    return container;
  }

  public Collection<Exception> getWarnings(Collection<Exception> container)
  {
    if (container == null) container = FastList.newInstance();
    container.addAll(_warnings);
    return container;
  }

  public Collection<Exception> getErrors(Collection<Exception> container)
  {
    if (container == null) container = FastList.newInstance();
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
