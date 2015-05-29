/*
 * Created on Jul 13, 2005 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * Created on Jul 13, 2005 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.launching.env;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.DumbAgent;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.provider.INetworkingProvider;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.reality.impl.DefaultReality;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.jactr.core.reality.ACTRAgent;
import org.jactr.core.reality.connector.CommonRealityConnector;
import org.jactr.core.reality.connector.LocalConnector;
import org.jactr.core.runtime.controller.DefaultController;
import org.jactr.core.runtime.controller.debug.DebugController;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.InstrumentDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.IterativeListenerDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.RuntimeTracerDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.SensorDescriptor;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.embed.EmbedConnector;
import org.jactr.tools.async.common.NetworkedEndpoint;
import org.jactr.tools.async.controller.RemoteInterface;
import org.jactr.tools.async.iterative.listener.NetworkedIterativeRunListener;
import org.jactr.tools.async.sync.SynchronizationManager;
import org.jactr.tools.tracer.RuntimeTracer;
import org.jactr.tools.tracer.listeners.ProceduralModuleTracer;
import org.jactr.tools.tracer.sinks.NetworkedSink;
import org.jactr.tools.tracer.sinks.trace.ArchivalSink;

public class EnvironmentConfigurator
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER       = LogFactory
                                                      .getLog(EnvironmentConfigurator.class);

  static private final String        NET_PROVIDER = "org.commonreality.netty.NettyNetworkingProvider";

  static public IFile createRuntimeEnvironmentFile(ILaunchConfiguration config,
      String mode, IProgressMonitor monitor) throws CoreException
  {
    /*
     * make sure we have runs/
     */
    IFile configurationFile = null;
    IProject project = ACTRLaunchConfigurationUtils.getProject(config);

    if (config.getAttribute(ACTRLaunchConstants.ATTR_SAVE_RUN, true))
    {
      IFolder runtimeFolder = project.getFolder("runs");

      if (!runtimeFolder.exists()) runtimeFolder.create(true, true, monitor);

      /*
       * create {now}
       */
      Date now = new Date();
      DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
      String name = df.format(now);
      name = name.replace("/", ".");
      IFolder thisRun = runtimeFolder.getFolder(name);

      if (!thisRun.exists()) thisRun.create(true, true, monitor);

      df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
      name = df.format(now).replace(":", ".").replace(" ", "");
      thisRun = thisRun.getFolder(name);

      if (!thisRun.exists()) thisRun.create(true, true, monitor);

      configurationFile = thisRun.getFile("environment.xml");
    }
    else
    {
      // tmp directory and temp file
      IPath workingLocation = project
          .getWorkingLocation(RuntimePlugin.PLUGIN_ID);
      workingLocation.append("/tmp/");
      IFolder tmpLocation = project.getFolder(workingLocation);
      tmpLocation.create(true, true, monitor);
      // TODO this could screw up if you try to run multiple instances at once
      configurationFile = tmpLocation.getFile(project.getName()
          + "-environment.xml");
    }

    return configurationFile;
  }

  /**
   * write the environment file used by org.jactr.jactr to launch this is the
   * ugliest piece of code I have probably ever written
   * 
   * @note should be refactored
   * @param configFile
   * @param configuration
   * @param monitor
   */
  static public void writeEnvironmentConfiguration(IFile environmentFile,
      ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(new BufferedOutputStream(baos));

    IProgressMonitor sub = null;
    if (monitor != null)
    {
      sub = new SubProgressMonitor(monitor, 3);
      sub.setTaskName("Writing environment file");
    }
    else
      sub = new NullProgressMonitor();

    try
    {

      Map<IResource, Collection<String>> models = getModelInfo(configuration);
      Map<SensorDescriptor, Map<String, String>> interfaces = getSensorInfo(configuration);

      /*
       * Ok, let's do some output
       */

      if (sub != null) sub.worked(1);

      pw.println("<!-- automatically generated environment.xml file -->");
      pw.println("<environment>");

      setupCommonReality(pw, interfaces, models, configuration);

      /*
       * we only set the controller, which will send back events (log and
       * others) if we've got a port and address
       */
      int port = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_PORT, -1);
      String address = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_ADDRESS, (String) null);
      String credentials = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_CREDENTIALS, (String) null);

      /**
       * will we be listening to this execution?
       */
      boolean attachedRun = port != -1 && address != null
          && credentials != null;

      int iterations = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_ITERATIONS, 0);
      boolean isIterative = iterations != 0;

      setupBasics(pw, models, interfaces.size() != 0, !isIterative,
          configuration);

      pw.println("<attachments>");

      setupInstruments(pw, configuration);

      if (attachedRun && !isIterative)
        setupNormalAttachedRun(pw, credentials, address, port, mode,
            configuration);

      pw.println("</attachments>");

      if (isIterative)
        setupIterativeAttachedRun(pw, credentials, address, port, iterations,
            mode, configuration);

      pw.println("</environment>");
      pw.flush();
      pw.close();

      if (sub != null) sub.worked(1);

      // now we generate it to the appropriate file..
      environmentFile.create(new ByteArrayInputStream(baos.toByteArray()),
          true, monitor);

      if (sub != null)
      {
        sub.worked(1);
        sub.done();
      }

    }
    catch (CoreException e)
    {
      CorePlugin.error("Could not generate the environment config file "
          + environmentFile.getProjectRelativePath(), e);
    }
  }

  /**
   * extract the model information
   * 
   * @param configuration
   * @return
   * @throws CoreException
   */
  static protected Map<IResource, Collection<String>> getModelInfo(
      ILaunchConfiguration configuration) throws CoreException
  {
    /*
     * now for each model defined in the configuration, snag the number of
     * aliases (usually 1) and snags the specific alias
     * {ACTR_MODEL_FILE}.numberOfInstances # {ACTR_MODEL_FILE}.#.instanceAlias
     * String
     */
    Map<IResource, Collection<String>> models = new HashMap<IResource, Collection<String>>();

    for (IResource modelFile : ACTRLaunchConfigurationUtils
        .getModelFiles(configuration))
      if (modelFile.exists())
        models.put(modelFile, ACTRLaunchConfigurationUtils.getModelAliases(
            modelFile, configuration));
    return models;
  }

  /**
   * get the common reality sensor info, but only if not using embed
   * 
   * @param configuration
   * @return
   * @throws CoreException
   */
  static protected Map<SensorDescriptor, Map<String, String>> getSensorInfo(
      ILaunchConfiguration configuration) throws CoreException
  {
    boolean usingEmbed = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER, false);
    /*
     * now we snag all the reality interfaces
     */
    Map<SensorDescriptor, Map<String, String>> interfaces = new HashMap<SensorDescriptor, Map<String, String>>();

    if (!usingEmbed)
      for (SensorDescriptor sensor : ACTRLaunchConfigurationUtils
          .getRequiredSensors(configuration))
      {
        /*
         * we need the parameters for the sensor
         */
        Map<String, String> parameters = new TreeMap<String, String>();
        parameters = configuration.getAttribute(
            ACTRLaunchConstants.ATTR_PARAMETERS + sensor.getClassName(),
            parameters);

        interfaces.put(sensor, parameters);
      }
    return interfaces;
  }

  /**
   * write out the common reality section
   * 
   * @param pw
   * @param interfaces
   */
  static protected void setupCommonReality(PrintWriter pw,
      Map<SensorDescriptor, Map<String, String>> interfaces,
      Map<IResource, Collection<String>> models, ILaunchConfiguration config)
      throws CoreException
  {

    String protocol = INetworkingProvider.NOOP_PROTOCOL;
    String transport = INetworkingProvider.NOOP_TRANSPORT;

    if (interfaces.size() != 0)
    {
      pw.println("<!-- common reality configuration -->");
      pw.println("<commonreality>");
      pw.println("  <reality class=\"org.commonreality.reality.impl.DefaultReality\">");
      pw.println("    <!-- define all the connections -->");
      pw.println("    <services>");
      pw.println(String.format("     <server provider=\"%s\"", NET_PROVIDER));
      pw.println(String.format("             transport=\"%s\"", transport));
      pw.println(String.format("             protocol=\"%s\"", protocol));
      pw.println("             address=\"4567\"/>");
      pw.println("    </services>");
      pw.println("    <!-- define the credentials necessary to connect -->");
      pw.println("    <credentials>");

      for (SensorDescriptor desc : interfaces.keySet())
      {
        pw.print("     <credential value=\"" + desc.getName() + ":1234\"");
        if (desc.isClockOwner()) pw.print(" clock-owner=\"true\"");
        pw.println("/>");
      }

      if (models.size() == 0
          && config.getAttribute(ACTRLaunchConstants.INCLUDE_MOCK_AGENT, false))
        pw.println("     <credential value=\"mockAgent:1234\"/>");
      else
        for (Collection<String> modelNames : models.values())
          for (String modelName : modelNames)
            pw.println("     <credential value=\"" + modelName + ":1234\"/>");

      pw.println("    </credentials>");

      int ackTime = config.getAttribute(
          ACTRLaunchConstants.ATTR_COMMON_REALITY_ACK_TIME, 10000);
      boolean disconnect = config.getAttribute(
          ACTRLaunchConstants.ATTR_COMMON_REALITY_DISCONNECT, false);

      pw.println("    <property name=\"" + DefaultReality.ACK_TIMEOUT_PARAM
          + "\" value=\"" + ackTime + "\"/>");
      pw.println("    <property name=\"" + DefaultReality.DISCONNECT_PARAM
          + "\" value=\"" + disconnect + "\"/>");

      pw.println("  </reality>");

      /*
       * now for the sensors
       */
      pw.println("  <sensors>");
      for (SensorDescriptor sensor : interfaces.keySet())
      {
        Map<String, String> parameters = interfaces.get(sensor);
        pw.println("  <sensor class=\"" + sensor.getClassName() + "\">");
        pw.println("    <credential value=\"" + sensor.getName() + ":1234\"/>");
        pw.println("    <services>");
        pw.println(String.format("     <client provider=\"%s\"", NET_PROVIDER));
        pw.println(String.format("             transport=\"%s\"", transport));
        pw.println(String.format("             protocol=\"%s\"", protocol));
        pw.println("             address=\"4567\"/>");
        pw.println("    </services>");
        for (Map.Entry<String, String> entry : parameters.entrySet())
          pw.println("    <property name=\"" + entry.getKey() + "\" value=\""
              + entry.getValue() + "\"/>");
        pw.println("  </sensor>");
      }
      pw.println("</sensors>");

      /*
       * and the agents
       */
      pw.println("  <agents>");

      if (models.size() == 0
          && config.getAttribute(ACTRLaunchConstants.INCLUDE_MOCK_AGENT, false))
      {
        pw.println("  <agent class=\"" + DumbAgent.class.getName() + "\">");
        pw.println("    <credential value=\"mockAgent:1234\"/>");
        pw.println("    <services>");
        pw.println(String.format("     <client provider=\"%s\"", NET_PROVIDER));
        pw.println(String.format("             transport=\"%s\"", transport));
        pw.println(String.format("             protocol=\"%s\"", protocol));
        pw.println("             address=\"4567\"/>");
        pw.println("    </services>");
        pw.println("  </agent>");
      }
      else
        for (Collection<String> modelNames : models.values())
          for (String modelName : modelNames)
          {
            pw.println("  <agent class=\"" + ACTRAgent.class.getName() + "\">");
            pw.println("    <credential value=\"" + modelName + ":1234\"/>");
            pw.println("    <services>");
            pw.println(String.format("     <client provider=\"%s\"",
                NET_PROVIDER));
            pw.println(String
                .format("             transport=\"%s\"", transport));
            pw.println(String.format("             protocol=\"%s\"", protocol));
            pw.println("             address=\"4567\"/>");
            pw.println("    </services>");
            pw.println("    <property name=\"ACTRAgent.ModelName\" value=\""
                + modelName + "\"/>");
            pw.println("  </agent>");
          }
      pw.println("</agents>");

      pw.println("</commonreality>");
    }
  }

  static protected void setupBasics(PrintWriter pw,
      Map<IResource, Collection<String>> models, boolean needsCommonReality,
      boolean useDebugController, ILaunchConfiguration configuration)
      throws CoreException
  {
    pw.println("<!-- jactr controller configuration -->");

    pw.println("<controller class=\""
        + (useDebugController ? DebugController.class.getName()
            : DefaultController.class.getName()) + "\" />");

    boolean useEmbed = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER, false);

    /*
     * now we need to set up the connector
     */
    String connector = LocalConnector.class.getName();
    pw.println("<!-- Connector specifies how we interface with common reality, if at all -->");
    if (needsCommonReality)
      connector = CommonRealityConnector.class.getName();
    else if (useEmbed) connector = EmbedConnector.class.getName();

    pw.println("<connector class=\"" + connector
          + "\"/>");

    /*
     * onStart and onStop <onStart class="" /> <outStop class="" />
     */
    String os = configuration.getAttribute(ACTRLaunchConstants.ATTR_ON_START,
        "");

    if (os.length() != 0)
    {
      pw.println("  <!-- will execute runnable when the runtime starts -->");
      pw.println("  <onstart class=\"" + os + "\"/>");
    }

    os = configuration.getAttribute(ACTRLaunchConstants.ATTR_ON_STOP, "");
    if (os.length() != 0)
    {
      pw.println("  <!-- will execute runnable when the runtime stops -->");
      pw.println("  <onstop class=\"" + os + "\"/>");
    }

    pw.println(" <models>");
    for (IResource modelFile : models.keySet())
    {
      Collection<String> aliases = models.get(modelFile);
      String urlString = null;

      urlString = modelFile.getProjectRelativePath().removeFirstSegments(1)
          .toString();
      // urlString = modelFile.getLocation().toFile().toURL().toString();
      for (String alias : aliases)
        pw.println("  <model url=\"" + urlString + "\" alias=\"" + alias
            + "\"/>");
    }

    pw.println(" </models>");
  }

  static protected void setupInstruments(PrintWriter pw,
      ILaunchConfiguration configuration) throws CoreException
  {

    /*
     * now we attach the instruments
     */
    for (InstrumentDescriptor instrument : ACTRLaunchConfigurationUtils
        .getRequiredInstruments(configuration))
    {

      /*
       * now we extract each parameter name/value for said interface
       */

      Map<String, String> parameters = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_PARAMETERS + instrument.getClassName(),
          new TreeMap<String, String>());

      String attach = parameters.remove("attach");
      if (attach == null) attach = "all";

      StringBuilder inst = new StringBuilder(" <attachment class=\"");
      inst.append(instrument.getClassName()).append("\" attach=\"");
      inst.append(attach).append("\">\n");

      if (parameters.size() != 0)
      {
        inst.append(" <parameters>\n");
        for (Map.Entry<String, String> parameter : parameters.entrySet())
          inst.append("  <parameter name=\"").append(parameter.getKey())
              .append("\" value=\"").append(parameter.getValue())
              .append("\"/>\n");
        inst.append(" </parameters>\n");
      }

      inst.append("</attachment>");
      pw.println(inst.toString());
    }
  }

  @SuppressWarnings("unchecked")
  static private void setupRuntimeTracers(PrintWriter pw, String credentials,
      String address, int port, String mode, boolean isIterative,
      ILaunchConfiguration config) throws CoreException
  {
    boolean isDebug = mode.equals(ILaunchManager.DEBUG_MODE);

    boolean useNetworkSync = config.getAttribute(
        ACTRLaunchConstants.ATTR_IDE_TRACE, false);
    boolean useArchivalSync = config.getAttribute(
        ACTRLaunchConstants.ATTR_RECORD_TRACE, false);

    /*
     * if iterative run, no sync is permitted - unless it is archival. iterative
     * normal : only archival, if selected. iterative debug : only archival, if
     * selected. normal : selected debug : +networked
     */
    if (isIterative)
      useNetworkSync = false;
    else
      useNetworkSync ^= isDebug; // we force it on

    // not using any? leave
    if (!useArchivalSync && !useNetworkSync) return;

    Map<String, Map<String, String>> traceListeners = new TreeMap<String, Map<String, String>>();
    Collection<RuntimeTracerDescriptor> selectedTraceListeners = ACTRLaunchConfigurationUtils
        .getRequiredTracers(config);

    selectedTraceListeners.forEach((rtd) -> {
      try
      {
        traceListeners.put(rtd.getClassName(), config.getAttribute(
            ACTRLaunchConstants.ATTR_PARAMETERS + rtd.getClassName(),
            Collections.EMPTY_MAP));
      }
      catch (Exception e)
      {
        LOGGER.error("EnvironmentConfigurator. threw Exception : ", e);
      }
    });

    if (isDebug && !isIterative) // we must be using network sync..
      traceListeners.put(ProceduralModuleTracer.class.getName(),
          Collections.EMPTY_MAP);

    if (traceListeners.size() > 0)
    {
      /*
       * now we actually do the writing. If there are
       */
      pw.println("<!-- and this routes events like log and production firing over the network -->");
      pw.println(" <attachment class=\"" + RuntimeTracer.class.getName()
          + "\" attach=\"all\">");

      pw.println("  <parameters>");
      pw.println("   <parameter name=\"" + RuntimeTracer.EXECUTOR_PARAM
          + "\" value=\"Background\"/>");

      Collection<String> sinks = new ArrayList<String>();
      if (useArchivalSync) sinks.add(ArchivalSink.class.getName());
      if (useNetworkSync) sinks.add(NetworkedSink.class.getName());

      pw.println("   <parameter name=\"" + RuntimeTracer.SINK_CLASS
          + "\" value=\"" + sinks.stream().collect(Collectors.joining(","))
          + "\"/>");
      pw.println("   <parameter name=\"" + RuntimeTracer.LISTENERS
          + "\" value=\""
          + traceListeners.keySet().stream().collect(Collectors.joining(","))
          + "\"/>");

      traceListeners.entrySet().forEach(
          (e) -> {
            Map<String, String> params = e.getValue();
            if (params.size() > 0)
            {
              pw.println("<!-- parameters for " + e.getKey() + "-->");
              params.entrySet().forEach(
                  (pe) -> {
                    pw.println("   <parameter name=\"" + pe.getKey()
                        + "\" value=\"" + pe.getValue() + "\"/>");
                  });
            }
          });

      pw.println("  </parameters>");
      pw.println(" </attachment>");

      if (useNetworkSync)
      {
        /*
         * auto install the synch manager..
         */
        pw.println("<!-- since we are connecting to the IDE, we'll auto install the sync tool -->");
        pw.println(String.format("<attachment class=\"%s\" />",
            SynchronizationManager.class.getName()));
      }
    }

    // /*
    // * the uses of traces is more complex than the above. Tracers are used if
    // we
    // * are non-iterative debug (which gives us break point control). Tracers
    // are
    // * used there are requested tracers (from the run config), AND at least
    // one
    // * sync
    // */
    //
    // if (useTracer)
    // {
    // pw.println("<!-- and this routes events like log and production firing over the network -->");
    // pw.println(" <attachment class=\"" + RuntimeTracer.class.getName()
    // + "\" attach=\"all\">");
    //
    // pw.println("  <parameters>");
    // pw.println("   <parameter name=\"" + RuntimeTracer.EXECUTOR_PARAM
    // + "\" value=\"Background\"/>");
    //
    // /*
    // * normal runs almost always route to the IDE, but iterative runs never
    // * do. However, iterative runs are allowed to use the ArchivalSink
    // */
    // StringBuilder sinks = new StringBuilder();
    //
    // if (!isIterative
    // && config.getAttribute(ACTRLaunchConstants.ATTR_IDE_TRACE, true))
    // sinks.append(NetworkedSink.class.getName()).append(",");
    //
    // if (config.getAttribute(ACTRLaunchConstants.ATTR_RECORD_TRACE, false))
    // sinks.append(ArchivalSink.class.getName()).append(",");
    //
    // sinks.delete(sinks.length() - 1, sinks.length());
    //
    // pw.println("   <parameter name=\"" + RuntimeTracer.SINK_CLASS
    // + "\" value=\"" + sinks.toString() + "\"/>");
    //
    // Collection<RuntimeTracerDescriptor> traceListeners =
    // ACTRLaunchConfigurationUtils
    // .getRequiredTracers(config);
    // StringBuilder listeners = new StringBuilder();
    // boolean hasDebug = false;
    // for (RuntimeTracerDescriptor listener : traceListeners)
    // {
    // if (!hasDebug
    // && listener.getClassName().equals(
    // ProceduralModuleTracer.class.getName())) hasDebug = true;
    //
    // listeners.append(listener.getClassName()).append(",");
    //
    // Map<String, String> parameters = config.getAttribute(
    // ACTRLaunchConstants.ATTR_PARAMETERS + listener.getClassName(),
    // new TreeMap<String, String>());
    // if (parameters.size() > 0)
    // {
    // pw.println("<!-- params for " + listener.getClassName() + " -->");
    // for (Map.Entry<String, String> parameter : parameters.entrySet())
    // pw.println("   <parameter name=\"" + parameter.getKey()
    // + "\" value=\"" + parameter.getValue() + "\"/>");
    // }
    // }
    //
    // // kill ','
    // if (listeners.length() != 0)
    // listeners.delete(listeners.length() - 1, listeners.length());
    //
    // if (mode.equals(ILaunchManager.DEBUG_MODE) && !hasDebug)
    // {
    // if (listeners.length() != 0) listeners.append(",");
    // listeners.append(ProceduralModuleTracer.class.getName());
    // }
    //
    // pw.println("   <parameter name=\"" + RuntimeTracer.LISTENERS
    // + "\" value=\"" + listeners + "\"/>");
    //
    // pw.println("  </parameters>");
    //
    // pw.println(" </attachment>");
    // }

  }

  static protected void setupNormalAttachedRun(PrintWriter pw,
      String credentials, String address, int port, String mode,
      ILaunchConfiguration config) throws CoreException
  {

    INetworkingProvider provider = null;
    try
    {
      provider = INetworkingProvider.getProvider(NET_PROVIDER);
    }
    catch (Exception e)
    {
      throw new CoreException(new Status(IStatus.ERROR,
          RuntimePlugin.PLUGIN_ID, String.format(
              "Failed to get networking provider %s", NET_PROVIDER), e));
    }

    pw.println("<!-- this attachment sets up the network  communication and control -->");
    pw.println(" <attachment class=\"" + RemoteInterface.class.getName()
        + "\" attach=\"all\">");
    pw.println("   <parameters>");

    /*
     * by default we always use this set up..
     */
    ITransportProvider transport = provider
        .getTransport(INetworkingProvider.NIO_TRANSPORT);
    pw.println(String.format("     <parameter name=\"%s\" value=\"%s\"/>",
        NetworkedEndpoint.TRANSPORT_CLASS, transport.getClass().getName()));

    IProtocolConfiguration proto = provider
        .getProtocol(INetworkingProvider.SERIALIZED_PROTOCOL);
    pw.println(String.format("     <parameter name=\"%s\" value=\"%s\"/>",
        NetworkedEndpoint.PROTOCOL_CLASS, proto.getClass().getName()));

    /*
     * this newClient call might seem expensive the the services don't create
     * state info until they are configured and started.
     */
    pw.println(String.format("     <parameter name=\"%s\" value=\"%s\"/>",
        NetworkedEndpoint.SERVICE_CLASS, provider.newClient().getClass()
            .getName()));

    pw.println("     <parameter name=\"" + NetworkedEndpoint.CREDENTAILS
        + "\" value=\"" + credentials + "\"/>");
    pw.println("     <parameter name=\"" + NetworkedEndpoint.ADDRESS
        + "\" value=\"" + address + ":" + port + "\"/>");
    /*
     * this is generally commented out because it is sooooo expensive.
     */
    pw.println("     <parameter name=\"SendModelOnSuspend\" value=\"false\"/>");

    // pw.println("     <parameter name=\"SendModelOnSuspend\" value=\""
    // + mode.equals(ILaunchManager.DEBUG_MODE) + "\"/>");

    pw.println("   </parameters>");
    pw.println("</attachment>");

    setupRuntimeTracers(pw, credentials, address, port, mode, false, config);
  }

  static protected void setupIterativeAttachedRun(PrintWriter pw,
      String credentials, String address, int port, int iterations,
      String mode, ILaunchConfiguration configuration) throws CoreException
  {
    setupRuntimeTracers(pw, credentials, address, port, mode, true,
        configuration);

    int deadlockWaitTime = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_ITERATIVE_DEADLOCK_TIMEOUT, 10000);
    pw.println("<!-- this is an iterative run -->");
    pw.println(" <iterative iterations=\"" + iterations + "\">");
    /*
     * we always install this guy to track what's going on..
     */
    pw.println("   <iterative-listener class=\""
        + NetworkedIterativeRunListener.class.getName() + "\">");
    pw.println("    <parameters>");
    pw.println("     <parameter name=\"address\" value=\"" + address + ":"
        + port + "\"/>");
    pw.println("     <parameter name=\"credentials\" value=\"" + credentials
        + "\"/>");
    pw.println("     <parameter name=\""
        + NetworkedIterativeRunListener.DEADLOCK_TIMEOUT_PARAM + "\" value=\""
        + deadlockWaitTime + "\"/>");
    pw.println("    </parameters>");
    pw.println("   </iterative-listener>");

    for (IterativeListenerDescriptor desc : ACTRLaunchConfigurationUtils
        .getRequiredListeners(configuration))
    {
      StringBuilder sb = new StringBuilder("   <iterative-listener class=\"");
      sb.append(desc.getClassName()).append("\">\n");

      /*
       * now we extract each parameter name/value for said interface
       */

      Map<String, String> parameters = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_PARAMETERS + desc.getClassName(),
          new TreeMap<String, String>());

      if (parameters.size() != 0)
      {
        sb.append("    <parameters>\n");
        for (Map.Entry<String, String> parameter : parameters.entrySet())
          sb.append("     <parameter name=\"").append(parameter.getKey())
              .append("\" value=\"").append(parameter.getValue())
              .append("\"/>\n");
        sb.append("    </parameters>\n");
      }
      sb.append("   </iterative-listener>\n");

      pw.println(sb.toString());
    }

    pw.println(" </iterative>");
  }
}
