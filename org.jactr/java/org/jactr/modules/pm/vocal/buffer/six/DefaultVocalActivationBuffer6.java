package org.jactr.modules.pm.vocal.buffer.six;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.AddChunkRequestDelegate;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.declarative.IDeclarativeModule;
import org.jactr.core.module.procedural.five.learning.ICompilableContext;
import org.jactr.core.module.procedural.six.learning.DefaultCompilableContext;
import org.jactr.modules.pm.common.buffer.AbstractPMActivationBuffer6;
import org.jactr.modules.pm.vocal.AbstractVocalModule;
import org.jactr.modules.pm.vocal.IVocalModule;
import org.jactr.modules.pm.vocal.buffer.IVocalActivationBuffer;
import org.jactr.modules.pm.vocal.buffer.processor.ClearRequestDelegate;
import org.jactr.modules.pm.vocal.buffer.processor.SpeechRequestDelegate;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class DefaultVocalActivationBuffer6 extends AbstractPMActivationBuffer6 implements IVocalActivationBuffer
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultVocalActivationBuffer6.class);

  public DefaultVocalActivationBuffer6(AbstractVocalModule module)
  {
    super(IActivationBuffer.VOCAL, module);
  }

  @Override
  public void initialize()
  {
    super.initialize();
  }

  @Override
  protected void grabReferences()
  {
    IDeclarativeModule dm = getModel().getDeclarativeModule();

    try
    {
      // handles imagined voices
      addRequestDelegate(new AddChunkRequestDelegate());
      
      /*
       * to support clear..
       */
      addRequestDelegate(new ClearRequestDelegate(dm.getChunkType(IVocalModule.CLEAR_CHUNK_TYPE).get()));
      
      AbstractVocalModule module = (AbstractVocalModule) getModule();
      
      addRequestDelegate(new SpeechRequestDelegate(module, module.getSpeakChunkType()));
      addRequestDelegate(
          new SpeechRequestDelegate(module, module.getSubvocalizeChunkType()));
    }
    catch (Exception e)
    {

    }
    super.grabReferences();
  }

  @Override
  protected boolean isValidChunkType(IChunkType chunkType)
  {
    return false;
  }

  @Override
  public ICompilableContext getCompilableContext()
  {
    return new DefaultCompilableContext(false, false, true, false, true, false);
  }

}
