package org.jactr.scripting.javascript;

import javax.script.ScriptContext;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.scripting.IScriptableFactory;
import org.jactr.scripting.action.IActionScript;
import org.jactr.scripting.condition.IConditionScript;

public class JavascriptFactory implements IScriptableFactory
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(JavascriptFactory.class);

  public IActionScript createActionScript(String script) throws Exception
  {
    return new JavascriptAction(script, this);
  }

  public IConditionScript createConditionScript(String script) throws Exception
  {
    return new JavascriptCondition(script, this);
  }

  public String getDescription()
  {
    return "Nashorn Javascript";
  }

  public String getLanguageName()
  {
    return "javascript";
  }

  public Object getGlobalContext()
  {
    return ScopeManager.getPublicScope();
  }

  public Object getVariable(String label, Object variableContext)
  {
    return ScopeManager.getVariable((ScriptContext) variableContext, label);
  }

  public void setVariable(String label, Object value, Object variableContext)
  {
    ScopeManager.defineVariable((ScriptContext) variableContext, label, value);
  }

  public boolean supports(String scriptName)
  {
    return scriptName.equalsIgnoreCase("javascript")
        || scriptName.equalsIgnoreCase("js");
  }

}
