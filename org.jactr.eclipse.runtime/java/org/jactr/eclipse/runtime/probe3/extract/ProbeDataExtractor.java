package org.jactr.eclipse.runtime.probe3.extract;

/*
 * default logging
 */
import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.jactr.core.utils.collections.FastSetFactory;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.playback.SessionArchive;
import org.jactr.eclipse.runtime.probe3.IProbeData;
import org.jactr.eclipse.runtime.probe3.ModelProbeData2;
import org.jactr.eclipse.runtime.probe3.ModelProbeDataStream;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

/**
 * @author harrison
 */
public class ProbeDataExtractor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProbeDataExtractor.class);

  private final SessionArchive                     _archive;
  private IFolder                    _outputDirectory;

  private SessionSpecificModelProbeRuntimeListener _probeListener;

  private final IDebugEventSetListener             _debugListener = new IDebugEventSetListener() {

                                                                    @Override
                                                                    public void handleDebugEvents(
                                                                        DebugEvent[] events)
                                                                    {
                                                                      for (DebugEvent event : events)
                                                                        if (event
                                                                            .getSource() == _archive
                                                                            && event
                                                                                .getKind() == DebugEvent.SUSPEND)
                                                                          cleanUp();
                                                                    }
                                                                  };

  public ProbeDataExtractor(SessionArchive archive)
  {
    _archive = archive;
    _outputDirectory = createFolder(archive);
    _probeListener = new SessionSpecificModelProbeRuntimeListener(archive,
        _outputDirectory);

    // attach the listener
    RuntimePlugin.getDefault().getRuntimeTraceManager()
        .addListener(_probeListener);
  }

  public void extract()
  {
    /*
     * we use Eclipse's debug events to listen for the state of the session, in
     * particular the resume, suspend events. Since we are running fully, we can
     * just listen for the suspend, which will arrive after all the events are
     * pumped out.
     */

    DebugPlugin.getDefault().addDebugEventListener(_debugListener);

    // playback & save to file
    try
    {
      _archive.getController().runFully();
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("ProbeDataExtractor.extract threw Exception : ", e);
    }

  }

  protected void cleanUp()
  {
    DebugPlugin.getDefault().removeDebugEventListener(_debugListener);

    FastSet<String> dataKeys = FastSetFactory.newInstance();

    _archive.getKeys(dataKeys);

    for (String key : dataKeys)
    {
      ISessionData sessionData = _archive.getData(key);
      for (String stream : sessionData.getAvailableStreams())
      {
        ISessionDataStream sds = sessionData.getDataStream(stream);
        if (sds instanceof ModelProbeDataStream)
          flush(((ModelProbeDataStream) sds).getRoot());
      }
    }

    FastSetFactory.recycle(dataKeys);

    _archive.close();
    _archive.destroy();
  }

  private void flush(ModelProbeData2 root)
  {
    FastSet<String> probeNames = FastSetFactory.newInstance();

    root.getProbeNames(probeNames);

    for (String probeName : probeNames)
    {
      IProbeData pd = root.getProbeData(probeName);
      if (pd instanceof OutputProbeData)
      {
        OutputProbeData opd = (OutputProbeData) pd;
        opd.close();
      }
    }

    FastSetFactory.recycle(probeNames);
  }

  protected IFolder createFolder(SessionArchive sa)
  {
    // index file
    IResource resource = sa.getIndex().getIndexFile();

    // sessionNamed folder, containing index file
    IContainer container = resource.getParent();

    // sessionData folder
    IContainer sessionDataFolder = container.getParent();

    // runtime folder
    IContainer rtFolder = sessionDataFolder.getParent();

    IFolder probesFolder = rtFolder.getFolder(new Path("probes/"));
    IFolder extractedFolder = probesFolder.getFolder(new Path(container
        .getName() + "/"));

    extractedFolder.getFullPath().toFile().mkdirs();
    return extractedFolder;
  }

}
