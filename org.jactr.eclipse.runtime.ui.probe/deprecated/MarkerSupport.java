package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.MarkerRange;
import org.eclipse.birt.chart.model.component.impl.MarkerRangeImpl;
import org.eclipse.birt.chart.model.data.NumberDataElement;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.swt.graphics.RGB;
import org.jactr.eclipse.runtime.marker.MarkerSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStreamListener;
import org.jactr.eclipse.runtime.ui.marker.MarkerUI;

/**
 * support class to enable rendering of markers in charts. When a marker is
 * openend, it's end position is set to an arbitrary point in the future, beyond
 * the runtime data window. When that position becomes visible, we extend it,
 * until such time as the true marker end is reached.
 * 
 * @author harrison
 */
public class MarkerSupport implements
    AbstractBIRTProbeContainer.IChartUpdateListener
{
  /**
   * Logger definition
   */
  static private final transient Log                 LOGGER = LogFactory
                                                                .getLog(MarkerSupport.class);

  private final MarkerSessionDataStream              _msds;

  private final AbstractBIRTProbeContainer           _chartContainer;

  private final ILiveSessionDataStreamListener<Long> _markerListener;

  /*
   * the last event we heard from
   */
  private double                                     _lastMarkerUpdateTime;

  private final Set<Long>                            _added;

  private final Set<Long>                            _modified;

  private final Set<Long>                            _removed;

  private final Map<Long, MarkerRange>               _existingMarkers;

  private final double                               _rangePadding;

  public MarkerSupport(AbstractBIRTProbeContainer chartContainer,
      MarkerSessionDataStream msds, double runtimeWindowSize)
  {
    _rangePadding = 2 * runtimeWindowSize;
    _msds = msds;
    _added = new TreeSet<Long>();
    _modified = new TreeSet<Long>();
    _removed = new TreeSet<Long>();
    _existingMarkers = new TreeMap<Long, MarkerRange>();

    _chartContainer = chartContainer;

    _markerListener = new ILiveSessionDataStreamListener<Long>() {

      public void dataChanged(ILiveSessionDataStream stream,
          Collection<Long> added, Collection<Long> modified,
          Collection<Long> removed)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String
              .format("A:%s M:%s R:%s", added, modified, removed));
        synchronized (MarkerSupport.this)
        {
          _added.addAll(added);
          _modified.addAll(modified);
          _removed.addAll(removed);
        }
        // request enventual update of the chart
        _chartContainer.refresh();
      }
    };

    // adding inline
    _msds.addListener(_markerListener, null);

    /*
     * snag all the existing markers.. as new adds
     */
    FastList<Long> added = FastList.newInstance();

    _msds.getData(_msds.getStartTime(), _msds.getEndTime(), added);
    synchronized (this)
    {
      _added.addAll(added);
    }

    FastList.recycle(added);

    _chartContainer.addListener(this);
  }

  /**
   * update the markers, this should be called on the SWT thread
   */
  protected void updateMarkers()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Updating markers %d, %d, %d", _added.size(),
          _modified.size(), _removed.size()));

    FastList<Long> markerContainer = FastList.newInstance();

    try
    {
      ChartWithAxes chart = _chartContainer.getChart();
      Axis xAxisPrimary = chart.getPrimaryBaseAxes()[0];
      chart.getPrimaryOrthogonalAxis(xAxisPrimary);

      // the additions
      getAndClearContents(_added, markerContainer);

      for (Long markerId : markerContainer)
      {
        MarkerRange range = _existingMarkers.get(markerId);
        double openTime = _msds.getOpenTime(markerId);
        if (Double.isNaN(openTime))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Marker %d has no open time", markerId));
          continue;
        }

        double closeTime = _msds.getCloseTime(markerId);
        if (Double.isNaN(closeTime))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Marker %d has no end time, estimating",
                markerId));
          closeTime = openTime + _rangePadding;
        }

        if (range == null)
        {
          String markerName = _msds.getName(markerId);

          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Creating new Marker %d %s %.2f-%.2f",
                markerId, markerName, openTime, closeTime));

          String type = _msds.getType(markerId);

          RGB newRGB = MarkerUI.getInstance().getRGB(type, true);

          range = MarkerRangeImpl
              .create(xAxisPrimary, NumberDataElementImpl.create(openTime),
                  NumberDataElementImpl.create(closeTime), ColorDefinitionImpl
                      .create(newRGB.red, newRGB.green, newRGB.blue));

          range.getLabel().getCaption().setValue(markerName);

          _existingMarkers.put(markerId, range);
        }
      }

      getAndClearContents(_modified, markerContainer);
      for (Long markerId : markerContainer)
      {
        MarkerRange range = _existingMarkers.get(markerId);
        if (range == null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String
                .format("Updated marker %d is unknown", markerId));
          continue;
        }

        double closeTime = _msds.getCloseTime(markerId);
        if (Double.isNaN(closeTime))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Still no end time for marker %d",
                markerId));
          continue;
        }

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Setting endtime for marker %d to %.2f",
              markerId, closeTime));

        range.setEndValue(NumberDataElementImpl.create(closeTime));
      }

      getAndClearContents(_removed, markerContainer);
      for (Long markerId : markerContainer)
      {
        MarkerRange range = _existingMarkers.remove(markerId);
        if (range != null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Removing marker %d", markerId));
          xAxisPrimary.getMarkerRanges().remove(range);
        }
      }

      /*
       * now we check the marker range end times of the known markers. if any
       * are still not closed, but their range times are less than now, we
       * extend them
       */
      double now = _msds.getEndTime();
      for (Map.Entry<Long, MarkerRange> entry : _existingMarkers.entrySet())
      {
        MarkerRange range = entry.getValue();
        NumberDataElement nde = (NumberDataElement) range.getEndValue();
        if (nde.getValue() <= now)
        {
          long markerId = entry.getKey();
          double closeTime = _msds.getCloseTime(markerId);
          // push the end time back
          if (Double.isNaN(closeTime))
          {
            closeTime = now + _rangePadding;
            range.setEndValue(NumberDataElementImpl.create(closeTime));
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format(
                  "Extending marker %d's estimated endtime", markerId,
                  closeTime));
          }
        }
      }

    }
    finally
    {
      FastList.recycle(markerContainer);
    }
  }

  private void getAndClearContents(Set<Long> queue, FastList<Long> container)
  {
    container.clear();
    synchronized (this)
    {
      container.addAll(queue);
      queue.clear();
    }
  }

  public void chartUpdated(boolean timeSpanHasChange, boolean scaleHasChanged,
      boolean newData)
  {
    updateMarkers();
  }
}
