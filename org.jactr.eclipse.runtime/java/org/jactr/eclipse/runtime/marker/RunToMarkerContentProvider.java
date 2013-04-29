package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jactr.eclipse.runtime.marker.MarkerIndex.MarkerRecord;

public class RunToMarkerContentProvider implements ITreeContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RunToMarkerContentProvider.class);

  private final MarkerIndex                                 _index;

  /**
   * first key is the type of marker, then children
   */
  private final SortedMap<String, Collection<MarkerRecord>> _tree;

  public RunToMarkerContentProvider(MarkerIndex index)
  {
    _index = index;
    _tree = new TreeMap<String, Collection<MarkerRecord>>();
    populate();
  }

  protected void populate()
  {
    FastList<MarkerRecord> records = FastList.newInstance();
    _index.getKnownRecords(records);

    for (MarkerRecord record : records)
    {
      String type = record._type;
      Collection<MarkerRecord> allOfType = _tree.get(type);
      if (allOfType == null)
      {
        allOfType = new FastList<MarkerRecord>();
        _tree.put(type, allOfType);
      }
      allOfType.add(record);
    }

    FastList.recycle(records);
  }

  public void dispose()
  {
    _tree.clear();
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {

  }

  public Object[] getElements(Object inputElement)
  {
    return _tree.keySet().toArray();
  }

  public Object[] getChildren(Object parentElement)
  {
    if (parentElement instanceof String)
    {
      String type = (String) parentElement;
      return _tree.get(type).toArray();
    }
    return null;
  }

  public Object getParent(Object element)
  {
    if (element instanceof MarkerRecord) return ((MarkerRecord) element)._type;
    return null;
  }

  public boolean hasChildren(Object element)
  {
    if (element instanceof String) return true;
    return false;
  }

}
