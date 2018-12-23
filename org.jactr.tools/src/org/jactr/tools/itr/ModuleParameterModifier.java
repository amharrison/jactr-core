package org.jactr.tools.itr;

import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io2.compilation.ICompilationUnit;

public class ModuleParameterModifier extends AbstractParameterModifier
{

  static public final String MODULE_CLASS     = "ModuleClass";

  protected String           _moduleClassName = "";

  
  static public void setModuleParameter(CommonTree modelTree, String moduleClassName, String parameter, String value)
  {
    Collection<CommonTree> modules = ASTSupport.getAllDescendantsWithType(
        modelTree, JACTRBuilder.MODULE);

    for (CommonTree module : modules)
    {
      CommonTree classDesc = ASTSupport.getFirstDescendantWithType(module,
          JACTRBuilder.CLASS_SPEC);
      if (classDesc != null && moduleClassName.equals(classDesc.getText()))
      {
        ASTSupport support = new ASTSupport();
        support.setParameter(module, parameter, value, true);
      }
    }
  }
  

  @Override
  protected void setParameter(ICompilationUnit modelDescriptor,
      String parameter, String value)
  {
    if (modelDescriptor.getAST() instanceof CommonTree)
      setParameter((CommonTree) modelDescriptor.getAST(), parameter, value);
    else
      throw new RuntimeException("not implemented yet");
  }

  protected void setParameter(CommonTree modelDescriptor, String parameter,
      String value)
  {
    setModuleParameter(modelDescriptor, _moduleClassName, parameter,value);
  }

  @Override
  public Collection<String> getSetableParameters()
  {
    ArrayList<String> rtn = new ArrayList<String>(super.getSetableParameters());
    rtn.add(MODULE_CLASS);
    return rtn;
  }

  @Override
  public void setParameter(String key, String value)
  {
    if(MODULE_CLASS.equalsIgnoreCase(key))
      _moduleClassName = value;
    else super.setParameter(key, value);
  }
}
