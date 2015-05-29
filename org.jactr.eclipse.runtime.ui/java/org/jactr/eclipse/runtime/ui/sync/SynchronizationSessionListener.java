package org.jactr.eclipse.runtime.ui.sync;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.session.ISessionInfo;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.impl.Session2SessionAdapter;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;
import org.jactr.eclipse.runtime.ui.UIPlugin;
import org.jactr.tools.async.sync.SynchronizationMessage;

public class SynchronizationSessionListener implements ISessionManagerListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SynchronizationSessionListener.class);

  public void sessionAdded(ISession session)
  {
    ACTRSession actr = getACTRSession(session);
    if (actr != null)
    {
      ISessionInfo<?> netSession = actr.getShadowController()
          .getActiveSession();
      netSession.addHandler(
          SynchronizationMessage.class,
          (s, m) -> synchronizationMessageReceived(s,
              m));
    }

  }

  protected void synchronizationMessageReceived(ISessionInfo<?> session,
      SynchronizationMessage message)
  {
    /*
     * a round about message and response to mark us as having processed the
     * data up to this point.
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Synchronizing"));

    Runnable synch = new Runnable() {
      public void run()
      {
        try
        {
          if (session.isConnected())
            session.write(new SynchronizationMessage(message));
        }
        catch (Exception e)
        {
          LOGGER.error(e);
        }
      }
    };
    // run this on the display thread when we get a chance.
    UIPlugin.getDefault().getWorkbench().getDisplay().asyncExec(synch);
  }

  public void sessionRemoved(ISession session)
  {
    // TODO Auto-generated method stub

  }

  protected ACTRSession getACTRSession(ISession session)
  {
    if (session instanceof Session2SessionAdapter)
    {
      Session2SessionAdapter s2sa = (Session2SessionAdapter) session;
      AbstractSession as = s2sa.getOldSession();
      if (as instanceof ACTRSession)
      {
        ACTRSession actr = (ACTRSession) as;
        return actr;
      }
    }
    return null;
  }

}
