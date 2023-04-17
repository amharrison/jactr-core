package org.jactr.tools.experiment.actions.sensor;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.parser.handlers.INodeHandler;
import org.w3c.dom.Element;

public class XMLSensorHandler implements INodeHandler<IAction>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(XMLSensorHandler.class);

  public String getTagName()
  {
    return "xml-sensor";
  }

  public IAction process(Element element, IExperiment experiment)
  {
    return new XMLSensorAction(element.getAttribute("url"), Boolean.parseBoolean(element.getAttribute("immediate")), experiment);
  }

  public boolean shouldDecend()
  {
    return false;
  }

}
