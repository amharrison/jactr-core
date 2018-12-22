package org.commonreality.time.impl;

/*
 * default logging
 */
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.IAuthoritativeClock;
import org.commonreality.time.IClock;

/**
 * 
 * @author harrison
 *
 */
public abstract class AbstractAuthoritativeClock extends WrappedClock implements IAuthoritativeClock
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractAuthoritativeClock.class);

  public AbstractAuthoritativeClock(IClock master)
  {
    super(master);
  }

  @Override
  public Optional<IAuthoritativeClock> getAuthority()
  {
    return Optional.of(this);
  }

}
