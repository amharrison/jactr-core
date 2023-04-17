package org.jactr.modules.pm.common.memory.map;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

 
import org.slf4j.LoggerFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.event.AbstractACTREvent;

public class FeatureMapEvent extends
    AbstractACTREvent<IFeatureMap, IFeatureMapListener>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(FeatureMapEvent.class);

  static public enum Type {
    ADDED, REMOVED, UPDATED
  };

  private final Type             _type;

  private final Set<IIdentifier> _identifiers;

  public FeatureMapEvent(IFeatureMap source, double simulationTime, Type type,
      Collection<IIdentifier> identifiers)
  {
    super(source, simulationTime);
    _type = type;
    _identifiers = Collections.unmodifiableSet(new HashSet<IIdentifier>(
        identifiers));
  }

  public Type getType()
  {
    return _type;
  }

  public Set<IIdentifier> getIdentifiers()
  {
    return _identifiers;
  }

  @Override
  public void fire(IFeatureMapListener listener)
  {
    switch (getType())
    {
      case ADDED:
        listener.featureAdded(this);
        break;
      case REMOVED:
        listener.featureRemoved(this);
        break;
      case UPDATED:
        listener.featureUpdated(this);
        break;
    }
  }

}
