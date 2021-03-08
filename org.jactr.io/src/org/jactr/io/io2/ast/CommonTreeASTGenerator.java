package org.jactr.io.io2.ast;

import org.jactr.core.model.IModel;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;
import org.jactr.io.resolver.ASTResolver;
import org.jactr.io2.ast.IASTGenerator;

public class CommonTreeASTGenerator implements IASTGenerator
{

  public CommonTreeASTGenerator()
  {
  }

  @Override
  public boolean generates(String format)
  {
    return CodeGeneratorFactory.getExtensions().contains(format);
  }

  @Override
  public Object generate(IModel model, String format, boolean trimIfPossible)
  {
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(format);
    if (codeGen == null) return null;

    return ASTResolver.toAST(model, trimIfPossible);
  }

}
