package org.jactr.fluent.registry;

import java.util.Map;
import java.util.function.Consumer;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.model.IModel;
import org.jactr.fluent.FluentChunk;
import org.jactr.fluent.FluentChunkType;

public class DefaultMetaParticipant implements Consumer<IModel>
{

  public DefaultMetaParticipant()
  {

  }

  @Override
  public void accept(IModel t)
  {
    try
    {
      Map<String, IChunk> chunks = FluentChunk
          .from(t.getDeclarativeModule().getChunkType("chunk").get())
          .chunks("equals", "less-than", "less-than-equals", "greater-than",
              "greater-than-equals", "not-equals");

      FluentChunkType.from(t).slots("name", "value")
          .slot("condition", chunks.get("equals")).encode();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

}
