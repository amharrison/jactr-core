package org.jactr.eclipse.runtime.playback;

/*
 * default logging
 */
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.jactr.eclipse.runtime.playback.internal.ArchivalIndex;
import org.jactr.eclipse.runtime.playback.internal.ArchiveController;
import org.jactr.eclipse.runtime.playback.internal.EventPumper;
import org.jactr.eclipse.runtime.session.impl.AbstractSession;

public class SessionArchive extends AbstractSession
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SessionArchive.class);

  private ArchivalIndex              _index;

  private EventPumper                _eventPumper;

  private ArchiveController          _controller;

  public SessionArchive(IResource resouce)
  {
    _index = new ArchivalIndex(resouce);
    _index.open();
    // circular references..
    _eventPumper = new EventPumper("Session Replay", this);
    _controller = new ArchiveController(this, _index, _eventPumper);
    _eventPumper.setController(_controller);
  }

  public ArchivalIndex getIndex()
  {
    return _index;
  }

  @Override
  public ArchiveController getController()
  {
    return _controller;
  }

  @Override
  public boolean isOpen()
  {
    boolean superOpen = super.isOpen();
    boolean currentLessThanEnd = _controller.getCurrentTime() < _index
        .getEndTime();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "superOpen:%s currentLessThan:%s (%.2f / %.2f)", superOpen,
 currentLessThanEnd, _controller.getCurrentTime(),
              _index.getEndTime()));

    return superOpen && currentLessThanEnd;
  }

  @Override
  protected void closeSession()
  {
    _index.close();
  }

  @Override
  protected void destroySession()
  {
    // TODO Auto-generated method stub

  }

  public Date getTimeOfExecution()
  {
    return new Date(_index.getIndexFile().getLocalTimeStamp());
  }

}
