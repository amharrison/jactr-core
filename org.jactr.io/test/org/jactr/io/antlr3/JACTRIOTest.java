package org.jactr.io.antlr3;

import org.antlr.runtime.tree.CommonTree;
/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.io.CommonIO;
import org.junit.Test;

import junit.framework.TestCase;

public class JACTRIOTest extends TestCase
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(JACTRIOTest.class);

  @Test
  public void testSemantic() throws Exception
  {
    testModel("org/jactr/io/antlr3/semantic-full.jactrx");
  }

  @Test
  public void testCount() throws Exception
  {
    testModel("org/jactr/io/antlr3/count.jactrx");
  }

  @Test
  public void testAddition() throws Exception
  {
    testModel("org/jactr/io/antlr3/addition.jactrx");
  }

  protected void testModel(String modelFile) throws Exception
  {
    CommonTree modelDesc = CommonIO.parserTest(modelFile, true, true);
    CommonIO.compilerTest(modelDesc, true, true);
  }
}
