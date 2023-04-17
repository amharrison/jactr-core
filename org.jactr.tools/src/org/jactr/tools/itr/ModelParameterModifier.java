package org.jactr.tools.itr;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io2.compilation.ICompilationUnit;

public class ModelParameterModifier extends AbstractParameterModifier
{
  

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
    ASTSupport support = new ASTSupport();
    support.setParameter(modelDescriptor, parameter, value, true);
  }

  
}
