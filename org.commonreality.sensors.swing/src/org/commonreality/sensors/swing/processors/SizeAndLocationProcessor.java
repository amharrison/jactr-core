package org.commonreality.sensors.swing.processors;

/*
 * default logging
 */
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IMutableObject;
import org.commonreality.sensors.swing.key.AWTObjectKey;

/**
 * rough approximation of a processor that computes the retinotopic loction &
 * size of arbitrary JComponents using the physical screen width & height and
 * viewers distance (in meters) <br/>
 * 
 * 
 * @bug does not handled multiple monitors correctly.
 * @bug we probably need to attach listeners to the parents all the way up to
 *      the root so that we know when they move, which will change the location
 *      of the children
 * @author harrison
 */
public class SizeAndLocationProcessor extends AbstractComponentProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER                = LogFactory
                                                               .getLog(SizeAndLocationProcessor.class);

  static public final String         SCREEN_WIDTH_PARAM    = "ScreenWidth";

  static public final String         SCREEN_HEIGHT_PARAM   = "ScreenHeight";

  static public final String         VIEWER_DISTANCE_PARAM = "ViewerDistance";

  private double                     _screenPhysicalWidth;

  private double                     _screenPhysicalHeight;

  private long                       _screenPixelWidth;

  private long                       _screenPixelHeight;

  private long                       _pixelHalfWidth;

  private long                       _pixelHalfHeight;

  private double                     _viewerPhysicalDistance;

  private double                     _viewerPhysicalDistanceSquared;

  /*
   * horizontal pixels per m
   */
  private double                     _horizontalDPM;

  private double                     _vertialDPM;

  private ComponentListener          _componentListener    = new ComponentListener() {

                                                             public void componentHidden(
                                                                 ComponentEvent e)
                                                             {
                                                               markAsChanged(e
                                                                   .getComponent());
                                                             }

                                                             public void componentMoved(
                                                                 ComponentEvent e)
                                                             {
                                                               markAsChanged(e
                                                                   .getComponent());

                                                             }

                                                             public void componentResized(
                                                                 ComponentEvent e)
                                                             {
                                                               markAsChanged(e
                                                                   .getComponent());

                                                             }

                                                             public void componentShown(
                                                                 ComponentEvent e)
                                                             {
                                                               markAsChanged(e
                                                                   .getComponent());
                                                             }

                                                           };

  public void configure(Map<String, String> properties)
  {
    try
    {
      _viewerPhysicalDistance = Double.parseDouble(properties
          .get(VIEWER_DISTANCE_PARAM));
    }
    catch (Exception e)
    {
      LOGGER
          .warn(
              "Could not extract viewer distance from properties, assuming 0.75",
              e);
      _viewerPhysicalDistance = 0.75;
    }

    try
    {
      _screenPhysicalHeight = Double.parseDouble(properties
          .get(SCREEN_HEIGHT_PARAM));
      _screenPhysicalWidth = Double.parseDouble(properties
          .get(SCREEN_WIDTH_PARAM));
    }
    catch (Exception e)
    {
      LOGGER.warn("Could not extract physical screen size, assuming 0.3 x 0.2",
          e);
      _screenPhysicalHeight = 0.25;
      _screenPhysicalWidth = 0.3;
    }

    _viewerPhysicalDistanceSquared = _viewerPhysicalDistance
        * _viewerPhysicalDistance;

    /*
     * and snag the pixel size
     */
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension dim = tk.getScreenSize();
    _screenPixelHeight = dim.height;
    _screenPixelWidth = dim.width;

    _pixelHalfHeight = _screenPixelHeight / 2;
    _pixelHalfWidth = _screenPixelWidth / 2;

    _vertialDPM = _screenPixelHeight / _screenPhysicalHeight;
    _horizontalDPM = _screenPixelWidth / _screenPhysicalWidth;
  }

  public boolean handles(AWTObjectKey object)
  {
    // return hasChanged(object.getComponent());
    return true;
  }

  /**
   * sets the retinal location, distance and size properties of a visual percept
   */
  public void process(AWTObjectKey object, IMutableObject simulationObject)
  {
    Component component = object.getComponent();

    // if (hasChanged(component))
    // {
    // clearChanged(component);

    double dimensions[] = toRetinotopic(component);

    simulationObject.setProperty(IVisualPropertyHandler.RETINAL_SIZE,
        new double[] { dimensions[3], dimensions[4] });
    simulationObject.setProperty(IVisualPropertyHandler.RETINAL_DISTANCE,
        dimensions[2]);
    simulationObject.setProperty(IVisualPropertyHandler.RETINAL_LOCATION,
        new double[] { dimensions[0], dimensions[1] });
    // }
  }

  /**
   * convert retinotopic location to a screen location
   * 
   * @param location double[2] angular (degrees)
   * @return double[2] screen coordinates
   */
  public double[] toScreenLocation(double[] location)
  {
    double[] rtn = new double[2];

    /*
     * convert to meters from center
     */
    rtn[0] = Math.tan(Math.toRadians(location[0])) * _viewerPhysicalDistance;
    rtn[1] = Math.tan(Math.toRadians(location[1])) * _viewerPhysicalDistance;

    for (int i = 0; i < rtn.length; i++)
    {
      // to meters
      rtn[i] = Math.tan(Math.toRadians(location[i])) * _viewerPhysicalDistance;

      // to pixels and
      // reposition to screen coordinate system
      if (i == 0)
      {
        rtn[i] *= _horizontalDPM;
        rtn[i] += _pixelHalfWidth;
      }
      else
      {
        rtn[i] *= _vertialDPM;
        rtn[i] = _pixelHalfHeight - rtn[i];
      }
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("retino (%.2f, %.2f) to screen (%.2f, %.2f)",
          location[0], location[1], rtn[0], rtn[1]));

    return rtn;
  }

  /**
   * returns the retinotopic spatial info of this component
   * 
   * @param component
   * @return double[x,y,z,width,height]
   */
  public double[] toRetinotopic(Component component)
  {
    /*
     * components center location
     */
    Rectangle bounds = component.getBounds();
    Point origin = new Point(0, 0);
    Point lowerRight = new Point(bounds.width, bounds.height);

    /*
     * converts to screen pixels..
     */
    SwingUtilities.convertPointToScreen(origin, component);
    SwingUtilities.convertPointToScreen(lowerRight, component);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s is at (%d, %d) screen", component.hashCode(),
          origin.x, origin.y));

    /*
     * now let's convert that coordinate system to 0,0 center, +x right +y up.
     */

    double ox = origin.x - _pixelHalfWidth;
    double oy = _pixelHalfHeight - origin.y;
    double lrx = lowerRight.x - _pixelHalfWidth;
    double lry = _pixelHalfHeight - lowerRight.y;

    /*
     * now put everyone in meters before retinotopic conversion
     */
    ox /= _horizontalDPM;
    oy /= _vertialDPM;
    lrx /= _horizontalDPM;
    lry /= _vertialDPM;

    // planar distance from center
    double oz = Math.sqrt(ox * ox + oy * oy);
    oz = Math.sqrt(oz * oz + _viewerPhysicalDistanceSquared);

    double lrz = Math.sqrt(lrx * lrx + lry * lry);
    lrz = Math.sqrt(lrz * lrz + _viewerPhysicalDistanceSquared);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s is at (%.2f, %.2f, %.2f) meters",
          component.hashCode(), ox, oy, oz));

    /*
     * and now the trig
     */
    ox = Math.toDegrees(Math.atan(ox / oz));
    oy = Math.toDegrees(Math.atan(oy / oz));

    lrx = Math.toDegrees(Math.atan(lrx / lrz));
    lry = Math.toDegrees(Math.atan(lry / lrz));

    double width = lrx - ox;
    double height = oy - lry;

    double[] rtn = new double[] { ox + width / 2, oy - height / 2,
        (oz + lrz / 2), width, height };

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "%s is at (%.2f, %.2f, %.2f) x (%.2f, %.2f) retino", component.hashCode(),
          rtn[0], rtn[1], rtn[2], rtn[3], rtn[4]));

    return rtn;
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
