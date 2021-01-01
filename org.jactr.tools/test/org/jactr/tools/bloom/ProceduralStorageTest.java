package org.jactr.tools.bloom;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.jactr.core.model.IModel;
import org.jactr.core.production.IProduction;
import org.jactr.fluent.FluentAction;
import org.jactr.fluent.FluentCondition;
import org.jactr.fluent.FluentProduction;
import org.jactr.tools.async.FluentSemantic;
import org.junit.Test;

public class ProceduralStorageTest
{

  @Test
  public void test() throws InterruptedException, ExecutionException
  {
    IModel model = new FluentSemantic().get(new BFProductionStorage());

    IProduction originalFail = model.getProceduralModule().getProduction("fail")
        .get();

    IProduction production = FluentProduction.from(model).named("newFail")
        .condition(
            FluentCondition
                .match("goal",
                    model
                        .getDeclarativeModule().getChunkType("is-member").get())
                .slot("object", "=obj").slot("category", "=cat")
                .slot("judgement",
                    model.getDeclarativeModule().getChunk("pending").get())
                .build())
        .condition(FluentCondition.query("retrieval")
            .slot("state", model.getDeclarativeModule().getErrorChunk())
            .build())
        .action(FluentAction.modify("goal")
            .slot("judgement",
                model.getDeclarativeModule().getChunk("no").get())
            .build())
        .action(FluentAction.remove("retrieval").build()).encode();

    assertTrue(originalFail.equals(production));
  }

}
