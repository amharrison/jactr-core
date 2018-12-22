package org.commonreality.executor;

/*
 * default logging
 */

public class ThreadNamer implements Runnable
{

  private String                     _name;

  public ThreadNamer(String name)
  {
    _name = name;
  }

  public void run()
  {
    try
    {
      Thread.currentThread().setName(_name);
    }
    catch (SecurityException se)
    {

    }
  }

}
