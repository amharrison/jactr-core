package org.jactr.tools.grapher.core.probe;

/*
 * default logging
 */
import java.util.concurrent.Executor;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.event.IParameterEvent;
import org.jactr.core.event.IParameterListener;
import org.jactr.core.utils.parameter.IParameterized;

public class ChunkProbe extends AbstractParameterizedProbe<IChunk>
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkProbe.class);
  
  private IParameterListener         _listener;

  public ChunkProbe(String name, IChunk parameterized)
  {
    super(name, parameterized);
  }
  
  @Override
  public void install(IChunk parameterized, Executor executor)
  {
    if (isPolling()) return;

    _listener = new IParameterListener() {

      public void parameterChanged(IParameterEvent pe)
      {
        set(pe.getParameterName(), pe.getNewParameterValue());
      }
    };

    parameterized.addListener(_listener, executor);
  }

  @Override
  protected AbstractParameterizedProbe<IChunk> newInstance(
      IChunk parameterized)
  {
    return new ChunkProbe(parameterized.getSymbolicChunk().getName(),
        parameterized);
  }

  @Override
  protected IParameterized asParameterized(IChunk parameterizedObject)
  {
    return parameterizedObject.getSubsymbolicChunk();
  }
}
