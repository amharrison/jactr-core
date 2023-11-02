package org.jactr.io2.tools.production;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.model.IModel;
import org.jactr.core.module.procedural.six.learning.IProceduralLearningModule6;
import org.jactr.core.module.procedural.six.learning.event.IProceduralLearningModule6Listener;
import org.jactr.core.module.procedural.six.learning.event.ProceduralLearningEvent;
import org.jactr.core.production.IProduction;
import org.jactr.instrument.IInstrument;
import org.jactr.io2.ast.ASTGeneratorManager;
import org.jactr.io2.ast.IASTGenerator;
import org.jactr.io2.source.ISourceGenerator;
import org.jactr.io2.source.SourceGeneratorManager;

public class ProductionCompilationListener implements IInstrument
{

  private IProceduralLearningModule6Listener _listener         = new IProceduralLearningModule6Listener() {

                                                                 @Override
                                                                 public void stopReward(
                                                                     ProceduralLearningEvent event)
                                                                 {

                                                                 }

                                                                 @Override
                                                                 public void startReward(
                                                                     ProceduralLearningEvent event)
                                                                 {

                                                                 }

                                                                 @Override
                                                                 public void rewarded(
                                                                     ProceduralLearningEvent event)
                                                                 {

                                                                 }

                                                                 @Override
                                                                 public void productionNotCompiled(
                                                                     ProceduralLearningEvent event)
                                                                 {
                                                                   notCompiled(
                                                                       event);
                                                                 }

                                                                 @Override
                                                                 public void productionCompiled(
                                                                     ProceduralLearningEvent event)
                                                                 {
                                                                   compiled(
                                                                       event);
                                                                 }
                                                               };

  private Map<IModel, PrintStream>           _writers          = new HashMap<>();

  private IASTGenerator                      _ast;

  private Map<IProduction, Set<IProduction>> _failedAndWritten = new HashMap<>();

  @Override
  public void install(IModel model)
  {
    IProceduralLearningModule6 procMod = (IProceduralLearningModule6) model
        .getModule(IProceduralLearningModule6.class);
    procMod.addListener(_listener,
        ExecutorServices.getExecutor(ExecutorServices.BACKGROUND));

    try
    {
      PrintStream ps = null;
      _writers.put(model, ps = new PrintStream(new FileOutputStream(
          new File(model.getName() + ".production.trace.xml"))));
      
      ps.println("<trace>");
      ps.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      _writers.put(model, System.err);
    }
  }

  @Override
  public void uninstall(IModel model)
  {
    _failedAndWritten.clear();
    _ast = null;

    IProceduralLearningModule6 procMod = (IProceduralLearningModule6) model
        .getModule(IProceduralLearningModule6.class);
    procMod.removeListener(_listener);

    PrintStream ps = _writers.remove(model);
    ps.println("</trace>");		
    ps.close();
  }

  @Override
  public void initialize()
  {
    _ast = ASTGeneratorManager.get().getASTGenerator("jactr").get();
  }

  protected void compiled(ProceduralLearningEvent event)
  {
    String format = "jactr";
    IProduction[] parents = event.getParents();
    IProduction child = event.getProduction();

    Object[] asts = new Object[3];
    asts[0] = _ast.generate(parents[0], format);
    asts[1] = _ast.generate(parents[1], format);
    asts[2] = _ast.generate(child, format);

    String[] codes = new String[3];
    for (int i = 0; i < 3; i++)
    {
      ISourceGenerator source = SourceGeneratorManager.get()
          .getSourceGenerator(null, format).get();
      codes[i] = source.generate(asts[i], format);
    }

    PrintStream ps = _writers.get(event.getSource().getModel());
    ps.println("<compiled cycle=\"" + event.getSource().getModel()
        .getProceduralModule().getNumberOfProductionsFired() + "\">");
    for (int i = 0; i < 2; i++)
    {
      ps.println("  <parent><![CDATA[");
      ps.println(codes[i]);
      ps.println("  ]]></parent>");
    }
    ps.println("  <child><![CDATA[");
    ps.println(codes[2]);
    ps.println("  ]]></child>");
    ps.println("</compiled>");

    ps.flush();
  }

  protected void notCompiled(ProceduralLearningEvent event)
  {
    IProduction[] parents = event.getParents();
    PrintStream ps = _writers.get(event.getSource().getModel());

    // we've already written, write short form
    if (_failedAndWritten.getOrDefault(parents[0], Collections.emptySet())
        .contains(parents[1]))
    {
      ps.println("<compile-failed cycle=\"" + event.getSource().getModel()
          .getProceduralModule().getNumberOfProductionsFired() + "\">");
      ps.println("  <message>");
      ps.println(event.getMessage());
      ps.println("  </message>");
      ps.println("</compile-failed>");
      return;
    }
    // otherwise, write the long form

    String format = "jactr";

    Object[] asts = new Object[2];
    asts[0] = _ast.generate(parents[0], format);
    asts[1] = _ast.generate(parents[1], format);

    String[] codes = new String[2];
    for (int i = 0; i < 2; i++)
    {
      ISourceGenerator source = SourceGeneratorManager.get()
          .getSourceGenerator(null, format).get();
      codes[i] = source.generate(asts[i], format);
    }

    ps.println("<compile-failed cycle=\"" + event.getSource().getModel()
        .getProceduralModule().getNumberOfProductionsFired() + "\">");
    ps.println("  <message>");
    ps.println(event.getMessage());
    ps.println("  </message>");
    for (int i = 0; i < 2; i++)
    {
      ps.println("  <parent><![CDATA[");
      ps.println(codes[i]);
      ps.println("  ]]></parent>");
    }
    ps.println("</compile-failed>");
    
    ps.flush();

    // add to the list of written
    _failedAndWritten.computeIfAbsent(parents[0], (k) -> {
      return new HashSet<>();
    }).add(parents[1]);
  }

}
