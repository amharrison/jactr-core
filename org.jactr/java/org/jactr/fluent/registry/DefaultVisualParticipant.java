package org.jactr.fluent.registry;

import java.awt.Color;
import java.util.function.Consumer;

import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.fluent.FluentChunk;
import org.jactr.fluent.FluentChunkType;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class DefaultVisualParticipant implements Consumer<IModel>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(DefaultVisualParticipant.class);

  @Override
  public void accept(IModel model)
  {
    IChunkType color = FluentChunkType.from(model).named("color").slot("red")
        .slot("green").slot("blue").slot("alpha").encode();

    createColorChunks(color);

    IChunkType visLoc = FluentChunkType.from(model).named("visual-location")
        .slots("screen-x", "screen-y", "distance", "color", "size", "kind",
            "value")
        .encode();

    FluentChunkType.fromParent(visLoc).named("set-default-visual-search");

    IChunkType visObj = FluentChunkType.from(model).named("visual-object")
        .slots("screen-pos", "value", "height", "width", "token", "type",
            "status", "color")
        .encode();

    IChunkType gui = FluentChunkType.fromParent(visObj).named("gui")
        .slots("text", "enabled")
        .encode();

    FluentChunkType.fromParent(gui).named("button-object").encode();
    FluentChunkType.fromParent(gui).named("label-object").encode();
    FluentChunkType.fromParent(gui).named("menu-object").encode();
    FluentChunkType.fromParent(visObj).named("text").encode();
    FluentChunkType.fromParent(visObj).named("empty-space").encode();
    FluentChunkType.fromParent(visObj).named("cursor").encode();
    FluentChunkType.fromParent(visObj).named("oval").encode();
    FluentChunkType.fromParent(visObj).named("phrase").slots("objects", "words")
        .encode();
    FluentChunkType.fromParent(visObj).named("line")
        .slots("other-pos", "end1-x", "end1-y", "end2-x", "end2-y").encode();

    try
    {
      IChunkType visCom = FluentChunkType.from(model)
          .childOf(model.getDeclarativeModule().getChunkType("command").get())
          .named("vision-command").encode();

      FluentChunkType.fromParent(visCom).named("move-attention")
          .slots("screen-pos", "scale").encode();

      FluentChunkType.fromParent(visCom).named("start-tracking").encode();

      FluentChunkType.fromParent(visCom).named("assign-finst")
          .slots("object", "location").encode();

    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    IChunkType visConstant = FluentChunkType.from(model)
        .named("visual-constant").encode();

    FluentChunk.from(visConstant).chunks("greater-than-current",
        "less-than-current", "current", "internal", "external");

  }

  private void createColorChunks(IChunkType colorType)
  {
    String[] colorNames = { "black", "darkGray", "gray", "lightGray", "white",
        "red", "blue", "green", "yellow", "orange", "magenta", "cyan" };

    Color[] colors = { Color.BLACK, Color.DARK_GRAY, Color.GRAY,
        Color.LIGHT_GRAY, Color.WHITE, Color.RED, Color.BLUE, Color.green,
        Color.YELLOW, Color.orange, Color.MAGENTA, Color.CYAN };

    for (int i = 0; i < colorNames.length; i++)
    {
      Color color = colors[i];
      FluentChunk.from(colorType).named(colorNames[i])
          .slot("red", color.getRed()).slot("green", color.getGreen())
          .slot("blue", color.getBlue()).slot("alpha", 255).encode();
    }
  }

}
