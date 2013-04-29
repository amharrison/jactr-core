package org.jactr.eclipse.runtime.buffer2;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * contains the information pre/post conflict resolution of all the buffers for
 * a specified time.
 * 
 * @author harrison
 */
public class BufferData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BufferData.class);

  private double                     _time;

  private Map<String, CommonTree[]>  _bufferDescriptors;

  public BufferData(double time)
  {
    _time = time;
    _bufferDescriptors = new TreeMap<String, CommonTree[]>();
  }

  public BufferData(double time, BufferData previous)
  {
    this(time);
    for (String buffer : previous.getBufferNames())
    {
      CommonTree post = previous.getBufferContents(buffer, true);
      if (post != null)
        _bufferDescriptors.put(buffer, new CommonTree[] { post, null });
    }
  }

  public double getTime()
  {
    return _time;
  }

  public Set<String> getBufferNames()
  {
    return Collections.unmodifiableSet(_bufferDescriptors.keySet());
  }

  public CommonTree getBufferContents(String bufferName, boolean postConflict)
  {
    CommonTree[] data = _bufferDescriptors.get(bufferName);
    if (data == null) return null;
    
    if (postConflict)
    {
      if(data[1]==null)
        return data[0];
      return data[1];
    }
    else
      return data[0];
  }

  protected void setBufferContents(String bufferName, CommonTree data,
      boolean postConflictResolution)
  {
    CommonTree[] trees = _bufferDescriptors.get(bufferName);
    if (trees == null)
    {
      trees = new CommonTree[2];
      _bufferDescriptors.put(bufferName, trees);
    }

    trees[(postConflictResolution ? 1 : 0)] = data;
  }
}
