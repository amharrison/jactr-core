package org.jactr.core.concurrent;

import java.util.concurrent.ThreadFactory;

import org.slf4j.LoggerFactory;

public class GeneralThreadFactory implements ThreadFactory
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(GeneralThreadFactory.class);

  private int                                     _count = 0;

  private String                                  _nameTemplate;

  public GeneralThreadFactory(String nameTemplate)
  {
    this(nameTemplate, null);
  }

  public GeneralThreadFactory(String nameTemplate, ThreadGroup parentGroup)
  {
    _nameTemplate = nameTemplate;
  }

  /**
   * destroy the thread group, if any exists
   */
  public void dispose()
  {

  }

  public Thread newThread(final Runnable r)
  {
    Thread t = Thread.ofVirtual().name(_nameTemplate + "-" + (++_count))
        .uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

      public void uncaughtException(Thread t, Throwable e)
      {
        LOGGER.error("Uncaught exception on " + t + " : ", e);
      }

        }).unstarted(r);
    return t;
  }

}
