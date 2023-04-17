package org.jactr.io.cl;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

/**
 * class loader that first uses its own class loader (and probably the
 * Eclipse-Buddy mechanism) to resolve a class. If that fails, it uses the
 * passed in class loader.
 * 
 * @author harrison
 */
public class Loader
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Loader.class);

  static public Class<?> getClass(String className, ClassLoader optionalLoader)
      throws ClassNotFoundException
  {
    try
    {
      return Loader.class.getClassLoader().loadClass(className);
    }
    catch (ClassNotFoundException cnfe)
    {
      if (optionalLoader == null) throw cnfe;

      return optionalLoader.loadClass(className);
    }
  }

}
