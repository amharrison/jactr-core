package org.jactr.tools.experiment.parser.handlers;

/*
 * default logging
 */
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.actions.common.DelayAction;
import org.w3c.dom.Element;

public class DelayHandler implements INodeHandler<IAction>
{
  public String getTagName()
  {
    return "delay";
  }

  public IAction process(Element element, IExperiment experiment)
  {
    return new DelayAction(Double.parseDouble(element.getAttribute("duration")),
        experiment);
  }

  public boolean shouldDecend()
  {
    return false;
  }
}