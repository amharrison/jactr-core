package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.NumberFormatSpecifier;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.NumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.CurveFitting;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.CurveFittingImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.ScatterSeriesImpl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.core.concurrent.QueueingJob;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.probe2.ModelProbeData;
import org.jactr.eclipse.runtime.ui.probe.TimeFormatSpecifier;

public abstract class AbstractBIRTProbeContainer extends
    AbstractProbeContainer<ModelProbeData, Series>
{
  static final transient Log               LOGGER                 = LogFactory
                                                                      .getLog(AbstractBIRTProbeContainer.class);

  private long                             MINIMUM_RESPONSIVENESS = 500;                                      // ms

  protected ChartWithAxes                  _chart;

  protected volatile GeneratedChartState   _chartState;

  protected IDeviceRenderer                _chartRenderer;

  protected Bounds                         _chartBounds;

  protected double[]                       _timeSpan              = new double[2];

  protected double[]                       _scale                 = new double[] {
      -1, 1                                                      };

  protected QueueingJob                    _updateJob;

  protected ReentrantLock                  _contextLock           = new ReentrantLock();

  boolean                                  _useScatter            = false;

  private Collection<IChartUpdateListener> _chartUpdateListeners  = new ArrayList<IChartUpdateListener>();

  public AbstractBIRTProbeContainer(Composite parent, ModelProbeData mpd)
  {
    super(parent, SWT.BORDER_SOLID);

    _probeData = mpd;


    setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    initializeChart();

    addPaintListener(new PaintListener() {

      public void paintControl(PaintEvent e)
      {
        try
        {
          _contextLock.lock();

          Image uiImage = getUIImage();
          if (uiImage != null && !uiImage.isDisposed())
            e.gc.drawImage(uiImage, 0, 0);
        }
        finally
        {
          _contextLock.unlock();
        }
      }

    });

    _updateJob = new QueueingJob("Probe Render") {

      @Override
      protected IStatus run(final IProgressMonitor monitor)
      {
        final boolean updated = updateChart(monitor);
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Chart updated. RefreshRequired : %s",
              updated));

        if (!isDisposed() && updated)
          getDisplay().asyncExec(new Runnable() {

            /*
             * signal that a repaint is needed
             */
            public void run()
            {
              // done drawing, swap the image buffers
              if (isDisposed()) return;

              swapImage();

              getParent().redraw();

              monitor.done();

              _updateJob.schedule(MINIMUM_RESPONSIVENESS);
            }

          });
        else
          monitor.done();

        return Status.OK_STATUS;
      }
    };

    parent.addControlListener(new ControlListener() {

      public void controlMoved(ControlEvent e)
      {

      }

      public void controlResized(ControlEvent e)
      {
        updateRenderContext();
        refresh();
      }

    });

    updateRenderContext();

    _updateJob.schedule(MINIMUM_RESPONSIVENESS);
  }

  public ChartWithAxes getChart()
  {
    return _chart;
  }


  @Override
  public void setFilteredProbes(Set<String> probeNames)
  {
    synchronized (_filteredOut)
    {
      _filteredOut.clear();
      _filteredOut.addAll(probeNames);
    }

    FastSet<String> allKnown = FastSet.newInstance();
    allKnown.addAll(_probeSeries.keySet());

    for (String probeName : allKnown)
    {
      Series series = _probeSeries.get(probeName);
      if (series != null)
      {
        boolean filterOut = probeNames.contains(probeName);
        series.setVisible(!filterOut);
      }
    }

    FastSet.recycle(allKnown);
  }


  /**
   * swap or copy the render image into the uiimage
   */
  abstract protected void swapImage();

  /**
   * this is the image that is rendered to the component
   * 
   * @return
   */
  abstract protected Image getUIImage();

  /**
   * update the context that the chart will be rendered to
   */
  abstract protected void updateRenderContext();

  /**
   * returns the required renderer
   * 
   * @return
   */
  abstract protected IDeviceRenderer getDeviceRenderer();

  protected void initializeChart()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("initializing chart"));
    try
    {

      // config.setProperty("STANDALONE", "true");
      _chartRenderer = getDeviceRenderer();
      _chart = ChartWithAxesImpl.create();

      // Plot
      _chart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
      _chart.getTitle().getLabel().getCaption().setValue("");

      Plot p = _chart.getPlot();
      p.getClientArea()
          .setBackground(ColorDefinitionImpl.create(255, 255, 225));

      // Legend
      //
      // Legend lg = _chart.getLegend();
      // LineAttributes lia = lg.getOutline();
      // lg.getText().getFont().setSize(16);
      // lia.setStyle(LineStyle.SOLID_LITERAL);
      // lg.getInsets().setLeft(10);
      // lg.getInsets().setRight(10);

      Axis xAxisPrimary = _chart.getPrimaryBaseAxes()[0];
      // xAxisPrimary.setType(AxisType.TEXT_LITERAL);
      xAxisPrimary.setType(AxisType.LINEAR_LITERAL);
      xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
      xAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);

      xAxisPrimary.getTitle().getCaption().setValue("Time");//$NON-NLS-1$
      xAxisPrimary.getTitle().setVisible(true);
      xAxisPrimary.setTitlePosition(Position.BELOW_LITERAL);

      // NumberFormatSpecifier spec = NumberFormatSpecifierImpl.create();
      // spec.setFractionDigits(2);
      xAxisPrimary.setFormatSpecifier(new TimeFormatSpecifier());

      xAxisPrimary.getLabel().getCaption().getFont().setRotation(75);
      xAxisPrimary.setLabelPosition(Position.BELOW_LITERAL);
      xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
      xAxisPrimary.getMajorGrid().getLineAttributes()
          .setStyle(LineStyle.DOTTED_LITERAL);

      xAxisPrimary.getMajorGrid().getLineAttributes()
          .setColor(ColorDefinitionImpl.create(64, 64, 64));

      // xAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);

      // Y-Axis

      Axis yAxisPrimary = _chart.getPrimaryOrthogonalAxis(xAxisPrimary);
      yAxisPrimary.getLabel().getCaption().setValue("");//$NON-NLS-1$
      yAxisPrimary.getLabel().getCaption().getFont().setRotation(37);

      NumberFormatSpecifier spec = NumberFormatSpecifierImpl.create();
      spec.setFractionDigits(2);
      yAxisPrimary.setFormatSpecifier(spec);

      yAxisPrimary.setLabelPosition(Position.LEFT_LITERAL);
      yAxisPrimary.setTitlePosition(Position.LEFT_LITERAL);
      yAxisPrimary.getTitle().getCaption().setValue("");//$NON-NLS-1$
      yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
      yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
      yAxisPrimary.getMajorGrid().getLineAttributes()
          .setStyle(LineStyle.DOTTED_LITERAL);

      // yAxisPrimary.getMajorGrid().getLineAttributes().setColor(
      // ColorDefinitionImpl.RED());

      // yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);

      // X-Series

      Series seCategory = SeriesImpl.create();
      SeriesDefinition sdX = SeriesDefinitionImpl.create();
      xAxisPrimary.getSeriesDefinitions().add(sdX);
      sdX.getSeries().add(seCategory);

      SeriesDefinition sdY = SeriesDefinitionImpl.create();
      yAxisPrimary.getSeriesDefinitions().add(sdY);
      sdY.getSeriesPalette().shift(-1);
      // sdY.getSeriesPalette().getEntries().clear();
    }
    catch (Exception e)
    {
      RuntimePlugin.error("Could not create chart ", e);
      LOGGER.error("Could not create chart ", e);
    }
  }

  /**
   * renders the chart into the image displayed by the component
   * 
   * @param newData
   * @param scaleHasChanged
   * @param timeSpanHasChanged
   */
  private void renderBackingImage(boolean timeSpanHasChanged,
      boolean scaleHasChanged, boolean newData)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "renderingChart timeChange:%s scaleChange:%s newData:%s",
          timeSpanHasChanged, scaleHasChanged, newData));
    try
    {
      _contextLock.lock();

      Generator gr = Generator.instance();

      try
      {
        if (timeSpanHasChanged || scaleHasChanged)
          _chartState = gr.build(_chartRenderer.getDisplayServer(), _chart,
              _chartBounds, null, null, null);
        else
          gr.refresh(_chartState);

        gr.render(_chartRenderer, _chartState);
      }
      catch (Exception ce)
      {
        LOGGER.error("Failed to render", ce);
        RuntimePlugin.error(String.format("Failed to render chart."), ce);
      }
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  public void addListener(IChartUpdateListener listener)
  {
    synchronized (_chartUpdateListeners)
    {
      _chartUpdateListeners.add(listener);
    }
  }

  public void removeListener(IChartUpdateListener listener)
  {
    synchronized (_chartUpdateListeners)
    {
      _chartUpdateListeners.remove(listener);
    }
  }

  /**
   * signal that the contents have changed and trigger a potential redial.
   */
  public void refresh()
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Refresh queued"));
    _updateJob.queue(MINIMUM_RESPONSIVENESS);
  }

  /**
   * update the chart data, possibly invalidating caches, for the chart. then
   * rendering to the back image
   * 
   * @return true if any changes have been made
   */
  protected boolean updateChart(IProgressMonitor monitor)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Updating chart"));
    /*
     * has the size of the canvas changed?
     */

    /*
     * first we see if either the max/min or the time scale has changed.
     */
    boolean timeSpanHasChanged = hasTimeSpanChanged();
    boolean scaleHasChanged = hasDataRangeChanged();
    boolean newData = false;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("timeSpanChanged:%s scaleHasChanged:%s. %s",
          timeSpanHasChanged, scaleHasChanged, timeSpanHasChanged
              || scaleHasChanged ? "chart rebuild required." : ""));

    if (timeSpanHasChanged && LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("time span (%.2f, %.2f) : axis (%.2f, %.2f)",
          _probeData.getStartTime(), _probeData.getEndTime(), _timeSpan[0],
          _timeSpan[1]));

    if (scaleHasChanged && LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("scale span (%.2f, %.2f) : axis (%.2f, %.2f)",
          _probeData.getMinimumValue(), _probeData.getMaximumValue(),
          _scale[0], _scale[1]));

    FastSet<String> probeNames = FastSet.newInstance();
    _probeData.getProbeNames(probeNames);

    probeNames.removeAll(_probeSeries.keySet());

    /*
     * remaining are new.
     */
    monitor.beginTask("Updating Chart", 5);

    monitor.subTask("Processing new data");
    for (String probeName : probeNames)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("adding series for probe %s", probeName));

      Series series = addSeries(probeName);
      _probeSeries.put(probeName, series);
      double[] data = _probeData.getProbeData(probeName, null);

      series.setDataSet(NumberDataSetImpl.create(data));
      newData = true;
    }
    monitor.worked(1);

    /*
     * zip through all of our probes. for (String probeName :
     * _probeData.getProbeNames(null)) { Series series =
     * _probeSeries.get(probeName); if (series == null) { if
     * (LOGGER.isDebugEnabled())
     * LOGGER.debug(String.format("adding series for probe %s", probeName));
     * series = addSeries(probeName); _probeSeries.put(probeName, series);
     * double[] data = _probeData.getProbeData(probeName, null);
     * series.setDataSet(NumberDataSetImpl.create(data)); newData = true; }
     * boolean shouldHide = _filteredOut.contains(probeName); if (shouldHide) {
     * if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Should hide %s",
     * probeName)); newData |= series.isVisible(); }
     * series.setVisible(!shouldHide); }
     */

    FastSet.recycle(probeNames);

    /*
     * update the axis.
     */
    monitor.subTask("Adjusting time scale");
    if (timeSpanHasChanged) adjustTimeScale();
    monitor.worked(1);

    /*
     * update the scales
     */
    monitor.subTask("Updating scale");
    if (scaleHasChanged)
    {

      double low = _probeData.getMinimumValue();
      double high = _probeData.getMaximumValue() * 1.1;

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Updating value scale (%.2f - %.2f)", low,
            high));

      Axis xAxisPrimary = _chart.getPrimaryBaseAxes()[0];
      Axis yAxisPrimary = _chart.getPrimaryOrthogonalAxis(xAxisPrimary);
      yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(high));
      yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(low));

      _scale[0] = low;
      _scale[1] = high;
    }
    monitor.worked(1);

    monitor.subTask("Notifying listeners");
    synchronized (_chartUpdateListeners)
    {
      for (IChartUpdateListener listener : _chartUpdateListeners)
        listener.chartUpdated(timeSpanHasChanged, scaleHasChanged, newData);
    }
    monitor.worked(1);

    monitor.subTask("Rendering");

    renderBackingImage(timeSpanHasChanged, scaleHasChanged, newData);
    monitor.worked(1);

    return timeSpanHasChanged || scaleHasChanged || newData;
  }

  private void adjustTimeScale()
  {
    double[] ranges = new double[(int) Math.max(_probeData.getTimeWindow(), 15) / 5 + 1];
    ranges[0] = 1;
    for (int i = 1; i < ranges.length; i++)
      ranges[i] = i * 5;

    double[] sampleTimes = _probeData.getSampleTimes(null);
    double delta = _probeData.getEndTime() - _probeData.getStartTime();

    int rangeIndex = ranges.length - 1;
    for (int i = 0; i < ranges.length; i++)
      if (delta < ranges[i])
      {
        rangeIndex = i;
        break;
      }

    _timeSpan[1] = _probeData.getEndTime();
    _timeSpan[0] = _timeSpan[1] - ranges[rangeIndex];

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "Updating time scale w/ %d samples, using range [%.2f, %.2f] (%.2f)",
          sampleTimes.length, _timeSpan[0], _timeSpan[1],
          _probeData.getEndTime()));

    Axis xAxisPrimary = _chart.getPrimaryBaseAxes()[0];

    xAxisPrimary.getSeriesDefinitions().get(0).getSeries().get(0)
        .setDataSet(NumberDataSetImpl.create(sampleTimes));

    xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(_timeSpan[1]));
    xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(_timeSpan[0]));

  }

  private boolean hasTimeSpanChanged()
  {
    return _probeData.getEndTime() > _timeSpan[1];
  }

  private boolean hasDataRangeChanged()
  {
    return _probeData.getMinimumValue() < _scale[0]
        || _probeData.getMaximumValue() > _scale[1];
  }

  protected Series addSeries(String probeName)
  {
    LineSeries series = null;
    if (_useScatter)
      series = (LineSeries) ScatterSeriesImpl.create();
    else
      series = (LineSeries) LineSeriesImpl.create();

    series.setPaletteLineColor(true);
    series.setSeriesIdentifier(probeName);
    series.setConnectMissingValue(true);

    if (_useScatter)
    {
      CurveFitting curve = CurveFittingImpl.create();
      series.setCurve(true);
      series.setCurveFitting(curve);
    }

    MarkerType type = MarkerType.get(Math.abs(probeName.hashCode())
        % MarkerType.VALUES.size());

    for (Object m : series.getMarkers())
      ((Marker) m).setType(type);

    // series.setDataSet(NumberDataSetImpl.create(new double[] { 0 }));

    SeriesDefinition seriesDef = _chart
        .getPrimaryOrthogonalAxis(_chart.getPrimaryBaseAxes()[0])
        .getSeriesDefinitions().get(0);

    seriesDef.getSeries().add(series);

    return series;
  }

  @Override
  public boolean setFocus()
  {
    refresh();
    return super.setFocus();
  }

  public interface IChartUpdateListener
  {
    public void chartUpdated(boolean timeSpanHasChange,
        boolean scaleHasChanged, boolean newData);
  }
}