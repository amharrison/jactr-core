/*
 * Created on Jun 9, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.runtime.debug;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.jactr.core.runtime.controller.debug.BreakpointType;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.elements.ACTRDebugElement;
import org.jactr.eclipse.runtime.debug.elements.ACTRThread;
import org.jactr.eclipse.runtime.debug.handlers.BreakpointMessageHandler;
import org.jactr.eclipse.runtime.debug.handlers.LoginMessageHandler;
import org.jactr.eclipse.runtime.debug.handlers.ModelStateMessageHandler;
import org.jactr.eclipse.runtime.debug.handlers.RuntimeStateMessageHandler;
import org.jactr.eclipse.runtime.debug.listener.ProceduralTraceListener;
import org.jactr.eclipse.runtime.debug.marker.ACTRBreakpoint;
import org.jactr.eclipse.runtime.debug.marker.IDisableProductionMarker;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.ISessionListener;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.tools.async.message.event.data.BreakpointReachedEvent;
import org.jactr.tools.async.message.event.login.LoginAcknowledgedMessage;
import org.jactr.tools.async.message.event.state.ModelStateEvent;
import org.jactr.tools.async.message.event.state.RuntimeStateEvent;
import org.jactr.tools.async.shadow.ShadowController;

public class ACTRDebugTarget extends ACTRDebugElement implements IDebugTarget
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER     = LogFactory
                                                    .getLog(ACTRDebugTarget.class);

  ILaunch                            _launch;

  ACTRSession                        _client;

  Map<String, ACTRThread>            _threads;

  volatile boolean                   _inStartUp = true;

  ProceduralTraceListener            _procTraceListener;

  public ACTRDebugTarget(ACTRSession client)
  {
    _client = client;
    _launch = _client.getLaunch();

    _threads = new HashMap<String, ACTRThread>();
    _procTraceListener = new ProceduralTraceListener(this);

    /*
     * we need to install various listeners..
     */
    installListeners();

    setDebugTarget(this);

    DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);

    client.getSession().addListener(new ISessionListener() {

      @Override
      public void sessionClosed(ISession session)
      {
        /*
         * disconnect our listeners
         */
        DebugPlugin.getDefault().getBreakpointManager()
            .removeBreakpointListener(ACTRDebugTarget.this);
        RuntimePlugin.getDefault().getRuntimeTraceManager()
            .removeListener(_procTraceListener);
      }

      @Override
      public void sessionDestroyed(ISession session)
      {

      }

      @Override
      public void newSessionData(ISessionData sessionData)
      {

      }

      @Override
      public void newSessionDataStream(ISessionData sessionData,
          ISessionDataStream sessionDataStream)
      {

      }

    }, null);
  }

  public ACTRSession getACTRSession()
  {
    return _client;
  }

  public String getName() throws DebugException
  {
    return "Debug ACTRRuntime";
  }

  public IProcess getProcess()
  {
    return null;
  }

  @Override
  public ILaunch getLaunch()
  {
    return _launch;
  }

  @Override
  public IDebugTarget getDebugTarget()
  {
    return this;
  }

  public IThread[] getThreads() throws DebugException
  {
    return _threads.values().toArray(new IThread[0]);
  }

  public boolean hasThreads() throws DebugException
  {
    return _threads.size() > 0;
  }

  public boolean supportsBreakpoint(IBreakpoint breakpoint)
  {
    if (breakpoint.getModelIdentifier().equals(getModelIdentifier())) try
    {
      // snag all the model files running in this launch
      ILaunchConfiguration launchConfig = getLaunch().getLaunchConfiguration();

      Collection<IResource> modelFiles = ACTRLaunchConfigurationUtils
          .getModelFiles(launchConfig);
      // get the resource associated with this break point
      IMarker marker = breakpoint.getMarker();
      if (marker != null)
        for (IResource modelFile : modelFiles)
          if (modelFile.equals(marker.getResource())
              && marker.getResource().exists()) return true;
    }
    catch (CoreException ce)
    {

    }
    return false;
  }

  public boolean canTerminate()
  {
    if (!_inStartUp)
    {
      ShadowController shadow = _client.getShadowController();
      return shadow.isConnected() && shadow.isRunning();
    }
    else
      return false;
  }

  public boolean isTerminated()
  {
    if (_inStartUp) return false;

    ShadowController shadow = _client.getShadowController();
    return !(shadow.isConnected() && shadow.isRunning());
  }

  public void terminate() throws DebugException
  {
    // send the termination signal
    try
    {
      _client.stop();
    }
    catch (CoreException e)
    {
      LOGGER.error(e);
      throw new DebugException(e.getStatus());
    }
  }

  public boolean canResume()
  {
    ShadowController shadow = _client.getShadowController();
    if (shadow.isRunning() && !_inStartUp)
      return shadow.getSuspendedModels().size() != 0;
    return false;
  }

  public boolean canSuspend()
  {
    ShadowController shadow = _client.getShadowController();
    if (shadow.isRunning() && !_inStartUp)
      return !isSuspended();
    else
      return false;
  }

  public boolean isSuspended()
  {
    ShadowController shadow = _client.getShadowController();
    if (!_inStartUp) return shadow.isSuspended();
    return false;
  }

  public void resume() throws DebugException
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Requesting resumption");
    _client.getShadowController().resume();
  }

  public void suspend() throws DebugException
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Requesting suspension");
    _client.getShadowController().suspend();
  }

  public ACTRThread addThread(String modelName)
  {
    ACTRThread thread = new ACTRThread(this, modelName);
    _threads.put(modelName, thread);
    thread.fireCreationEvent();
    return thread;
  }

  public ACTRThread removeThread(String modelName)
  {
    ACTRThread thread = _threads.remove(modelName);
    if (thread != null) thread.setTerminated(true);
    return thread;
  }

  public ACTRThread getThread(String modelName)
  {
    return _threads.get(modelName);
  }

  public void breakpointAdded(IBreakpoint breakpoint)
  {
    if (supportsBreakpoint(breakpoint))
      try
      {
        if (breakpoint.isEnabled())
        {
          ACTRBreakpoint abp = (ACTRBreakpoint) breakpoint;
          String name = abp.getBreakpointName();
          String type = abp.getBreakpointType();
          if (name.length() == 0 || type.length() == 0) return;

          ShadowController service = _client.getShadowController();
          /*
           * we have to add a breakpoint for all the models.. this is not
           * completely correct, but since models can be renamed, we can't be
           * sure we have the correct name..
           */
          if (ACTRBreakpoint.PRODUCTION.equals(type) && service.isConnected())
            service.addBreakpoint(BreakpointType.PRODUCTION, "all", name);
        }
      }
      catch (CoreException ce)
      {

      }
  }

  public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta)
  {
    try
    {
      if (breakpoint.isEnabled())
        breakpointAdded(breakpoint);
      else
        breakpointRemoved(breakpoint, delta);
    }
    catch (CoreException e)
    {
      LOGGER.error("ACTRDebugTarget.breakpointChanged threw CoreException : ",
          e);
    }
  }

  public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta)
  {
    if (supportsBreakpoint(breakpoint))
    {
      ACTRBreakpoint abp = (ACTRBreakpoint) breakpoint;
      String name = abp.getBreakpointName();
      String type = abp.getBreakpointType();
      if (name.length() == 0 || type.length() == 0) return;

      ShadowController service = _client.getShadowController();
      // we cant be sure of the model name, so we dont bother setting it
      // this will set the break point for all models that have this
      // named production
      if (ACTRBreakpoint.PRODUCTION.equals(type)) try
      {
        service.removeBreakpoint(BreakpointType.PRODUCTION, "all", name);
      }
      catch (Exception e)
      {
        // silently consume
      }
    }

  }

  public boolean canDisconnect()
  {
    return false;
  }

  public void disconnect() throws DebugException
  {
    terminate();
  }

  public boolean isDisconnected()
  {
    return !_client.getShadowController().isConnected();
  }

  public IMemoryBlock getMemoryBlock(long startAddress, long length)
      throws DebugException
  {
    return null;
  }

  public boolean supportsStorageRetrieval()
  {
    return false;
  }

  public void installBreakpoints()
  {
    try
    {
      ShadowController service = _client.getShadowController();
      Collection<IResource> modelFiles = ACTRLaunchConfigurationUtils
          .getModelFiles(_launch.getLaunchConfiguration());
      IBreakpoint[] breakpoints = DebugPlugin.getDefault()
          .getBreakpointManager().getBreakpoints(ACTRSession.ACTR_DEBUG_MODEL);
      for (IBreakpoint breakpoint2 : breakpoints)
        if (breakpoint2.isEnabled())
        {
          ACTRBreakpoint breakpoint = (ACTRBreakpoint) breakpoint2;
          String type = breakpoint.getBreakpointType();
          String name = breakpoint.getBreakpointName();
          IResource resource = breakpoint.getMarker().getResource();
          for (IResource modelFile : modelFiles)
            if (resource.equals(modelFile))
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug("installing " + type + " breakpoint " + name);

              // install the break point
              if (ACTRBreakpoint.PRODUCTION.equals(type))
                service.addBreakpoint(BreakpointType.PRODUCTION, "all", name);
            }
        }

      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      /*
       * now let's get all the disable annotations
       */
      for (IResource modelFile : modelFiles)
      {
        ICompilationUnit compilationUnit = CompilationUnitManager
            .acquire(modelFile);
        if (compilationUnit == null) continue;

        Collection<String> aliases = ACTRLaunchConfigurationUtils
            .getModelAliases(modelFile, _launch.getLaunchConfiguration());
        markDisabledProductions(modelFile, aliases);

        /*
         * now for the imports
         */
        for (URI importSource : compilationUnit.getImportSources())
          for (IFile file : root.findFilesForLocationURI(importSource))
            markDisabledProductions(file, aliases);
      }
    }
    catch (CoreException ce)
    {
      LOGGER.error("Could not get model files", ce);
    }

    _inStartUp = false;
  }

  protected void markDisabledProductions(IResource modelFile,
      Collection<String> aliases) throws CoreException
  {
    IMarker[] disabled = modelFile.findMarkers(
        IDisableProductionMarker.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
    for (String alias : aliases)
      for (IMarker marker : disabled)
      {
        String productionName = marker.getAttribute(
            IDisableProductionMarker.PRODUCTION_NAME_ATTR, "");
        if (productionName.length() > 0)
          _client.getShadowController().setProductionEnabled(alias,
              productionName, false);
      }
  }

  protected void installListeners()
  {
    Map<Class<?>, IMessageHandler<?>> handlers = _client.getShadowController()
        .getDefaultHandlers();

    /*
     * we need to add the handler for transformed production events which will
     * handle the conflict res info. remove on session close
     */
    RuntimePlugin.getDefault().getRuntimeTraceManager()
        .addListener(_procTraceListener, null);

    handlers.put(BreakpointReachedEvent.class, new BreakpointMessageHandler(
        this));

    handlers.put(ModelStateEvent.class, new ModelStateMessageHandler(this));

    handlers.put(LoginAcknowledgedMessage.class, new LoginMessageHandler(this));

    /*
     * use our own runtimestate handler to catch the suspend on stop w/ debug
     */
    handlers.put(RuntimeStateEvent.class, new RuntimeStateMessageHandler());
  }

}
