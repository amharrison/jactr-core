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
    Thread t = null;

    Runnable defensive = new Runnable() {
      public void run()
      {
        try
        {
          r.run();
        }
        catch (Throwable thrown)
        {
          LOGGER
              .error("Uncaught exception on " + Thread.currentThread().getName()
                  + ", while executing " + r + "(" + r.getClass().getName()
                  + ") : " + thrown.getMessage() + " ", thrown);
        }
      }
    };

    t = new Thread(defensive);

    t.setName(_nameTemplate + "-" + (++_count));

    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

      public void uncaughtException(Thread t, Throwable e)
      {
        LOGGER.error("Uncaught exception on " + t + " : ", e);
      }

    });

    return t;
  }

}
