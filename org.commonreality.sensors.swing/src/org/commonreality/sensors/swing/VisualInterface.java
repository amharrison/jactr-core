package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.base.BaseSensor;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.base.PerceptManager;
import org.commonreality.sensors.swing.processors.SizeAndLocationProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class VisualInterface
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VisualInterface.class);

  private PerceptManager             _visualPerceptManager;

  private BootstrapListener          _bootstrapListener;

  private SizeAndLocationProcessor   _sizeAndLocationProcessor;

  private URL                        _configurationURL;

  public VisualInterface(BaseSensor sensor)
  {
    _visualPerceptManager = new PerceptManager(sensor);

    _bootstrapListener = new BootstrapListener(_visualPerceptManager);
  }

  public void initialize(URL url) throws Exception
  {
    _visualPerceptManager.reset();
    
    /*
     * load the config file and run with it..
     */
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      Document doc = parser.parse(_configurationURL.openStream());

      /*
       * find all the processors
       */
      NodeList nl = doc.getElementsByTagName("processor");
      for (int i = 0; i < nl.getLength(); i++)
      {
        Element node = (Element) nl.item(i);
        String className = node.getAttribute("class");
        try
        {
          Class clazz = getClass().getClassLoader().loadClass(className);
          IObjectProcessor processor = (IObjectProcessor) clazz.newInstance();
          processor.configure(getOptions(node));
          _visualPerceptManager.install(processor);
          if (processor instanceof SizeAndLocationProcessor)
            _sizeAndLocationProcessor = (SizeAndLocationProcessor) processor;
        }
        catch (Exception e)
        {
          LOGGER.error("Could not install processor " + className, e);
        }
      }

      /*
       * and creators
       */
      nl = doc.getElementsByTagName("creator");
      for (int i = 0; i < nl.getLength(); i++)
      {
        Element node = (Element) nl.item(i);
        String className = node.getAttribute("class");
        try
        {
          Class clazz = getClass().getClassLoader().loadClass(className);
          IObjectCreator creator = (IObjectCreator) clazz.newInstance();
          creator.configure(getOptions(node));
          _visualPerceptManager.install(creator);
        }
        catch (Exception e)
        {
          LOGGER.error("Could not install processor " + className, e);
        }
      }
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not configure sensor", e);
    }
  }
  
  private Map<String, String> getOptions(Element element)
  {
    Map<String, String> rtn = new TreeMap<String, String>();
    NodeList options = element.getElementsByTagName("option");
    for (int i = 0; i < options.getLength(); i++)
    {
      Element option = (Element) options.item(i);
      rtn.put(option.getAttribute("name"), option.getAttribute("value"));
    }
    return rtn;
  }
  
  public void start() throws Exception
  {
    /*
     * finally, attach to AWTevent thread
     */
    _bootstrapListener.install();
    
  }
  
  public void stop()
  {
    
    _bootstrapListener.uninstall();
  }

  public void dispose()
  {
  }

  public SizeAndLocationProcessor getSizeAndLocationProcessor()
  {
    return _sizeAndLocationProcessor;
  }

  public PerceptManager getVisualPerceptManager()
  {
    return _visualPerceptManager;
  }

  protected void processDirtyObjects()
  {
    _visualPerceptManager.processDirtyObjects();
  }
}
