package org.jactr.core.logging.impl;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.logging.IMessageBuilder;

public class StringMessageBuilder implements IMessageBuilder
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(StringMessageBuilder.class);

  private StringBuilder              _internal = new StringBuilder();

  public StringMessageBuilder()
  {
  }

  @Override
  public IMessageBuilder clear()
  {
    _internal.delete(0, _internal.length());
    return this;
  }

  @Override
  public IMessageBuilder append(String str)
  {
    _internal.append(str);
    return this;
  }

  @Override
  public IMessageBuilder prepend(String str)
  {
    _internal.insert(0, str);
    return this;
  }

  @Override
  public String toString()
  {
    return _internal.toString();
  }

  @Override
  public IMessageBuilder append(Object obj)
  {
    _internal.append(obj);
    return this;
  }

  @Override
  public IMessageBuilder prepend(Object obj)
  {
    _internal.insert(0, obj);
    return this;
  }
}
