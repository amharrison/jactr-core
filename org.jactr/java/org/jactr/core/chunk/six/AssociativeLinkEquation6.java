package org.jactr.core.chunk.six;

import java.util.Collection;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.four.Link4;
import org.jactr.core.chunk.link.IAssociativeLink;
import org.jactr.core.chunk.link.IAssociativeLinkEquation;
import org.jactr.core.model.IModel;
import org.jactr.core.module.declarative.associative.IAssociativeLinkContainer;
import org.jactr.core.module.declarative.six.learning.IDeclarativeLearningModule6;
import org.jactr.core.utils.collections.FastCollectionFactory;
import org.slf4j.LoggerFactory;

public class AssociativeLinkEquation6 implements IAssociativeLinkEquation
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(AssociativeLinkEquation6.class);

  private IDeclarativeLearningModule6 _declarativeLearningModule;

  public AssociativeLinkEquation6(IDeclarativeLearningModule6 decLM)
  {
    _declarativeLearningModule = decLM;
  }

  public double computeLearnedStrength(IAssociativeLink link)
  {
    return computeDefaultStrength(link);
  }

  public double computeDefaultStrength(IAssociativeLink link)
  {
    if (Double.isNaN(_declarativeLearningModule.getMaximumStrength())) return 0;

    double max = _declarativeLearningModule.getMaximumStrength();

    Collection<IAssociativeLink> links = FastCollectionFactory.newInstance();
    link.getJChunk().getAdapter(IAssociativeLinkContainer.class)
        .getOutboundLinks(links);

    double fanj = links.stream().filter(l -> {
      return l.getIChunk().isEncoded();
    }).count();

    FastCollectionFactory.recycle(links);
    links = null;

    fanj = fanj / ((Link4) link).getCount();

    double ln = Math.log(fanj);
    double strength = max - ln;

    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format(
        "Sji j:%s i:%s fanj = %.2f, ln(fanj) = %.2f, max = %.2f yields Sji %.2f",
        link.getJChunk(), link.getIChunk(), fanj, ln, max, strength));

    return strength;
  }

  public void resetStrengths(IModel model)
  {
    Collection<IAssociativeLink> links = FastCollectionFactory.newInstance();
    try
    {
      for (IChunk chunk : model.getDeclarativeModule().getChunks().get())
      {
        IAssociativeLinkContainer alc = chunk
            .getAdapter(IAssociativeLinkContainer.class);
        links.clear();
        alc.getOutboundLinks(links);
        for (IAssociativeLink link : links)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Reseting strength of " + link);
          ((Link4) link).setStrength(computeDefaultStrength(link));
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Could not reset links because of an exception ", e);
    }
    finally
    {
      FastCollectionFactory.recycle(links);
    }
  }

}
