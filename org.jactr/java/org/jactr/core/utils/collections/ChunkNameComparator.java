package org.jactr.core.utils.collections;

/*
 * default logging
 */
import java.util.Comparator;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;

public class ChunkNameComparator implements Comparator<IChunk>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkNameComparator.class);

  public int compare(IChunk o1, IChunk o2)
  {
    return o1.getSymbolicChunk().getName()
        .compareTo(o2.getSymbolicChunk().getName());
  }

}
