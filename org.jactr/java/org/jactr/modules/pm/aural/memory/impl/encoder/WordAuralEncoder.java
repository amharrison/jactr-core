package org.jactr.modules.pm.aural.memory.impl.encoder;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class WordAuralEncoder extends AbstractAuralEncoder
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(WordAuralEncoder.class);

  public WordAuralEncoder()
  {
    super("word");
  }



}
