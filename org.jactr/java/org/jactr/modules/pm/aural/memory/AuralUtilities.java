package org.jactr.modules.pm.aural.memory;

import java.util.ArrayList;
import java.util.List;

import org.commonreality.identifier.IIdentifier;
import org.jactr.core.chunk.IChunk;
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.slf4j.LoggerFactory;

public class AuralUtilities
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(AuralUtilities.class);

  
  static public PerceptualSearchResult getSearchResult(IChunk locationChunk,
      IAuralMemory auralMemory)
  {
    List<PerceptualSearchResult> results = new ArrayList<>();
    auralMemory.getRecentSearchResults(results);

    for (PerceptualSearchResult result : results)
      if (locationChunk==result.getLocation())
        return result;

    
    return null;
  }

  /**
   * return search result with identifier
   * 
   * @param perceptualIdentifier
   * @param auralMemory
   * @return
   */
  static public PerceptualSearchResult getSearchResult(
      IIdentifier perceptualIdentifier, IAuralMemory auralMemory)
  {
    List<PerceptualSearchResult> results = new ArrayList<>();
    auralMemory.getRecentSearchResults(results);

    for (PerceptualSearchResult result : results)
      if (result.getPerceptIdentifier().equals(perceptualIdentifier))
        return result;

    
    return null;
  }

  /**
   * @param searchResult
   * @param auralMemory
   * @return
   */
  static public IChunk getAuralEvent(PerceptualSearchResult searchResult,
      IAuralMemory auralMemory)
  {
    if (auralMemory.getLastSearchResult() != searchResult)
      return searchResult.getLocation();

    /*
     * need to snag the screen-pos for visual object
     */
    IChunk auralChunk = searchResult.getPercept();
    return (IChunk) auralChunk.getSymbolicChunk().getSlot(
        IAuralModule.EVENT_SLOT).getValue();
  }


}
