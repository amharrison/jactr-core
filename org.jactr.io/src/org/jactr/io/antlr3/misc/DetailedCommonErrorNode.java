package org.jactr.io.antlr3.misc;

/*
 * default logging
 */
import java.net.URL;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
 
import org.slf4j.LoggerFactory;

public class DetailedCommonErrorNode extends DetailedCommonTree
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DetailedCommonErrorNode.class);

  public DetailedCommonErrorNode(CommonTree arg0, URL source)
  {
    super(arg0, source);
  }

  public DetailedCommonErrorNode(Token arg0, URL source)
  {
    super(arg0, source);
  }

}
