package org.jactr.eclipse.runtime.launching.cr;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.jactr.eclipse.runtime.launching.norm.AbstractACTRLaunchConfiguration;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.launching.session.SessionTracker;

/**
 * launch configuration for lone CR set up
 * 
 * @author harrison
 */
public class CRLaunchConfiguration extends AbstractACTRLaunchConfiguration
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(CRLaunchConfiguration.class);


  @Override
  protected String getLaunchPrefix()
  {
    return "CommonReality";
  }

  @Override
  protected SessionTracker getSessionTracker()
  {
    // no defered support
    return null;
  }

  @Override
  protected AbstractSession startSession(ILaunch launch,
      ILaunchConfigurationWorkingCopy configuration, String mode)
      throws CoreException
  {
    // no session support
    return null;
  }

}
