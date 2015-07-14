package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.jactr.eclipse.runtime.playback.SessionArchive;
import org.jactr.eclipse.runtime.playback.internal.ArchiveController;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;

/**
 * simple listener that detects when an SessionArchive is added, allowing us to
 * find and load the of the marker index file (if it exists)
 * 
 * @author harrison
 */
public class MarkerIndexSessionListener implements ISessionManagerListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MarkerIndexSessionListener.class);

  public void sessionAdded(ISession session)
  {
    try
    {
      if (session instanceof SessionArchive)
      {
        SessionArchive archive = (SessionArchive) session;
        IResource indexFile = archive.getIndex().getIndexFile();
        IContainer sessionData = indexFile.getParent().getParent();
        IResource markerIndex = sessionData
            .findMember("marker.index");
        IResource markerTypes = sessionData
            .findMember("marker.types");
        MarkerIndex index = new MarkerIndex(markerIndex, markerTypes);

        ((ArchiveController) archive.getController())
            .setRunToContentProvider(new RunToMarkerContentProvider(index));

        // set the metadata so we can always get to the index
        session.setMetaData("marker.index", index);
      }
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format("Failed to install marker index"), e);
    }
  }

  public void sessionRemoved(ISession session)
  {

  }

}
