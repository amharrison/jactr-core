package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.runtime.marker.MarkerIndex.MarkerRecord;

public class RunLabelProvider extends BaseLabelProvider implements
    ILabelProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RunLabelProvider.class);

  private boolean                    _showType = false;

  private boolean                    _showId   = false;


  public String getText(Object element)
  {
    if (element instanceof Number)
    {
      double timeInSeconds = ((Number) element).doubleValue();
      return timeFormat(timeInSeconds);
    }
    if (element instanceof MarkerRecord)
    {
      MarkerRecord record = (MarkerRecord) element;
      return String.format("%s %s [%s]%s",
          _showType ? String.format("(%s)", record._type) : "", record._name,
          timeFormat(record._time), record._isClosed ? "-" : "+");
    }
    return null;
  }

  private String timeFormat(double timeInSeconds)
  {
    double hours = Math.floor(timeInSeconds / 3600);
    timeInSeconds -= hours * 3600;
    double minutes = Math.floor(timeInSeconds / 60);
    timeInSeconds -= minutes * 60;

    return String
        .format("%02.0f:%02.0f:%05.2fs", hours, minutes, timeInSeconds);
  }

  public Image getImage(Object element)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
