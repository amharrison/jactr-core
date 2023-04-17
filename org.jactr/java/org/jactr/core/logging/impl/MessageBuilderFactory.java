package org.jactr.core.logging.impl;

import org.jactr.core.logging.IMessageBuilder;
import org.jactr.core.utils.recyclable.AbstractThreadLocalRecyclableFactory;
import org.jactr.core.utils.recyclable.RecyclableFactory;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class MessageBuilderFactory
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger   LOGGER   = LoggerFactory
                                                              .getLogger(MessageBuilderFactory.class);

  static private RecyclableFactory<IMessageBuilder> _factory = new AbstractThreadLocalRecyclableFactory<IMessageBuilder>() {

                                                            @SuppressWarnings({
      "unchecked", "rawtypes"                              })
                                                            @Override
                                                            protected void cleanUp(
                                                                   IMessageBuilder obj)
                                                            {
                                                                obj.clear();
                                                            }

                                                            @Override
                                                               protected IMessageBuilder instantiate(
                                                                Object... params)
                                                            {
                                                                 return new StringMessageBuilder();
                                                            }

                                                              @Override
                                                              protected void release(
                                                                   IMessageBuilder obj)
                                                              {

                                                              }

                                                          };

  static public IMessageBuilder newInstance()
  {
    return _factory.newInstance();
  }

  static public void recycle(IMessageBuilder set)
  {
    _factory.recycle(set);
  }
}
