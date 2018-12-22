package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.manager.IObjectManager;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.base.BaseSensor;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.base.PerceptManager;
import org.commonreality.sensors.swing.creators.ButtonCreator;
import org.commonreality.sensors.swing.creators.LabelCreator;
import org.commonreality.sensors.swing.creators.TextFieldCreator;
import org.commonreality.sensors.swing.processors.ColorProcessor;
import org.commonreality.sensors.swing.processors.SizeAndLocationProcessor;
import org.commonreality.sensors.swing.processors.VisibilityProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * bare bones swing sensor that uses {@link BaseSensor}, {@link PerceptManager},
 * {@link IObjectCreator} and {@link IObjectProcessor}s.
 * 
 * @author harrison
 */
public class DefaultSwingSensor extends BaseSensor
{
  static public String               CONFIGURATION_PARAM = "ConfigurationURL";

  /**
   * Logger definition
   */
  static private final transient Log LOGGER              = LogFactory
                                                             .getLog(DefaultSwingSensor.class);


  private URL                        _visualConfigurationURL;

  private VisualInterface            _visualInterface;
  private InterpolatedMotorInterface _motorInterface;

  public DefaultSwingSensor()
  {
    _visualInterface = new VisualInterface(this);
    _motorInterface = new InterpolatedMotorInterface(this);
  }

  @Override
  public String getName()
  {
    return "swing";
  }

  public void initialize() throws Exception
  {    
    _visualInterface.initialize(_visualConfigurationURL);
    _motorInterface.initialize(_visualConfigurationURL);

    super.initialize();
  }

  public void configure(Map<String, String> options) throws Exception
  {
    super.configure(options);

    /*
     * 
     */
    String config = options.get(CONFIGURATION_PARAM);

    if (config == null || config.trim().length() == 0)
      config = "org/commonreality/sensors/swing/default.xml";

    _visualConfigurationURL = getClass().getClassLoader().getResource(config);
    if (_visualConfigurationURL == null)
      try
      {
        _visualConfigurationURL = new URI(config).toURL();
      }
      catch (Exception e)
      {
        throw new IllegalArgumentException("Could not get valid url from "
            + config, e);
      }
  }
  
  public void start() throws Exception
  {
    _visualInterface.start();
    _motorInterface.start();
    super.start();
  }

  public void stop() throws Exception
  {
    _visualInterface.stop();
    _motorInterface.stop();
    super.stop();
  }
  
  public void shutdown() throws Exception
  {
    _visualInterface.dispose();
    _motorInterface.dispose();
    super.shutdown();
  }

  public SizeAndLocationProcessor getSizeAndLocationProcessor()
  {
    return _visualInterface.getSizeAndLocationProcessor();
  }

  protected PerceptManager getVisualPerceptManager()
  {
    return _visualInterface.getVisualPerceptManager();
  }


  protected double processPercepts()
  {
    _visualInterface.processDirtyObjects();
    return Double.NaN;
  }


  /**
   * current interpolated motor doesn't queue up messages but sends them immediately.
   * it should be refactored to match base sensor's contract
   */
  @Override
  protected double processMotor()
  {
    return _motorInterface.process();
  }
}
