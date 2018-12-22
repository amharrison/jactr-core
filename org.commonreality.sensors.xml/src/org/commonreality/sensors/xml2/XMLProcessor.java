/*
 * Created on May 14, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.sensors.xml2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.request.object.IObjectDataRequest;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.manager.IRequestableObjectManager;
import org.commonreality.object.manager.event.IAfferentListener;
import org.commonreality.object.manager.event.IEfferentListener;
import org.commonreality.sensors.xml2.processor.AbstractProcessor;
import org.commonreality.sensors.xml2.processor.IXMLProcessor;
import org.w3c.dom.Element;

/**
 * @author developer
 */
public class XMLProcessor
{
  /**
   * logger definition
   */
  static private final Log                       LOGGER = LogFactory
                                                            .getLog(XMLProcessor.class);

  private XMLSensor                              _sensor;

  private Collection<IXMLProcessor>              _processors;

  private Map<IIdentifier, Collection<IMessage>> _pendingData;

  public XMLProcessor(XMLSensor sensor)
  {
    _sensor = sensor;
    _processors = new ArrayList<IXMLProcessor>();
    addProcessor(new AbstractProcessor<IAfferentObject, IAfferentListener>() {

      @Override
      protected IRequestableObjectManager<IAfferentObject, IAfferentListener> getRequestableObjectManager(
          XMLSensor sensor)
      {
        return sensor.getAfferentObjectManager();
      }

      @Override
      protected boolean shouldProcess(Element element, IIdentifier agentId)
      {
        String alias = element.getAttribute("alias");
        return getIdentifier(alias, agentId) != null
            || IIdentifier.Type.valueOf(element.getAttribute("type")) == IIdentifier.Type.AFFERENT;
      }
    });

    addProcessor(new AbstractProcessor<IEfferentObject, IEfferentListener>() {

      @Override
      protected IRequestableObjectManager<IEfferentObject, IEfferentListener> getRequestableObjectManager(
          XMLSensor sensor)
      {
        return sensor.getEfferentObjectManager();
      }

      @Override
      protected boolean shouldProcess(Element element, IIdentifier agentId)
      {
        String alias = element.getAttribute("alias");
        return getIdentifier(alias, agentId) != null
            || IIdentifier.Type.valueOf(element.getAttribute("type")) == IIdentifier.Type.EFFERENT;
      }
    });
  }

  public XMLSensor getSensor()
  {
    return _sensor;
  }

  public void addProcessor(IXMLProcessor processor)
  {
    _processors.add(processor);
  }

  public double getTime(Element frame)
  {
    double nextTime = 0;
    /*
     * is time absolute or relative?
     */
    if (frame.hasAttribute("value"))
      nextTime = Double.valueOf(frame.getAttribute("value"));
    else if (frame.hasAttribute("relative"))
      nextTime = _sensor.getClock().getTime()
          + Double.valueOf(frame.getAttribute("relative"));
    else if (frame.hasAttribute("immediate")) nextTime = Double.NaN;

    return nextTime;
  }

  public Collection<IMessage> processFrame(IIdentifier forAgent, Element frame,
      boolean sendDataRequestsNow)
  {
    Collection<IMessage> data = new ArrayList<>();
    for (IXMLProcessor processor : _processors)
      try
      {
        Collection<IMessage> rtnData = processor.process(frame, forAgent,
            _sensor);
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Got " + rtnData.size() + " messages");
        if (sendDataRequestsNow)
        {
          for (IMessage msg : rtnData)
            if (msg instanceof IObjectDataRequest)
              _sensor.send(msg);
            else
              data.add(msg);
        }
        else
          data.addAll(rtnData);
      }
      catch (Exception e)
      {
        /**
         * Error : error
         */
        LOGGER.error("Failed to process frame ", e);
      }
    return data;
  }

  public void sendData(IIdentifier agentId, Collection<IMessage> data)
  {
    // long lastId = 0;
    // String lastType = "";
    for (IMessage message : data)
      // if (lastId > message.getMessageId())
      // LOGGER.error("Invalid sequence. Current message ("
      // + message.getMessageId() + "." + message.getClass().getSimpleName()
      // + ") is less than previous ("+lastId+"."+lastType+")");
      _sensor.send(message);
    // lastId = message.getMessageId();
    // lastType = message.getClass().getSimpleName();


  }
}
