package org.commonreality.sensors.swing.processors;

/*
 * default logging
 */
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IMutableObject;
import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.swing.key.AWTObjectKey;

/**
 * rough approximation of the visibility detector. This probably doesn't handle
 * viewports correctly.
 * 
 * @author harrison
 */
public class VisibilityProcessor extends AbstractComponentProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER             = LogFactory
                                                            .getLog(VisibilityProcessor.class);

  private ComponentListener          _componentListener = new ComponentListener() {

                                                          public void componentHidden(
                                                              ComponentEvent e)
                                                          {
                                                            markAsChanged(e
                                                                .getComponent());
                                                          }

                                                          public void componentMoved(
                                                              ComponentEvent e)
                                                          {

                                                          }

                                                          public void componentResized(
                                                              ComponentEvent e)
                                                          {
                                                          }

                                                          public void componentShown(
                                                              ComponentEvent e)
                                                          {
                                                            markAsChanged(e
                                                                .getComponent());
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
      boolean isShowing = component.isShowing();
      simulationObject.setProperty(IVisualPropertyHandler.VISIBLE, isShowing);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s is%s showing", object.getIdentifier(), isShowing?"":"n't")); 
    }
  }

  @Override
  void attachListener(Component component)
  {
    component.addComponentListener(_componentListener);
  }

  @Override
  void detachListener(Component component)
  {
    component.removeComponentListener(_componentListener);
  }
}
