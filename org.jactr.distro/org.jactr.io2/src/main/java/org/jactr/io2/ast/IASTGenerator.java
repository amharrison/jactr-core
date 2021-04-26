package org.jactr.io2.ast;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.production.IProduction;

public interface IASTGenerator
{

  public boolean generates(String format);

  public Object generate(IModel model, String format, boolean trimIfPossible);

  public Object generate(IChunkType chunkType, String format,
      boolean includeChunks);

  public Object generate(IChunk chunk, String format);

  public Object generate(IProduction production, String format);

}
