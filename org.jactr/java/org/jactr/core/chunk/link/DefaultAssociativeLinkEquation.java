package org.jactr.core.chunk.link;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.IModel;

/**
 * noop associative link equation that just returns the existing link value and
 * does nothing on reset
 * 
 * @author harrison
 */
public class DefaultAssociativeLinkEquation implements IAssociativeLinkEquation
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultAssociativeLinkEquation.class);

  public double computeLearnedStrength(IAssociativeLink link)
  {
    return link.getStrength();
  }

  public void resetStrengths(IModel model)
  {
    // noop

  }

  public double computeDefaultStrength(IAssociativeLink link)
  {
    return 0;
  }

}
