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
package org.commonreality.sensors.xml2.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.manager.IRequestableObjectManager;
import org.commonreality.object.manager.event.IObjectListener;
import org.commonreality.sensors.xml2.XMLSensor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author developer
 */
public abstract class AbstractProcessor<O extends ISimulationObject, L extends IObjectListener<O>>
    implements IXMLProcessor
{
  /**
   * logger definition
   */
  static private final Log                           LOGGER = LogFactory
                                                                .getLog(AbstractProcessor.class);

  private Map<IIdentifier, Map<String, IIdentifier>> _aliases;

  private Collection<IMessage>                       _pendingData;

  public AbstractProcessor()
  {
    _aliases = new HashMap<IIdentifier, Map<String, IIdentifier>>();
    _pendingData = new ArrayList<IMessage>();
  }

  /**
   * @see org.commonreality.sensors.xml.processor.IXMLProcessor#process(org.w3c.dom.Element,
   *      org.commonreality.identifier.IIdentifier,
   *      org.commonreality.sensors.xml.XMLSensor)
   */
  public Collection<IMessage> process(Element element, IIdentifier agentID,
      XMLSensor sensor)
  {
    Collection<IMessage> rtn = new ArrayList<IMessage>();
    Collection<IObjectDelta> added = new ArrayList<IObjectDelta>();
    Collection<IIdentifier> addedIds = new ArrayList<IIdentifier>();
    Collection<IObjectDelta> updated = new ArrayList<IObjectDelta>();
    Collection<IIdentifier> updatedIds = new ArrayList<IIdentifier>();
    Collection<IIdentifier> removedIds = new ArrayList<IIdentifier>();

    Collection<NodeList> nodeLists = new ArrayList<NodeList>();
    nodeLists.add(element.getElementsByTagName("add"));
    nodeLists.add(element.getElementsByTagName("update"));
    nodeLists.add(element.getElementsByTagName("remove"));

    for (NodeList nl : nodeLists)
      for (int i = 0; i < nl.getLength(); i++)
      {
        Node child = nl.item(i);
        if (!(child instanceof Element)) continue;

        Element childElement = (Element) child;
        String tagName = childElement.getTagName();

        String forWhichAgent = childElement.getAttribute("for");
        if (forWhichAgent != null && forWhichAgent.length() > 0)
        {
          IAgentObject ao = sensor.getAgentObjectManager().get(agentID);
          String agentName = (String) ao.getProperty("name");
          if (agentName == null)
            LOGGER.error("Agent name was not set for " + agentID);
          else if (!agentName.equals(forWhichAgent))
          {
            if (LOGGER.isDebugEnabled())
              LOGGER
                  .debug(String
                      .format(
                          "agentId %s does not match targeted name %s, skipping processing",
                          agentName,
                  forWhichAgent));
            
            continue;
          }
        }

        /**
         * remove all that match a pattern
         */
        if (tagName.equalsIgnoreCase("remove")
            && childElement.hasAttribute("pattern")
            && _aliases.containsKey(agentID))
        {
          Pattern pattern = Pattern.compile(childElement
              .getAttribute("pattern"));

          Collection<String> aliases = new ArrayList<String>(_aliases.get(
              agentID).keySet());
          for (String alias : aliases)
            if (pattern.matcher(alias).matches())
            {
              IIdentifier objectId = getIdentifier(alias, agentID);
              if (objectId != null)
              {
                removeIdentifier(alias, agentID);
                removedIds.add(objectId);
              }
            }
        }
        else if (shouldProcess(childElement, agentID)
            && childElement.hasAttribute("alias"))
          if (tagName.equalsIgnoreCase("add"))
          {
            IObjectDelta delta = add(childElement, agentID, sensor);
            if (delta != null)
            {
              added.add(delta);
              addedIds.add(delta.getIdentifier());
            }
          }
          else if (tagName.equalsIgnoreCase("update"))
          {
            IObjectDelta delta = update(childElement, agentID, sensor);
            if (delta != null)
            {
              updated.add(delta);
              updatedIds.add(delta.getIdentifier());
            }
          }
          else if (tagName.equalsIgnoreCase("remove"))
          {
            IIdentifier id = remove(childElement, agentID, sensor);
            if (id != null) removedIds.add(id);
          }
      }

    IIdentifier sId = sensor.getIdentifier();
    /*
     * handle all the adds, updates and removes send the data first..
     */
    if (added.size() != 0)
    {
      rtn.add(new ObjectDataRequest(sId, agentID, added));
      rtn.add(new ObjectCommandRequest(sId, agentID, IObjectCommand.Type.ADDED,
          addedIds));
    }

    if (updated.size() != 0)
    {
      rtn.add(new ObjectDataRequest(sId, agentID, updated));
      rtn.add(new ObjectCommandRequest(sId, agentID,
          IObjectCommand.Type.UPDATED, updatedIds));
    }

    if (removedIds.size() != 0)
      rtn.add(new ObjectCommandRequest(sId, agentID,
          IObjectCommand.Type.REMOVED, removedIds));

    return rtn;
  }

  abstract protected boolean shouldProcess(Element element, IIdentifier agentId);

  abstract protected IRequestableObjectManager<O, L> getRequestableObjectManager(
      XMLSensor sensor);

  protected IObjectDelta add(Element element, IIdentifier agentId,
      XMLSensor sensor)
  {
    String alias = element.getAttribute("alias");
    IIdentifier objectId = getIdentifier(alias, agentId);
    if (objectId != null)
      throw new RuntimeException("Already have an object defined for " + alias);

    O object = getRequestableObjectManager(sensor).request(agentId);

    objectId = object.getIdentifier();

    ((BasicIdentifier) objectId).setName(alias);
    DeltaTracker dt = new DeltaTracker(object);
    processContent(dt, element);

    addIdentifier(alias, objectId, agentId);

    return dt.getDelta();
  }

  protected IObjectDelta update(Element element, IIdentifier agentId,
      XMLSensor sensor)
  {
    String alias = element.getAttribute("alias");
    IIdentifier objectId = getIdentifier(alias, agentId);
    if (objectId == null)
      throw new RuntimeException("No object defined for " + alias + alias);

    O object = getRequestableObjectManager(sensor).get(objectId);
    if (object == null)
      throw new RuntimeException("Could not find object for " + objectId);

    DeltaTracker dt = new DeltaTracker(object);
    processContent(dt, element);
    return dt.getDelta();
  }

  protected IIdentifier remove(Element element, IIdentifier agentId,
      XMLSensor sensor)
  {
    String alias = element.getAttribute("alias");
    IIdentifier objectId = getIdentifier(alias, agentId);
    if (objectId == null)
      throw new RuntimeException("No object defined for " + alias + alias);
    removeIdentifier(alias, agentId);
    return objectId;
  }

  protected IIdentifier getIdentifier(String alias, IIdentifier agentId)
  {
    Map<String, IIdentifier> aliases = _aliases.get(agentId);
    if (aliases == null) return null;

    return aliases.get(alias);
  }

  protected void addIdentifier(String alias, IIdentifier id,
      IIdentifier agentIdentifier)
  {
    Map<String, IIdentifier> aliases = _aliases.get(agentIdentifier);
    if (aliases == null)
    {
      aliases = new TreeMap<String, IIdentifier>();
      _aliases.put(agentIdentifier, aliases);
    }
    aliases.put(alias, id);
  }

  protected void removeIdentifier(String alias, IIdentifier agentIdentifier)
  {
    Map<String, IIdentifier> aliases = _aliases.get(agentIdentifier);
    if (aliases == null) return;
    aliases.remove(alias);
    if (aliases.size() == 0) _aliases.remove(agentIdentifier);
  }

  protected void processContent(IMutableObject realObject, Node content)
  {
    if (!(content instanceof Element)) return;
    Element element = (Element) content;
    String tagName = element.getTagName();
    if (tagName.equalsIgnoreCase("double"))
      processDouble(realObject, element);
    else if (tagName.equalsIgnoreCase("doubles"))
      processDoubles(realObject, element);
    else if (tagName.equalsIgnoreCase("string"))
      processString(realObject, element);
    else if (tagName.equalsIgnoreCase("strings"))
      processStrings(realObject, element);
    else if (tagName.equalsIgnoreCase("boolean"))
      processBoolean(realObject, element);
    else if (tagName.equalsIgnoreCase("int"))
      processInt(realObject, element);
    else if (tagName.equalsIgnoreCase("ints"))
      processInts(realObject, element);
    else
    {
      /*
       * process children
       */
      NodeList nl = element.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
        processContent(realObject, nl.item(i));
    }
  }

  protected void processDouble(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    String str = element.getAttribute("value");
    try
    {
      realObject.setProperty(name, Double.valueOf(str));
    }
    catch (NumberFormatException nfe)
    {
      LOGGER.warn(element.getTagName() + "." + name
          + " not properly formatted : " + str);
    }
  }

  protected void processDoubles(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    Collection<Double> doubles = new ArrayList<Double>();
    String[] strings = getStrings(element);
    for (String element2 : strings)
      try
      {
        doubles.add(Double.valueOf(element2));
      }
      catch (NumberFormatException nfe)
      {
        LOGGER.warn(element.getTagName() + "." + name
            + " not properly formatted : " + element2);
      }

    double[] dbls = new double[doubles.size()];
    int i = 0;
    for (Double d : doubles)
      dbls[i++] = d;

    realObject.setProperty(name, dbls);
  }

  protected void processString(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    String value = element.getAttribute("value");
    realObject.setProperty(name, value);
  }

  protected void processStrings(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    realObject.setProperty(name, getStrings(element));
  }

  protected void processBoolean(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    try
    {
      realObject.setProperty(name,
          Boolean.valueOf(element.getAttribute("value")));
    }
    catch (Exception e)
    {
      LOGGER.warn(element.getTagName() + "." + name
          + " not properly formatted : " + element.getAttribute("value"));
    }
  }

  protected void processInt(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    String str = element.getAttribute("value");
    try
    {
      realObject.setProperty(name, Integer.valueOf(str));
    }
    catch (NumberFormatException nfe)
    {
      LOGGER.warn(element.getTagName() + "." + name
          + " not properly formatted : " + str);
    }
  }

  protected void processInts(IMutableObject realObject, Element element)
  {
    String name = element.getAttribute("name");
    Collection<Integer> integers = new ArrayList<Integer>();
    String[] strings = getStrings(element);
    for (String element2 : strings)
      try
      {
        integers.add(Integer.valueOf(element2));
      }
      catch (NumberFormatException nfe)
      {
        LOGGER.warn(element.getTagName() + "." + name
            + " not properly formatted : " + element2);
      }

    int[] ints = new int[integers.size()];
    int i = 0;
    for (Integer d : integers)
      ints[i++] = d;

    realObject.setProperty(name, ints);
  }

  protected String[] getStrings(Element element)
  {
    String value = element.getAttribute("value");
    String[] split = value.split(",");
    Collection<String> strings = new ArrayList<String>();
    for (int i = 0; i < split.length; i++)
    {
      split[i] = split[i].trim();
      if (split[i].length() > 0) strings.add(split[i]);
    }
    return strings.toArray(new String[0]);
  }

}
