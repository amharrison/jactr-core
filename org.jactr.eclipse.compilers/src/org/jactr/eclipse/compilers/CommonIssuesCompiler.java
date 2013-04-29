package org.jactr.eclipse.compilers;

/*
 * default logging
 */
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io.compiler.AbstractReportableUnitCompiler;

public class CommonIssuesCompiler extends AbstractReportableUnitCompiler 
{

  public CommonIssuesCompiler()
  {
    setRelevantTypes(JACTRBuilder.PRODUCTION, JACTRBuilder.MATCH_CONDITION);
  }

  @Override
  protected void compile(CommonTree node)
  {
    if (node.getType() == JACTRBuilder.MATCH_CONDITION) checkForIdentityMatch(node);
    else
      // production..
      checkForUnmatchedRemoval(node);
  }

  /**
   * tests lhs for the matching of a specific chunk in the buffer which will
   * likely fail
   */
  protected void checkForIdentityMatch(CommonTree node)
  {
    // second child is the content to match
    CommonTree content = (CommonTree) node.getChild(1);
    if (content.getType() == JACTRBuilder.CHUNK_IDENTIFIER
        || content.getType() == JACTRBuilder.VARIABLE)
      report("Matching to the specific chunk " + content.getText()
          + " will likely fail as most buffers copy on insertion", content);
  }

  /**
   * checking for a remove w/o query or match
   * 
   * @param node
   */
  protected void checkForUnmatchedRemoval(CommonTree node)
  {
    Map<String, CommonTree> removals = ASTSupport.getMapOfTrees(node,
        JACTRBuilder.REMOVE_ACTION);
    if (removals.size() == 0) return;

    Set<String> queries = ASTSupport.getMapOfTrees(node,
        JACTRBuilder.QUERY_CONDITION).keySet();
    Set<String> matches = ASTSupport.getMapOfTrees(node,
        JACTRBuilder.MATCH_CONDITION).keySet();

    for (Map.Entry<String, CommonTree> removal : removals.entrySet())
      if (!queries.contains(removal.getKey())
          && !matches.contains(removal.getKey()))
        report("Requesting removal from untested buffer " + removal.getKey()
            + " results in module specific behavior", removal.getValue());
  }

  public void setInitializationData(IConfigurationElement config,
      String propertyName, Object data) throws CoreException
  {

    
  }
}
