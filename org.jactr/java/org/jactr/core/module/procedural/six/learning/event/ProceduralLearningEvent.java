package org.jactr.core.module.procedural.six.learning.event;

/*
 * default logging
 */
import org.jactr.core.event.AbstractACTREvent;
import org.jactr.core.module.procedural.six.learning.IProceduralLearningModule6;
import org.jactr.core.production.IProduction;
import org.jactr.core.runtime.ACTRRuntime;

public class ProceduralLearningEvent
    extends
    AbstractACTREvent<IProceduralLearningModule6, IProceduralLearningModule6Listener>
{

  static public enum Type {
    START_REWARDING, REWARDED, END_REWARDING
  };

  final private Type        _type;

  final private double      _reward;

  final private IProduction _production;

  final private IProduction _parentA, _parentB;

  public ProceduralLearningEvent(IProceduralLearningModule6 source, Type type,
      double reward)
  {
    super(source, ACTRRuntime.getRuntime().getClock(source.getModel())
        .getTime());
    _type = type;
    _production = null;
    _reward = reward;
    _parentA = _parentB = null;
  }

  public ProceduralLearningEvent(IProceduralLearningModule6 source,
      IProduction production, double reward)
  {
    super(source, ACTRRuntime.getRuntime().getClock(source.getModel())
        .getTime());
    _type = Type.REWARDED;
    _production = production;
    _reward = reward;
    _parentA = _parentB = null;
  }

  public ProceduralLearningEvent(IProceduralLearningModule6 source,
      IProduction parentA, IProduction parentB, IProduction production)
  {
    super(source,
        ACTRRuntime.getRuntime().getClock(source.getModel()).getTime());
    _type = Type.REWARDED;
    _production = production;
    _reward = Double.NaN;
    _parentA = parentA;
    _parentB = parentB;
  }

  @Override
  public void fire(IProceduralLearningModule6Listener listener)
  {
    switch (getType())
    {
      case REWARDED:
        listener.rewarded(this);
        break;
      case START_REWARDING:
        listener.startReward(this);
        break;
      case END_REWARDING:
        listener.stopReward(this);
        break;
    }
  }

  public Type getType()
  {
    return _type;
  }

  public IProduction getProduction()
  {
    return _production;
  }

  public IProduction[] getParents()
  {
    if (_parentA != null) return new IProduction[] { _parentA, _parentB };
    return new IProduction[0];
  }

  public double getReward()
  {
    return _reward;
  }
}
