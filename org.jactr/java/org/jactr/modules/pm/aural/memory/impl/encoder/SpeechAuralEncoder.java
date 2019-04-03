package org.jactr.modules.pm.aural.memory.impl.encoder;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class SpeechAuralEncoder extends AbstractAuralEncoder
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(SpeechAuralEncoder.class);

  public SpeechAuralEncoder()
  {
    super("speech");
  }



}
