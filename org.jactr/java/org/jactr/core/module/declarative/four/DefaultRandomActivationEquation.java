package org.jactr.core.module.declarative.four;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.basic.AbstractSubsymbolicChunk;
import org.jactr.core.model.IModel;
import org.jactr.core.module.random.IRandomModule;

public class DefaultRandomActivationEquation implements
    IRandomActivationEquation
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultRandomActivationEquation.class);

  private IRandomModule              _randomModule;

  private IDeclarativeModule4        _declarativeModule;

  public DefaultRandomActivationEquation(IRandomModule random,
      IDeclarativeModule4 decM)
  {
    _randomModule = random;
    _declarativeModule = decM;
  }

  public double computeRandomActivation(IModel model, IChunk c)
  {
    if (_randomModule == null) return 0;
    return _randomModule.logisticNoise(_declarativeModule.getActivationNoise());
  }

  @Override
  public String getName()
  {
    return "random";
  }

  @Override
  public double computeAndSetActivation(IChunk chunk, IModel model)
  {
    double random = computeRandomActivation(model, chunk);
    ((AbstractSubsymbolicChunk) chunk.getSubsymbolicChunk())
        .setRandomActivation(random);
    return random;
  }

}
