/*
 * Created on May 10, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.parser;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.net.message.credentials.PlainTextCredentials;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.provider.INetworkingProvider;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.participant.IParticipant;
import org.commonreality.participant.impl.AbstractParticipant;
import org.commonreality.reality.CommonReality;
import org.commonreality.reality.IReality;
import org.commonreality.reality.control.RealitySetup;
import org.commonreality.reality.control.RealityShutdown;
import org.commonreality.sensors.ISensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * parses and creates a running reality interface <code>
 *  <reality>
 *   <credentials>
 *    <credential value=""/>
 *   </credentials>
 *   <sensors>
 *    <sensor class="org.some.instanceof.ISensorImpl">
 *     <credential value=""/>
 *     <property name="" value=""/>
 *    </sensor>
 *   </sensors>
 *  </reality>
 * </code>
 * 
 * @author developer
 */
public class RealityParser
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(RealityParser.class);

  public void parse(InputSource source) throws IOException, SAXException,
      ParserConfigurationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document doc = parser.parse(source);

    parse(doc.getDocumentElement());
  }

  public void parse(Element documentRoot)
  {
    IReality reality = CommonReality.getReality();

    NodeList nl = documentRoot.getElementsByTagName("reality");
    if (nl.getLength() > 1)
      throw new RuntimeException("Must only at most one reality "
          + nl.getLength());
    if (nl.getLength() == 1)
    {
      /*
       * clean up the previous reality if it exists
       */
      if (reality != null) new RealityShutdown(reality, false).run();

      reality = (IReality) create((Element) nl.item(0), "reality");
    }

    nl = documentRoot.getElementsByTagName("sensor");
    Collection<ISensor> sensors = new ArrayList<ISensor>();
    for (int i = 0; i < nl.getLength(); i++)
      sensors.add((ISensor) create((Element) nl.item(i), "sensor"));

    nl = documentRoot.getElementsByTagName("agent");
    Collection<IAgent> agents = new ArrayList<IAgent>();
    for (int i = 0; i < nl.getLength(); i++)
      agents.add((IAgent) create((Element) nl.item(i), "agent"));

    /*
     * configure and initialize the participants
     */
    new RealitySetup(reality, sensors, agents).run();
  }

  protected IParticipant create(Element element, String participantType)
  {
    IParticipant participant = (IParticipant) instantiate(element,
        participantType);

    if (participant instanceof AbstractParticipant)
    {
      NodeList nl = element.getElementsByTagName("services");
      if (nl.getLength() != 1)
        throw new RuntimeException(
            "Only one services child is permitted for sensor");

      addServices((AbstractParticipant) participant, (Element) nl.item(0));
    }
    /*
     * find the credential section
     */
    NodeList nl = element.getElementsByTagName("credential");
    if (nl.getLength() == 0)
      throw new RuntimeException(participantType + " must define credential");
    for (int i = 0; i < nl.getLength(); i++)
    {
      ICredentials creds = createCredentials((Element) nl.item(i));
      if (participant instanceof ISensor)
        ((ISensor) participant).setCredentials(creds);
      else if (participant instanceof IAgent)
        ((IAgent) participant).setCredentials(creds);
      else if (participant instanceof IReality)
      {
        boolean wantsClockControl = Boolean.parseBoolean(((Element) nl.item(i))
            .getAttribute("clock-owner"));
        ((IReality) participant).add(creds, wantsClockControl);
      }
    }
    // handle any properties
    nl = element.getElementsByTagName("property");
    Map<String, String> properties = new TreeMap<String, String>();
    for (int i = 0; i < nl.getLength(); i++)
    {
      Element property = (Element) nl.item(i);
      String name = property.getAttribute("name");
      String value = property.getAttribute("value");
      properties.put(name, value);
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("configuring " + participant + " with " + properties);
    try
    {
      participant.configure(properties);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Could not configure participant "
          + participant + " with " + properties, e);
    }

    return participant;
  }

  protected void addServices(AbstractParticipant participant, Element sElement)
  {
    /*
     * first we get all the server services
     */
    NodeList nl = sElement.getElementsByTagName("server");
    for (int i = 0; i < nl.getLength(); i++)
      try
      {
        Element el = (Element) nl.item(i);
        String provider = el.getAttribute("provider");
        INetworkingProvider netProvider = INetworkingProvider
            .getProvider(provider);

        IServerService service = netProvider.newServer();
        ITransportProvider transport = netProvider.getTransport(el
            .getAttribute("transport"));
        IProtocolConfiguration protocol = netProvider.getProtocol(el
            .getAttribute("protocol"));
        SocketAddress address = transport.createAddress(el
            .getAttribute("address"));
        participant.addServerService(service, transport, protocol, address);
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to instantiate server service :", e);
      }

    nl = sElement.getElementsByTagName("client");
    for (int i = 0; i < nl.getLength(); i++)
      try
      {
        Element el = (Element) nl.item(i);

        String provider = el.getAttribute("provider");
        INetworkingProvider netProvider = INetworkingProvider
            .getProvider(provider);

        IClientService service = netProvider.newClient();
        ITransportProvider transport = netProvider.getTransport(el
            .getAttribute("transport"));
        IProtocolConfiguration protocol = netProvider.getProtocol(el
            .getAttribute("protocol"));

        SocketAddress address = transport.createAddress(el
            .getAttribute("address"));
        participant.addClientService(service, transport, protocol, address);
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to instantiate client service :", e);
      }
  }

  protected ICredentials createCredentials(Element element)
  {
    String value = element.getAttribute("value");
    String[] vals = value.split(":");
    return new PlainTextCredentials(vals[0], vals[1]);
  }

  protected Object instantiate(Element element, String objectType)
  {
    return instantiate(element, "class", objectType);
  }

  protected Object instantiate(Element element, String attribute,
      String objectType)
  {
    String className = element.getAttribute(attribute);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("creating " + objectType + " from " + className);

    try
    {
      Object obj = getClass().getClassLoader().loadClass(className)
          .newInstance();
      return obj;
    }
    catch (Exception e)
    {
      String message = new String("Could not instantiate " + objectType
          + " from " + className);
      throw new RuntimeException(message, e);
    }
  }
}
