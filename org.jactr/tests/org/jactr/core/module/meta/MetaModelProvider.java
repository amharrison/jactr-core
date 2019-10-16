package org.jactr.core.module.meta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.fluent.FluentAction;
import org.jactr.fluent.FluentChunk;
import org.jactr.fluent.FluentChunkType;
import org.jactr.fluent.FluentCondition;
import org.jactr.fluent.FluentModel;
import org.jactr.fluent.FluentProduction;

public class MetaModelProvider implements Supplier<IModel>
{

  public MetaModelProvider()
  {

  }

  public List<String> getExpectedSequence()
  {
    return Arrays.asList(new String[] { "01-start", "02-test-empty-spec",
        "03-test-partial-spec" });
  }

  public Collection<String> getFailureProductions()
  {
    return Arrays.asList(new String[] { "04-should-not-fire" });
  }

  @Override
  public IModel get()
  {
    try
    {
      IModel model = FluentModel.named("meta").withCoreModules()
          .with(MetaModule.class).build();

      IChunkType goalType = FluentChunkType.from(model).named("goal")
          .slot("step").slot("value").encode();
      IChunkType referenceType = FluentChunkType.from(model).named("reference")
          .slot("one").slot("two").encode();

      IChunk empty = model.getDeclarativeModule().getEmptyChunk();
      IChunk goal = FluentChunk.from(goalType).slot("step", 1).slot("value", 2)
          .build();
      IChunk imaginal = FluentChunk.from(referenceType).slot("one", 1)
          .slot("two", 2).build();
      /*
       * first production merely starts building the reference pattern
       */
      FluentProduction.from(model).named("01-start")
          .condition(
              FluentCondition.match("goal", goalType).slot("step", 1).build())
          .condition(
              FluentCondition.query("meta").slot("buffer", empty).build())
          .action(FluentAction.add("meta", referenceType).build())
          .action(FluentAction.modify("goal").slot("step", 2).build()).encode();

      /*
       * second production tests the limited spec, and adds a two != 3 test
       */
      FluentProduction.from(model).named("02-test-empty-spec")
          .condition(
              FluentCondition.match("goal", goalType).slot("step", 2).build())
          .condition(FluentCondition.matchNoSlots("meta", "=meta").build())
          .condition(FluentCondition.matchNoSlots("imaginal", "=meta").build())
          .action(FluentAction.add("meta", (IChunkType) null).slot("two").not(3)
              .build())
          .action(FluentAction.modify("goal").slot("step", 3).build()).encode();

      /*
       * third tests a single slot spec, and adds a to-fail feature to the spec
       */
      FluentProduction.from(model).named("03-test-partial-spec")
          .condition(
              FluentCondition.match("goal", goalType).slot("step", 3).build())
          .condition(FluentCondition.matchNoSlots("meta", "=meta").build())
          .condition(FluentCondition.matchNoSlots("imaginal", "=meta").build())
          .action(FluentAction.add("meta", (IChunkType) null).slot("two").not(2)
              .build())
          .action(FluentAction.modify("goal").slot("step", 4).build()).encode();

      FluentProduction.from(model).named("04-should-not-fire")
          .condition(
              FluentCondition.match("goal", goalType).slot("step", 4).build())
          .condition(FluentCondition.matchNoSlots("meta", "=meta").build())
          .condition(FluentCondition.matchNoSlots("imaginal", "=meta").build())
          .action(FluentAction.modify("goal").slot("step", 5).build()).encode();

      model.getActivationBuffer("goal").addSourceChunk(goal);
      model.getActivationBuffer(IActivationBuffer.IMAGINAL)
          .addSourceChunk(imaginal);

      model.initialize();

      return model;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

}
