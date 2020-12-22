package org.jactr.fluent.registry;

import java.util.Map;
import java.util.function.Consumer;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.module.asynch.IAsynchronousModule;
import org.jactr.fluent.FluentChunk;
import org.jactr.fluent.FluentChunkType;
import org.jactr.modules.pm.motor.AbstractMotorModule;
import org.jactr.modules.pm.motor.command.translators.AbstractManualTranslator;
import org.jactr.modules.pm.motor.six.DefaultMotorModule6;

public class DefaultMotorParticipant implements Consumer<IModel>
{

  @Override
  public void accept(IModel model)
  {
    try
    {
      IChunkType command = model.getDeclarativeModule().getChunkType("command")
          .get();

      IChunkType motorCommand = FluentChunkType.fromParent(command)
          .named("motor-command").slot("muscle").encode();

      FluentChunkType.fromParent(motorCommand).named("compound-motor-command")
          .slot("state").encode();

      IChunkType handCommand = FluentChunkType.fromParent(motorCommand)
          .named("hand-command").slot("hand").encode();
      IChunkType fingerCommand = FluentChunkType.fromParent(handCommand)
          .named("finger-command").slot("finger").encode();

      IChunkType constant = FluentChunkType.from(model).named("motor-constant")
          .encode();
      Map<String, IChunk> definedChunks = FluentChunk.from(constant).chunks(
          "right", "left", "index", "middle", "ring", "thumb", "pinkie",
          "mouse", "joystick1", "joystick2", "aborting");

      IChunkType peck = FluentChunkType.fromParent(fingerCommand).named("peck")
          .slots("distance", "theta").encode();
      FluentChunkType.fromParent(peck).named("peck-recoil").encode();

      FluentChunkType.fromParent(fingerCommand).named("punch").encode();
      FluentChunkType.fromParent(fingerCommand).named("press").encode();
      FluentChunkType.fromParent(fingerCommand).named("release").encode();
      FluentChunkType.fromParent(handCommand).named("point-hand-at-key")
          .slot("to-key").encode();
      FluentChunkType.fromParent(motorCommand).named("press-key").slot("key")
          .encode();
      FluentChunkType.fromParent(fingerCommand).named("click-mouse")
          .slot("finger", definedChunks.get("index"))
          .slot("hand", definedChunks.get("right")).encode();
      FluentChunkType.fromParent(handCommand).named("hand-to-mouse")
          .slot("hand", definedChunks.get("right")).encode();
      FluentChunkType.fromParent(handCommand).named("hand-to-home")
          .slot("hand", definedChunks.get("right")).encode();
      FluentChunkType.fromParent(motorCommand).named("move-cursor")
          .slots("object", "location", "device").encode();
      FluentChunkType
          .fromParent(model.getDeclarativeModule().getChunkType("clear").get())
          .named("motor-clear").slot("muscle").encode();

      /*
       * do some parameter setting since this also installs default handlers
       */
      DefaultMotorModule6 motorModule = (DefaultMotorModule6) model
          .getModule(DefaultMotorModule6.class);
      motorModule.setParameter(IAsynchronousModule.STRICT_SYNCHRONIZATION_PARAM,
          "true");
      motorModule.setParameter(
          AbstractMotorModule.ENABLE_PARALLEL_MUSCLES_PARAM, "false");
      motorModule.setParameter(AbstractManualTranslator.MINIMUM_FITTS_TIME,
          "0.1");
      motorModule.setParameter(AbstractManualTranslator.MINIMUM_MOVEMENT_TIME,
          "0.05");
      motorModule.setParameter(AbstractManualTranslator.PECK_FITTS_COEFFICIENT,
          "0.075");
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

}
