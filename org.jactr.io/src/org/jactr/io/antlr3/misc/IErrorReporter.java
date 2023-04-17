package org.jactr.io.antlr3.misc;

/*
 * default logging
 */
import org.antlr.runtime.RecognitionException;
 
import org.slf4j.LoggerFactory;

public interface IErrorReporter
{

  public void reportError(Exception exception);
  public void reportError(String message, RecognitionException exception);
}
