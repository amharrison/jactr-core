package org.jactr.core.module.meta;

import java.util.Collection;
import java.util.List;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.logging.impl.DefaultModelLogger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.IInstantiation;
import org.jactr.core.reality.connector.LocalConnector;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.runtime.controller.DefaultController;
import org.jactr.core.runtime.controller.IController;
import org.jactr.core.utils.StringUtilities;
import org.jactr.test.ExecutionTester;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class MetaTests
{

  static private final org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(MetaTests.class);

  @Test
  public void test() throws Throwable
  {
    MetaModelProvider mmp = new MetaModelProvider();
    IModel model = mmp.get();

    ExecutionTester tester = setup(model, mmp.getExpectedSequence(),
        mmp.getFailureProductions(), true);
    run(model);
    cleanup(tester, model, false);

    for (Throwable thrown : tester.getExceptions())
      throw thrown;
  }

  protected ExecutionTester setup(IModel model, List<String> validSequence,
      Collection<String> failedProductions, boolean fullLogging)
  {
    ExecutionTester tester = new ExecutionTester() {

      @Override
      public void verifyModelState(IModel model, IInstantiation instantiation)
      {
        if (LOGGER.isDebugEnabled())
        {
          IActivationBuffer buffer = model
              .getActivationBuffer(IActivationBuffer.GOAL);
          LOGGER.debug("Goal buffer contents");
          for (IChunk chunk : buffer.getSourceChunks())
            LOGGER.debug("\t" + StringUtilities.toString(chunk) + "\n");
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Instantiation : " + instantiation.getProduction()
                .getSymbolicProduction().getName());
        }
      }
    };

    tester.setProductionSequence(validSequence);
    tester.setFailedProductions(failedProductions);
    model.install(tester);

    if (fullLogging)
    {
      DefaultModelLogger dml = new DefaultModelLogger();
      dml.setParameter("all", "err");
      model.install(dml);
    }

    return tester;
  }

  protected void run(IModel model) throws Exception
  {
    ACTRRuntime runtime = ACTRRuntime.getRuntime();
    runtime.setConnector(new LocalConnector());
    runtime.setController(new DefaultController());

    runtime.addModel(model);

    IController controller = runtime.getController();

    controller.start().get();
    controller.waitForCompletion().get();

    runtime.removeModel(model);
  }

  protected void cleanup(ExecutionTester tester, IModel model, boolean dispose)
  {
    model.uninstall(tester);
    if (dispose) model.dispose();
  }
}
