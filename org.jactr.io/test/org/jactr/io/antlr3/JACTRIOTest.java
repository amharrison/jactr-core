package org.jactr.io.antlr3;

import org.antlr.runtime.tree.CommonTree;
/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.io.CommonIO;
import org.junit.Test;

import junit.framework.TestCase;

public class JACTRIOTest extends TestCase
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
      .getLog(JACTRIOTest.class);

  @Test
  public void testSemantic() throws Exception
  {
    testModel("org/jactr/io/antlr3/semantic-full.jactr");
  }

  @Test
  public void testCount() throws Exception
  {
    testModel("org/jactr/io/antlr3/count.jactr");
  }

  @Test
  public void testAddition() throws Exception
  {
    testModel("org/jactr/io/antlr3/addition.jactr");
  }

  protected void testModel(String modelFile) throws Exception
  {
    CommonTree modelDesc = CommonIO.parserTest(modelFile, true, true);
    CommonIO.compilerTest(modelDesc, true, true);
  }
}
