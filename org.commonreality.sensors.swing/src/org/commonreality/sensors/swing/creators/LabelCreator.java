package org.commonreality.sensors.swing.creators;

/*
 * default logging
 */
import java.awt.Label;

import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IMutableObject;
import org.commonreality.sensors.base.PerceptManager;
import org.commonreality.sensors.swing.key.AWTObjectKey;

/**
 * Handles the creation and updating of labels (JLabel and label)
 * 
 * @author harrison
 */
public class LabelCreator extends AWTObjectCreator
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LabelCreator.class);

  public boolean handles(Object object)
  {
    return object instanceof Label || object instanceof JLabel;
  }

  @Override
  protected void initialize(AWTObjectKey objectKey, IMutableObject simObject)
  {
    super.initialize(objectKey, simObject);
    /*
     * add the 'label' type, the text we will handle below.. in process
     * inefficient, I know - but this is being used as an example..
     */
    String[] types = _pHandler.getTypes(simObject);
    String[] newTypes = new String[types.length + 1];
    System.arraycopy(types, 0, newTypes, 0, types.length);
    newTypes[newTypes.length - 1] = "label";
    simObject.setProperty(IVisualPropertyHandler.TYPE, newTypes);

    process(objectKey, simObject);
  }


  public void process(AWTObjectKey object, IMutableObject simulationObject)
  {
    /*
     * can be true if the system hasn't acknowledged the object yet.
     */
    if (simulationObject == null) return;

    String text = null;

    if (object.getObject() instanceof JLabel)
      text = ((JLabel) object.getObject()).getText();
    else
      text = ((Label) object.getObject()).getText();

    if (text != null)
    {
      simulationObject.setProperty(IVisualPropertyHandler.TEXT, text);
      simulationObject.setProperty(IVisualPropertyHandler.TOKEN, text);
    }
  }

  public void installed(PerceptManager manager)
  {
    // TODO Auto-generated method stub
    
  }

  public void uninstalled(PerceptManager manager)
  {
    // TODO Auto-generated method stub
    
  }
}
