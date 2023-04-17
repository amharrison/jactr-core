package org.jactr.io.io2.source;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;
import org.jactr.io2.source.ISourceGenerator;

public class CommonTreeSourceGenerator implements ISourceGenerator
{

  public CommonTreeSourceGenerator()
  {
  }

  @Override
  public boolean canGenerate(Object astNode, String format)
  {
    boolean isCommonTree = astNode == null || astNode instanceof CommonTree;
    if (!isCommonTree) return false;

    // now check the installed generators..
    return CodeGeneratorFactory.getExtensions().contains(format);
  }

  @Override
  public boolean canSave(Object astNode, URI resource)
  {
    boolean isCommonTree = astNode instanceof CommonTree;
    if (!isCommonTree) return false;

    File fp = new File(resource);
    String str = fp.getName();
    str = str.substring(str.lastIndexOf('.'), str.length());
    // now check the installed generators..
    return CodeGeneratorFactory.getExtensions().contains(str);
  }

  @Override
  public String generate(Object astNode, String format)
  {
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(format);
    if (codeGen == null) return null;

    return codeGen.generate((CommonTree) astNode, false).stream()
        .map(Object::toString).collect(Collectors.joining());
  }

  @Override
  public void save(Object astNode, URI resource)
      throws IOException
  {
    File fp = new File(resource);
    String str = fp.getName();
    str = str.substring(str.lastIndexOf('.'), str.length());
    ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(str);
    if (codeGen != null)
    {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fp)));
      for (StringBuilder sb : codeGen
          .generate((CommonTree) astNode, false))
        pw.print(sb.toString());
      pw.flush();
      pw.close();
    }
  }

}
