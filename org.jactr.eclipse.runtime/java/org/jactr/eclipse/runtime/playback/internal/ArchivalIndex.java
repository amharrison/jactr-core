package org.jactr.eclipse.runtime.playback.internal;

/*
 * default logging
 */
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;

public class ArchivalIndex
{
  /**
   * Logger definition
   */
  static private final transient Log   LOGGER = LogFactory
                                                  .getLog(ArchivalIndex.class);

  final private IResource              _indexFile;

  final private TreeMap<Double, Index> _timeIndex;

  private double                       _readRecordRange;

  final private double[]               _span  = { Double.MAX_VALUE,
      Double.MIN_VALUE                       };

  public ArchivalIndex(IResource resource)
  {
    _indexFile = resource;
    _timeIndex = new TreeMap<Double, Index>();
  }

  public IResource getIndexFile()
  {
    return _indexFile;
  }

  public void open()
  {
    read(_indexFile);
  }

  public void close()
  {
    _timeIndex.clear();
  }

  /**
   * pump out all the events from startTime to endTime.
   * 
   * @param startTime
   * @param endTime
   * @param pumper
   */
  public void pump(double startTime, double endTime, EventPumper pumper)
  {
    double min = Math.max(startTime, _span[0]);
    double max = Math.min(endTime, _span[1]);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Will pump %.2f-%.2f (%.2f, %.2f)", startTime,
          endTime, min, max));
    /*
     * determine which time blocks will be needed, then pump the relevant
     * subsections and full blocks.
     */
    SortedMap<Double, Index> subset = _timeIndex.tailMap(min);

    if (subset.size() == 0)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("No data available to pump"));
      return;
    }

    /*
     * we need to grow the range
     */
    if (min < subset.firstKey())
      subset = _timeIndex.tailMap(min - _readRecordRange);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("tailSubset : %s", subset.keySet()));

    /*
     * constraining at the max end doesn't quite work. Let's say you've pumped
     * events up to 5s. But there is a large cycle skip from 5-100s. Any pump
     * request in that range will be ignored by this constraint. We need to go
     * the the next key. Instead we want the next larger end time.
     */
    Double nextHigherKey = _timeIndex.ceilingKey(max + _readRecordRange / 2);
    if (nextHigherKey != null)
    {
      nextHigherKey += 0.05;
      subset = subset.headMap(nextHigherKey);
      max = nextHigherKey;
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("headSubset : %s", subset.keySet()));

    for (Index index : subset.values())
    {
      Index idx = new Index();
      idx._span = new double[2];
      idx._span[0] = Math.max(min, index._span[0]);
      idx._span[1] = Math.min(max, index._span[1]);

      if (idx._span[0] > idx._span[1])
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Span invert?"));
        continue;
      }

      idx._data = index._data;

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Pumping [%.2f, %.2f) %s", idx._span[0],
            idx._span[1], idx._data.getLocationURI()));

      pumper.pump(idx);
    }

  }

  public double getStartTime()
  {
    return _span[0];
  }

  public double getEndTime()
  {
    return _span[1];
  }

  private boolean read(IResource index)
  {
    File fp = new File(index.getRawLocationURI());
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(fp);
      // GZIPInputStream giz = new GZIPInputStream(fis);
      DataInputStream dis = new DataInputStream(fis);

      while (dis.available() > 0)
      {
        double[] range = new double[2];
        for (int i = 0; i < range.length; i++)
          range[i] = dis.readDouble();

        Index record = new Index();
        record._span = range;

        _readRecordRange = range[1] - range[0];

        String path = dis.readUTF();
        record._data = index.getParent().findMember(path);

        _timeIndex.put(range[0], record);

        _span[0] = Math.min(_span[0], range[0]);
        _span[1] = Math.max(_span[1], range[1]);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("read [%.2f, %.2f] %s", range[0],
              range[1], record._data));
      }

      return true;
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to read index ", e);

      return false;
    }
    finally
    {
      if (fis != null) try
      {
        fis.close();
      }
      catch (IOException e1)
      {
        LOGGER.error("ArchivalIndex.read threw IOException : ", e1);
      }

    }
  }

  public class Index
  {
    public double[]  _span;

    public IResource _data;
  }
}
