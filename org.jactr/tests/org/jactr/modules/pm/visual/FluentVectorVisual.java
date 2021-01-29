package org.jactr.modules.pm.visual;

import java.util.Map;
import java.util.function.Supplier;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.fluent.FluentAction;
import org.jactr.fluent.FluentChunk;
import org.jactr.fluent.FluentChunkType;
import org.jactr.fluent.FluentCondition;
import org.jactr.fluent.FluentModel;
import org.jactr.fluent.FluentProduction;
import org.jactr.modules.pm.aural.six.DefaultAuralModule6;
import org.jactr.modules.pm.visual.six.DefaultVisualModule6;

public class FluentVectorVisual implements Supplier<IModel>
{

  public FluentVectorVisual()
  {
  }

  @Override
  public IModel get()
  {
    try
    {
      IModel model = FluentModel.named("test-visual").withCoreModules()
          .withPMModules().build();

      ((DefaultAuralModule6) model.getModule(DefaultAuralModule6.class))
          .setParameter("EnableBufferStuff", "false"); // turn off stuffing

      // reduces the logging noise
      ((DefaultVisualModule6) model.getModule(DefaultVisualModule6.class))
          .setParameter("EnableVisualBufferStuff", "false");

      IChunkType chunk = model.getDeclarativeModule().getChunkType("chunk")
          .get();
      IChunkType visLoc = model.getDeclarativeModule()
          .getChunkType("visual-location").get();

      model.getDeclarativeModule().getFreeChunk();
      IChunk error = model.getDeclarativeModule().getErrorChunk();

      Map<String, IChunk> constants = FluentChunk.from(chunk).chunks("start",
          "retrieve", "search", "match", "done");

      IChunkType goal = FluentChunkType.from(model).named("goal")
          .slot("stage", constants.get("start")).encode();
      IChunkType sequence = FluentChunkType.from(model)
          .slots("src", "dst", "expected", "next").slot("threshold", 0.5)
          .encode();

      FluentChunk.from(visLoc).named("center")
          .slot(IVisualModule.SCREEN_X_SLOT, 0)
          .slot(IVisualModule.SCREEN_Y_SLOT, 0).encode();
      IChunk upperLeft = FluentChunk.from(visLoc).named("upperLeft")
          .slot(IVisualModule.SCREEN_X_SLOT, -60)
          .slot(IVisualModule.SCREEN_Y_SLOT, 45).encode();
      IChunk lowerLeft = FluentChunk.from(visLoc).named("lowerLeft")
          .slot(IVisualModule.SCREEN_X_SLOT, -60)
          .slot(IVisualModule.SCREEN_Y_SLOT, -45).encode();
      IChunk upperRight = FluentChunk.from(visLoc).named("upperRight")
          .slot(IVisualModule.SCREEN_X_SLOT, 60)
          .slot(IVisualModule.SCREEN_Y_SLOT, 45).encode();
      IChunk lowerRight = FluentChunk.from(visLoc).named("lowerRight")
          .slot(IVisualModule.SCREEN_X_SLOT, 60)
          .slot(IVisualModule.SCREEN_Y_SLOT, -45).encode();
      FluentChunk.from(visLoc).slot(IVisualModule.SCREEN_X_SLOT, 0)
          .named("lowerMiddle").slot(IVisualModule.SCREEN_Y_SLOT, -45).encode();

      IChunk sequence8 = FluentChunk.from(sequence).slot("src", lowerLeft)
          .slot("dst", upperRight).slot("expected", null).slot("threshold", 0.2)
          .slot("next", null).encode();
      IChunk sequence7 = FluentChunk.from(sequence).slot("src", lowerRight)
          .slot("dst", upperLeft).slot("expected", null).slot("threshold", 0.2)
          .slot("next", sequence8).encode();
      IChunk sequence6 = FluentChunk.from(sequence).slot("src", upperRight)
          .slot("dst", lowerLeft).slot("expected", "center")
          .slot("threshold", 1).slot("next", sequence7).encode();
      IChunk sequence5 = FluentChunk.from(sequence).slot("src", upperLeft)
          .slot("dst", lowerRight).slot("expected", "center")
          .slot("threshold", 1)
          .slot("next", sequence6).encode();
      IChunk sequence4 = FluentChunk.from(sequence).slot("src", upperRight)
          .slot("dst", upperLeft).slot("expected", "upperMiddle")
          .slot("next", sequence5).encode();
      IChunk sequence3 = FluentChunk.from(sequence).slot("src", lowerRight)
          .slot("dst", upperRight).slot("expected", "middleRight")
          .slot("threshold", 1.1)
          .slot("next", sequence4).encode();
      IChunk sequence2 = FluentChunk.from(sequence).slot("src", lowerLeft)
          .slot("dst", lowerRight).slot("expected", "lowerMiddle")
          .slot("threshold", 0.6).slot("next", sequence3).encode();
      IChunk sequence1 = FluentChunk.from(sequence).slot("src", upperLeft)
          .slot("dst", lowerLeft).slot("expected", "middleLeft")
          .slot("next", sequence2).encode();

      FluentProduction.from(model).named("start")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("start")).build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("retrieve")).build())
          .action(FluentAction.add("retrieval", sequence1).build()).encode();

      FluentProduction.from(model).named("restart")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("done")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("next", "=next").build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("retrieve")).build())
          .action(FluentAction.add("retrieval", "=next").build()).encode();

      FluentProduction.from(model).named("completed")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("done")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("next", null).build())
          .action(FluentAction.remove("goal").build()).encode();

      FluentProduction.from(model).named("search-between")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("retrieve")).build())
          .condition(
              FluentCondition.match("retrieval", sequence).slot("src", "=src")
                  .slot("dst", "=dst").slot("threshold", "=threshold").build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("match")).build())
          .action(FluentAction.add("visual-location", visLoc)
              .slot(":vector-origin", "=src")
              .slot(":vector-destination", "=dst")
              .slot(":vector-threshold", "=threshold").build())
          .action(FluentAction.modify("retrieval").build()).encode();

      FluentProduction.from(model).named("found-match")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("match")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("expected", "=expected").build())
          .condition(FluentCondition.match("visual-location", visLoc)
              .slot("value", "=expected").build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("done")).build())
          .action(FluentAction.modify("retrieval").build())
          .action(FluentAction.remove("visual-location").build()).encode();

      FluentProduction.from(model).named("found-mismatch")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("match")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("expected", "=expected").build())
          .condition(FluentCondition.match("visual-location", visLoc)
              .slot("value").not("=expected").build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("done")).build())
          .action(FluentAction.modify("retrieval").build())
          .action(FluentAction.remove("visual-location").build()).encode();

      FluentProduction.from(model).named("not-found-match")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("match")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("expected", null).build())
          .condition(FluentCondition.query("visual-location")
              .slot("state", error).build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("done")).build())
          .action(FluentAction.modify("retrieval").build()).encode();

      FluentProduction.from(model).named("not-found-mismatch")
          .condition(FluentCondition.match("goal", goal)
              .slot("stage", constants.get("match")).build())
          .condition(FluentCondition.match("retrieval", sequence)
              .slot("expected", "=expected").build())
          .condition(FluentCondition.query("visual-location")
              .slot("state", error).build())
          .action(FluentAction.modify("goal")
              .slot("stage", constants.get("done")).build())
          .action(FluentAction.modify("retrieval").build()).encode();

      model.getActivationBuffer("goal")
          .addSourceChunk(FluentChunk.from(goal).build());

      model.initialize();
      return model;
    }
    catch (Exception e)
    {

      throw new RuntimeException(e);
    }
  }

}
