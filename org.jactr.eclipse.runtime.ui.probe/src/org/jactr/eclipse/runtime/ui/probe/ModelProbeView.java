package org.jactr.eclipse.runtime.ui.probe;

/*
 * default logging
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.jactr.eclipse.runtime.probe2.ModelProbeData;
import org.jactr.eclipse.runtime.probe3.IModelProbeSessionDataStream;
import org.jactr.eclipse.runtime.probe3.ModelProbeData2;
import org.jactr.eclipse.runtime.session.ILocalSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStream;
import org.jactr.eclipse.runtime.session.stream.ILiveSessionDataStreamListener;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.runtime.ui.misc.AbstractRuntimeModelViewPart;
import org.jactr.eclipse.runtime.ui.probe.components.AbstractProbeContainer;
import org.jactr.eclipse.runtime.ui.probe.components.XYGraphProbeContainer;
import org.jactr.eclipse.ui.images.JACTRImages;

public class ModelProbeView extends AbstractRuntimeModelViewPart
{
  /**
   * Logger definition
   */
  static private final transient Log                      LOGGER                  = LogFactory
                                                                                      .getLog(ModelProbeView.class);

  private IAction                                         _saveCVSAction;

  private Action                                          _filterAction;

  // private boolean _useAWT = false;

  private IPath                                           _lastSaveTo;

  private final Map<String, Set<String>>                  _filteredProbes;

  // private final Map<ISessionData, MarkerSupport> _installedMarkerSupport =
  // new HashMap<ISessionData, MarkerSupport>();

  @SuppressWarnings("rawtypes")
  private final Map<ISessionData, AbstractProbeContainer> _installedContainers    = new HashMap<ISessionData, AbstractProbeContainer>();

  // private QueueingUIJob _updater;

  public ModelProbeView()
  {
    /*
     * sanity check. There seen to be some crash issues w/ cocoa & birt on Mac.
     */
    // String os = System.getProperty("os.name");
    // if (os.equalsIgnoreCase("mac os x")) _useAWT = true;
    _filteredProbes = new TreeMap<String, Set<String>>();

    // _updater = new QueueingUIJob("Probe Populator") {
    //
    // @Override
    // public IStatus runInUIThread(IProgressMonitor monitor)
    // {
    // LOGGER.warn("This should be calling the graph display update");
    // return Status.OK_STATUS;
    // }
    // };
    //
    // // hide it from the user
    // _updater.setSystem(true);
  }

  @Override
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);

    createActions();
    createToolbar();
  }

  private void createActions()
  {
    _saveCVSAction = new Action() {
      @Override
      public void run()
      {

        ISessionData sessionData = getSelectedSessionData();
        if (sessionData == null) return;
        ISession session = sessionData.getSession();
        IPath path = _lastSaveTo;

        if (path == null)
          if (session instanceof ILocalSession)
            path = new Path(((ILocalSession) session).getWorkingDirectory()
                .getRawPath());
          else
            path = ResourcesPlugin.getWorkspace().getRoot().getLocation();

        FileDialog dialog = new FileDialog(Display.getDefault()
            .getActiveShell(), SWT.SAVE);
        dialog.setText("Save to CSV");
        dialog.setFilterPath(path.toString());

        String fileName = dialog.open();
        if (fileName == null || fileName.length() == 0) return;

        if (!fileName.endsWith(".csv")) fileName += ".csv";

        _lastSaveTo = new Path(fileName);
        _lastSaveTo = _lastSaveTo.removeLastSegments(1);

        // ((ModelProbeContainer) selected.getControl()).saveCSV(fileName);
        // saveData(
        // ((AbstractProbeContainer) getSelectedTab().getControl())
        // .getProbeData(),
        // fileName);
      }
    };

    _saveCVSAction.setText("Save Data");
    _saveCVSAction.setToolTipText("Save to data csv");
    _saveCVSAction.setImageDescriptor(JACTRImages
        .getImageDescriptor(JACTRImages.LOG_SAVE));
    _saveCVSAction.setEnabled(false);

    _filterAction = new Action("Filter") {
      @SuppressWarnings("rawtypes")
      @Override
      public void run()
      {
        /*
         * 
         */
        CTabItem item = getSelectedTab();

        if (item == null) return;

        AbstractProbeContainer pc = (AbstractProbeContainer) item.getControl();

        Set<String> probes = pc.getProbeNames();
        Set<String> filtered = pc.getFilteredProbes(new TreeSet<String>());

        Set<String> unfiltered = new TreeSet<String>(probes);
        unfiltered.removeAll(filtered);

        ListSelectionDialog lsd = new ListSelectionDialog(getViewSite()
            .getShell(), probes.toArray(), new ArrayContentProvider(),
            new LabelProvider(), "Select probes to show");

        lsd.setInitialSelections(unfiltered.toArray());

        if (lsd.open() == Window.OK)
        {
          unfiltered.clear();

          for (Object probeName : lsd.getResult())
            unfiltered.add(probeName.toString());

          filtered.clear();
          filtered.addAll(probes);
          filtered.removeAll(unfiltered);
          pc.setFilteredProbes(filtered);

          Set<String> filteredOut = _filteredProbes.get(item.getText());
          if (filteredOut == null)
            _filteredProbes.put(item.getText(), filtered);
          else
          {
            filteredOut.clear();
            filteredOut.addAll(filtered);
          }
        }
      }
    };

    _filterAction.setText("Filter");
    _filterAction.setToolTipText("Select which probes to display");
    _filterAction.setImageDescriptor(JACTRImages
        .getImageDescriptor(JACTRImages.BASIC_FILTER));
    _filterAction.setEnabled(true);
  }

  protected void saveData(ModelProbeData probeData, String fileName)
  {
    try
    {
      FileWriter fw = new FileWriter(fileName);
      double[] samples = probeData.getSampleTimes(null);
      StringBuilder sb = new StringBuilder("time");
      for (double sampleTime : samples)
        sb.append(",").append(String.format("%.2f", sampleTime));

      fw.write(sb.toString());
      fw.write("\n");

      Set<String> probes = new TreeSet<String>();
      probeData.getProbeNames(probes);

      for (String probe : probes)
      {
        sb.delete(0, sb.length());
        sb.append(probe);
        samples = probeData.getProbeData(probe, samples);

        for (double sample : samples)
          sb.append(",").append(
              Double.isNaN(sample) ? "" : String.format("%.3f", sample));

        fw.write(sb.toString());
        fw.write("\n");
        fw.flush();
      }

      fw.close();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("ModelProbeView.saveData threw FileNotFoundException : ", e);
    }

  }

  private void createToolbar()
  {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(new Separator());
    mgr.add(_saveCVSAction);
    mgr.add(_filterAction);
  }

  @Override
  protected void modifyActionAvailability()
  {
    super.modifyActionAvailability();

    ISessionData sessionData = getSelectedSessionData();
    if (sessionData == null) return;

    _saveCVSAction.setEnabled(true);

    _filterAction.setEnabled(true);

    // refresh();
  }

  @Override
  protected Composite createModelComposite(String modelName, Object modelData,
      Composite parent)
  {
    ISessionData sessionData = (ISessionData) modelData;

    IModelProbeSessionDataStream lsds = (IModelProbeSessionDataStream) sessionData
        .getDataStream("probe");

    /*
     * we can't be sure this session will get provide this data, so we defer the
     * add until we have the data or the session is done.
     */
    if (lsds == null || lsds.getRoot() == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s DataStream : %s. Root : %s", lsds,
            modelName,
            lsds != null ? lsds.getRoot() : null));

      if (sessionData.isOpen())
      {
        if (!wasDeferred(modelData))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Deferring add of %s", modelName));
          deferAdd(modelName, modelData, 500);
        }
        else
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s was deferred, retrying", modelName));
          deferAdd(modelName, modelData, 1000);
        }
      }
      else
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("SessionData is closed for %s, ignoring",
              modelName));
        removeDeferred(modelData);
      }
      return null;
    }

    removeDeferred(modelData);

    final XYGraphProbeContainer container = new XYGraphProbeContainer(parent,
        lsds.getRoot());


    _installedContainers.put(sessionData, container);

    // modelName.substring(0, modelName.lastIndexOf('.'));

    /*
     * if we've already got filters, apply them
     */
    Set<String> filteredOut = _filteredProbes.get(modelName);
    if (filteredOut != null) container.setFilteredProbes(filteredOut);

    if (lsds instanceof ILiveSessionDataStream)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Is a live stream, listening"));

      ILiveSessionDataStreamListener<ModelProbeData2> listener = new ILiveSessionDataStreamListener<ModelProbeData2>() {

        public void dataChanged(ILiveSessionDataStream stream,
            Collection<ModelProbeData2> added,
            Collection<ModelProbeData2> modified,
            Collection<ModelProbeData2> removed)
        {
          container.refresh();
        }

      };

      ((ILiveSessionDataStream) lsds).addListener(listener, null);

      container.setData("liveSessionListener", listener);
      container.setData("sessionData", sessionData);

      // force display update, just incase all the data
      // comes in before we are finished building - and therefor get no updates
      container.refresh();
    }

    return container;
  }


  @Override
  protected void disposeModelComposite(String modelName, Object modelData,
      Composite content)
  {
    content.dispose();
    _installedContainers.remove(modelData);
    // _installedMarkerSupport.remove(modelData);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void tabSelected(CTabItem item)
  {
    super.tabSelected(item);
    if (item == null || item.isDisposed()) return;

    AbstractProbeContainer pc = (AbstractProbeContainer) item.getControl();
    pc.refresh();
  }

  @Override
  protected void newSessionData(ISessionData sessionData)
  {
    deferAdd(sessionData.getModelName(), sessionData, 250);
  }

  @Override
  protected void newSessionDataStream(ISessionData sessionData,
      ISessionDataStream sessionDataStream)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("NewDataStream for session %s, %s",
          sessionData, sessionDataStream.getClass().getSimpleName()));

    // if (_installedMarkerSupport.get(sessionData) == null
    // && sessionDataStream instanceof MarkerSessionDataStream)
    // {
    // /*
    // * probe data can arrive across many different ISessionData's (one for
    // * each unique set of probes), but markers are stored in the primary
    // * ISessionData for the named model. So we need to look at all of the
    // * probe/ISessionDatas looking for a match to the session
    // */
    //
    // String modelRootName = sessionData.getModelName();
    //
    // for (Map.Entry<ISessionData, AbstractProbeContainer> entry :
    // _installedContainers
    // .entrySet())
    // if (entry.getKey().getModelName().startsWith(modelRootName + "."))
    // {
    // if (LOGGER.isDebugEnabled())
    // LOGGER.debug(String.format("Installing marker support"));
    // entry.getValue();
    //
    // // MarkerSupport support = new MarkerSupport(apc,
    // // (MarkerSessionDataStream) sessionDataStream, RuntimePlugin
    // // .getDefault().getPreferenceStore()
    // // .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW));
    // //
    // // _installedMarkerSupport.put(entry.getKey(), support);
    // }
    // }
  }

}
