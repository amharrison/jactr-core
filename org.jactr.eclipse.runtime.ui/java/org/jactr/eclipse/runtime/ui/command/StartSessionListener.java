package org.jactr.eclipse.runtime.ui.command;

import java.net.InetSocketAddress;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.launching.remote.ProxyLaunch;
import org.jactr.tools.async.credentials.ICredentials;

public class StartSessionListener extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
      .getLog(StartSessionListener.class);

  public StartSessionListener()
  {
  }

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ACTRSession session = new ACTRSession(new ProxyLaunch(), null);

    try
    {
      session.start();

      InetSocketAddress listeningTo = session.getConnectionAddress();
      ICredentials credentials = session.getCredentials();

      MessageDialog.openInformation(null, "Listening for models",
          String.format("Listening on %s:%d (%s)", listeningTo.getHostName(),
              listeningTo.getPort(), credentials.toString()));

    }
    catch (CoreException e)
    {

    }
    return null;
  }

}
