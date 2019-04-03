package org.jactr.modules.pm.aural.memory.impl.encoder;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class DigitAuralEncoder extends AbstractAuralEncoder
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DigitAuralEncoder.class);

  public DigitAuralEncoder()
  {
    super("digit");
  }


}
