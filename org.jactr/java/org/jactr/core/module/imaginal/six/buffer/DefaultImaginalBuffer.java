package org.jactr.core.module.imaginal.six.buffer;

import org.jactr.core.buffer.delegate.DefaultDelegatedRequestableBuffer6;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.logging.Logger;
import org.jactr.core.module.IModule;
import org.jactr.core.module.imaginal.IImaginalModule;
import org.jactr.core.module.procedural.five.learning.ICompilableBuffer;
import org.jactr.core.module.procedural.five.learning.ICompilableContext;
import org.jactr.core.module.procedural.six.learning.DefaultCompilableContext;

/*
 * default logging
 */

import org.slf4j.LoggerFactory;

public class DefaultImaginalBuffer extends DefaultDelegatedRequestableBuffer6
    implements ICompilableBuffer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultImaginalBuffer.class);

  public DefaultImaginalBuffer(IModule module)
  {
    super(IImaginalModule.IMAGINAL_BUFFER, module);
    setStrictHarvestingEnabled(false);

    addRequestDelegate(new ImaginalAddChunkRequestDelegate());
    addRequestDelegate(new ImaginalAddChunkTypeRequestDelegate());
    addRequestDelegate(new ImaginalSlotRequestDelegate());
  }

  @Override
  protected void setSourceChunkInternal(IChunk sourceChunk)
  {
    if (sourceChunk != null && Logger.hasLoggers(getModel()))
      Logger.log(getModel(), IImaginalModule.IMAGINAL_LOG, "Imagining "
          + sourceChunk);
    
    super.setSourceChunkInternal(sourceChunk);
  }

  public ICompilableContext getCompilableContext()
  {
    return new DefaultCompilableContext(false, true, true, false, false, false);
  }
}
