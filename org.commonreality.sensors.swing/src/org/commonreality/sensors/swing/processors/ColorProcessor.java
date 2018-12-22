package org.commonreality.sensors.swing.processors;

/*
 * default logging
 */
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IMutableObject;
import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.base.PerceptManager;
import org.commonreality.sensors.swing.key.AWTObjectKey;

/**
 * the color processor will snag the foreground and background colors of any
 * component and set the appropriate properties for the percept. To save on
 * unnecessary processing, it also uses a combination of a listener and a weak
 * hash map to flag those that have changed..
 * 
 * @author harrison
 */
public class ColorProcessor extends AbstractComponentProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER               = LogFactory
                                                              .getLog(ColorProcessor.class);

  PropertyChangeListener             _colorChangeListener = new PropertyChangeListener() {

                                                            public void propertyChange(
                                                                PropertyChangeEvent evt)
                                                            {
                                                              String property = evt
                                                                  .getPropertyName();
                                                              if (property
                                                                  .equals("foreground")
                                                                  || property
                                                                      .equals("background"))
                                                                markAsChanged((Component) evt
                                                                    .getSource());
                                                            }
                                                          };

  public boolean handles(AWTObjectKey object)
  {
    return hasChanged(object.getComponent());
  }

  public void process(AWTObjectKey object, IMutableObject simulationObject)
  {
    Component component = object.getComponent();

    if (hasChanged(component))
    {
      clearChanged(component);
      float[] foreground = new float[4];
      float[] background = new float[4];

      component.getForeground().getColorComponents(foreground);
      component.getBackground().getColorComponents(background);

      simulationObject.setProperty(IVisualPropertyHandler.COLOR, new double[] {
          foreground[0], foreground[1], foreground[2], foreground[3],
          background[0], background[1], background[2], background[3] });
    }
  }

  @Override
  void attachListener(Component component)
  {
    component.addPropertyChangeListener("foreground", _colorChangeListener);
    component.addPropertyChangeListener("background", _colorChangeListener);
  }

  @Override
  void detachListener(Component component)
  {
    component.removePropertyChangeListener("foreground", _colorChangeListener);
    component.removePropertyChangeListener("background", _colorChangeListener);

  }

}
