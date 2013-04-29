package org.jactr.eclipse.ui.content;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ViewerComparator;
import org.jactr.io.antlr3.builder.JACTRBuilder;

/**
 * viewer comparator that is used to sort the content outline. we separate the
 * nodes based on their type, and then use the default lexical behavior.
 * 
 * @author harrison
 */
public class ACTRContentSorter extends ViewerComparator
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRContentSorter.class);

  public int category(Object element)
  {
    CommonTree node = (CommonTree) element;
    int rtn = node.getType();
    switch (node.getType())
    {
      case JACTRBuilder.PARAMETERS:
        rtn = 1000; //forces parameters to the rear
        break;
      case JACTRBuilder.BUFFERS :
        rtn = 0; //force buffers to the top
        break;
    }
    return rtn;
  }
}
