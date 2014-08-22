package org.jactr.eclipse.runtime.ui.log2;

/*
 * default logging
 */
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jactr.core.logging.Logger;
import org.jactr.eclipse.runtime.log2.LogData;
import org.jactr.eclipse.runtime.marker.OpenMarkerSessionDataStream;
import org.jactr.eclipse.runtime.marker.OpenMarkers;
import org.jactr.eclipse.runtime.ui.marker.MarkerUI;

public class TableLabelProvider extends LabelProvider implements
    ITableLabelProvider, ITableColorProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TableLabelProvider.class);

  private Table                      _table;

  private LogData                    _lastBackgroundQuery;

  private Color                      _lastBackgroundColor;

  public TableLabelProvider(Table table)
  {
    _table = table;
  }

  public Image getColumnImage(Object element, int columnIndex)
  {
    return null;
  }

  public String getColumnText(Object element, int columnIndex)
  {
    LogData ld = (LogData) element;

    TableColumn column = _table.getColumn(columnIndex);

    String text = ld.get(column.getText());
    if(column.getText().equals(Logger.Stream.PROCEDURAL.toString())) {
    	// Show only the last line of the procedural text (or the line
    	// before the last line, if the last line is empty).
    	int lastIndex = text.lastIndexOf("\n");
    	if(lastIndex != -1) {
    		String newText = text.substring(lastIndex + 1).trim();
    		if(newText.length() == 0 && lastIndex != -1) {
    			int secondLastIndex = text.lastIndexOf("\n", lastIndex-1);
    			newText = text.substring(secondLastIndex+1).trim();
    		}
    		text = newText;
    	}
    }
    return text;
  }

  public Color getForeground(Object element, int columnIndex)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Color getBackground(Object element, int columnIndex)
  {
    LogData ld = (LogData) element;
    /*
     * we only color by row
     */
    if (ld == _lastBackgroundQuery) return _lastBackgroundColor;

    _lastBackgroundQuery = ld;

    Color color = null;
    /*
     * do we support coloring?
     */
    OpenMarkerSessionDataStream omds = (OpenMarkerSessionDataStream) ld
        .getDataStream().getSessionData().getDataStream("openMarkers");

    if (omds != null)
    {
      /*
       * we may have marker data.
       */
      FastList<OpenMarkers> openMarkers = FastList.newInstance();
      FastList<Long> markerIds = FastList.newInstance();
      try
      {
        double logTime = ld.getTime();
        omds.getLatestData(logTime, openMarkers);

        /*
         * find the first one we want to render.
         */
        if (openMarkers.size() > 0)
          for (OpenMarkers open : openMarkers)
          {
            markerIds.clear();
            open.getOpenMarkers(markerIds);

            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("[%.2f] Currently open markers : %s",
                  logTime,
                  markerIds));

            for (Long markerId : markerIds)
              if (isMarkerDisplayable(markerId))
              {
                String type = omds.getRawMarkerDataStream().getType(markerId);
                if (type == null) continue;

                color = MarkerUI.getInstance().getColor(type, true);
                if (color != null) break;
              }
          }
      }
      finally
      {
        FastList.recycle(openMarkers);
        FastList.recycle(markerIds);
      }
    }

    _lastBackgroundColor = color;
    return color;
  }

  private boolean isMarkerDisplayable(Long markerId)
  {
    return true;
  }

}
