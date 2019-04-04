package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.util.Calendar;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.jactr.core.utils.collections.FastSetFactory;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.probe3.ModelProbeData2;

public class XYGraphProbeContainer extends
    AbstractProbeContainer<ModelProbeData2, Trace>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER                 = LogFactory
                                                                .getLog(XYGraphProbeContainer.class);

  private XYGraph                    _graph;

  protected Job                      _updateJob;

  private volatile boolean           _jobQueued             = false;

  private long                       MINIMUM_RESPONSIVENESS = 500;                                   // ms

  public XYGraphProbeContainer(Composite parent, ModelProbeData2 data)
  {
    super(parent, SWT.BORDER_SOLID);

    _probeData = data;
    LightweightSystem lws = new LightweightSystem(this);

    _graph = new XYGraph();
    ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(_graph);

    lws.setContents(toolbarArmedXYGraph);

    // build the component here-ish
    configureGraph(_graph);

    _updateJob = new UIJob("Probe Render") {

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor)
      {
        if (!isDisposed())
          getDisplay().asyncExec(new Runnable() {

            /*
             * signal that a repaint is needed
             */
            public void run()
            {
              // done drawing, swap the image buffers
              if (isDisposed()) return;

              refreshInternal();

              monitor.done();
              _jobQueued = false;
              // _updateJob.schedule(MINIMUM_RESPONSIVENESS);
            }

          });
        else
        {
          monitor.done();
          _jobQueued = false;
        }

        return Status.OK_STATUS;
      }
    };

    // so it is hidden
    _updateJob.setSystem(true);
  }

  protected void configureGraph(XYGraph graph)
  {
    graph.getPrimaryXAxis().setTitle("Time");
    graph.getPrimaryXAxis().setTimeUnit(Calendar.MILLISECOND);
    graph.getPrimaryXAxis().setAutoScale(true);
    graph.getPrimaryXAxis().setDateEnabled(true);
    graph.getPrimaryXAxis().setShowMajorGrid(true);


    graph.getPrimaryXAxis().setRange(
        0,
        RuntimePlugin.getDefault().getPreferenceStore()
            .getInt(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW));

    graph.getPrimaryXAxis().setZoomType(ZoomType.PANNING);
    graph.getPrimaryXAxis().setFormatPattern("mm:ss.SSS");

    graph.getPrimaryYAxis().setTitle("Parameter Value");
    graph.getPrimaryYAxis().setAutoScale(true);
    graph.getPrimaryYAxis().setShowMajorGrid(true);

    // graph.getPlotArea().addMouseListener(new MouseListener.Stub() {
    // @Override
    // public void mousePressed(final MouseEvent me)
    // {
    // showNearestValue(me);
    // }
    // });
  }

  @Override
  public void setFilteredProbes(Set<String> probeNames)
  {
    synchronized (_filteredOut)
    {
      _filteredOut.clear();
      _filteredOut.addAll(probeNames);
    }

    Set<String> allKnown = FastSetFactory.newInstance();
    allKnown.addAll(_probeSeries.keySet());

    for (String probeName : allKnown)
    {
      Trace series = _probeSeries.get(probeName);
      if (series != null)
      {
        boolean filterOut = probeNames.contains(probeName);
        series.setVisible(!filterOut);
        // filter
      }
    }

    FastSetFactory.recycle(allKnown);
  }

  @Override
  public void dispose()
  {
    super.dispose();
    for (Trace t : _probeSeries.values())
      t.getTraceColor().dispose();
    _probeSeries.clear();
  }

  @Override
  public void refresh()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("refresh requested"));
    if (!_jobQueued)
    {
      _jobQueued = true;
      _updateJob.schedule(MINIMUM_RESPONSIVENESS);
    }
  }

  /**
   * add new traces where necessary. Only run on UI thread due to color
   * resources
   */
  protected void refreshInternal()
  {
    Set<String> allKnown = FastSetFactory.newInstance();
    allKnown.addAll(_probeSeries.keySet());

    Set<String> allData = FastSetFactory.newInstance();

    ModelProbeData2 mpd2 = getProbeData();
    mpd2.getProbeNames(allData);

    allData.removeAll(allKnown);

    if (allData.size() > 0)
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Adding traces for : %s", allData));

    for (String newTrace : allData)
    {
      // this had better be true..
      ProbeDataSourceProvider pd = (ProbeDataSourceProvider) mpd2
          .getProbeData(newTrace);
      Trace trace = new Trace(newTrace, _graph.getPrimaryXAxis(),
          _graph.getPrimaryYAxis(), pd);

      PointStyle[] values = PointStyle.values();
      int hash = Math.abs(newTrace.hashCode());
      trace.setPointStyle(values[hash % values.length]);
      trace.setPointSize(6);


      Color traceColor = new Color(Display.getCurrent(), hash / 2 % 256,
          hash / 3 % 256, hash / 4 % 256);
      trace.setTraceColor(traceColor);

      trace.setTraceType(TraceType.SOLID_LINE);
      trace.setVisible(true);

      _probeSeries.put(newTrace, trace);
      _graph.addTrace(trace);
    }


    FastSetFactory.recycle(allKnown);
    FastSetFactory.recycle(allData);
  }

  protected void showNearestValue(MouseEvent me)
  {
    IFigure figure = _graph.findFigureAt(me.x, me.y);
    if (figure instanceof Trace)
    {
      Trace t = (Trace) figure;
      t.getName();

    }
  }
}
