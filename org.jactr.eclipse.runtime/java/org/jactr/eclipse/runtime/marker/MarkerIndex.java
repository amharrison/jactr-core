package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;

/**
 * index of a particular recorded session's markers and the types of markers
 * 
 * @author harrison
 */
public class MarkerIndex
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MarkerIndex.class);

  private final IResource            _markerIndex;

  private final IResource            _markerTypes;

  private final Set<String>          _knownTypes;

  private final List<MarkerRecord>   _knownMarkers;

  public MarkerIndex(IResource markerIndex, IResource markerTypes)
  {
    _markerIndex = markerIndex;
    _markerTypes = markerTypes;
    _knownTypes = new TreeSet<String>();
    _knownMarkers = new FastList<MarkerRecord>();
    readTypes();
    readIndex();
  }

  public Set<String> getKnownTypes(Set<String> container)
  {
    if (container == null) container = new TreeSet<String>();
    container.addAll(_knownTypes);
    return container;
  }

  public List<MarkerRecord> getKnownRecords(List<MarkerRecord> container)
  {
    if (container == null)
      container = new ArrayList<MarkerRecord>(_knownMarkers.size());
    container.addAll(_knownMarkers);
    return container;
  }

  public List<MarkerRecord> getKnownRecords(String typeFilter,
      String nameFilter, List<MarkerRecord> container)
  {
    if (container == null)
      container = new ArrayList<MarkerRecord>(_knownMarkers.size());

    Pattern type = Pattern.compile(typeFilter != null ? typeFilter : ".*");
    Pattern name = Pattern.compile(nameFilter != null ? nameFilter : ".*");

    for (MarkerRecord record : _knownMarkers)
      if (type.matcher(record._type).matches()
          && name.matcher(record._name).matches()) container.add(record);

    return container;
  }

  protected void readTypes()
  {
    DataInputStream dis = null;

    try
    {
      dis = new DataInputStream(new FileInputStream(new File(
          _markerTypes.getLocationURI())));

      while (dis.available() > 0)
      {
        String type = dis.readUTF();
        _knownTypes.add(type);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Read %s", type));
      }
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(
            String.format("Failed to read %s ", _markerTypes.getName()), e);
      if (dis != null) try
      {
        dis.close();
      }
      catch (Exception e2)
      {

      }
    }
  }

  protected void readIndex()
  {
    DataInputStream dis = null;

    try
    {
      dis = new DataInputStream(new FileInputStream(new File(
          _markerIndex.getLocationURI())));

      while (dis.available() > 0)
      {
        MarkerRecord record = new MarkerRecord(dis);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Read [%d, %s, %s]", record._id,
              record._name, record._type));

        _knownMarkers.add(record);
      }
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(
            String.format("Failed to read %s ", _markerIndex.getName()), e);
      if (dis != null) try
      {
        dis.close();
      }
      catch (Exception e2)
      {

      }
    }
  }

  static public class MarkerRecord
  {
    public final boolean _isClosed;

    public final long    _id;

    public final String  _name;

    public final String  _type;

    public final double  _time;

    public MarkerRecord(DataInputStream dis) throws IOException
    {
      _isClosed = dis.readBoolean();
      _id = dis.readLong();
      _type = dis.readUTF();
      _name = dis.readUTF();
      _time = dis.readDouble();
    }
  }
}
