package org.jactr.io.io2.ast;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.production.IProduction;
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

  @Override
  public Object generate(IChunkType chunkType, String format,
      boolean includeChunks)
  {
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(format);
    if (codeGen == null) return null;
    return ASTResolver.toAST(chunkType, includeChunks);
  }

  @Override
  public Object generate(IChunk chunk, String format)
  {
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(format);
    if (codeGen == null) return null;
    return ASTResolver.toAST(chunk, false);
  }

  @Override
  public Object generate(IProduction production, String format)
  {
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(format);
    if (codeGen == null) return null;
    return ASTResolver.toAST(production);
  }

}
