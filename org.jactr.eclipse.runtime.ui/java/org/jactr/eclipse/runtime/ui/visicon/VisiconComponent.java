package org.jactr.eclipse.runtime.ui.visicon;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.visual.geom.Point2D;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jactr.eclipse.runtime.visual.VisualDescriptor;

public class VisiconComponent extends Canvas
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VisiconComponent.class);

  public static final float DEFAULT_MAGNIFICATION = 1.25f;
  
  /*
   * for rendering the visicon
   */
  private PaintListener              _painter;

  /*
   * for maintaining the transform
   */
  private ControlListener            _scaler;

  /*
   * used to scale and translate the graphics coord to match visual
   */
  private Transform                  _transform;

  private Transform                  _inverse;

  private Map<IIdentifier, Feature>  _features;

  private VisualDescriptor           _descriptor;

  private IIdentifier                _found;

  private IIdentifier                _encoded;
  
  private float _magnification = DEFAULT_MAGNIFICATION;

  public VisiconComponent(Composite parent, int style,
      VisualDescriptor descriptor, float magnification)
  {
    super(parent, style);

    _descriptor = descriptor;
    _features = new HashMap<IIdentifier, Feature>();
    _magnification = magnification;
    
    addMouseMoveListener(new MouseMoveListener() {

      public void mouseMove(MouseEvent me)
      {
        Point2D p = toVisiconCoordinates(me.x, me.y);
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Trying to find hover at " + p);

        double smallest = Double.MAX_VALUE;
        Feature selected = null;
        synchronized (_features)
        {
          for (Feature feature : _features.values())
            if (feature.contains(p))
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug(feature.getIdentifier() + " is viable");
              double area = feature.getArea();
              if (area != -1 && area < smallest)
              {
                selected = feature;
                smallest = area;
              }
            }
        }

        if (selected != null)
          setToolTipText(selected.getToolTip());
        else
          setToolTipText("");
      }
    });

    _painter = new PaintListener() {

      public void paintControl(PaintEvent e)
      {
        renderFeatures(e.gc);
      }
    };

    _scaler = new ControlListener() {

      public void controlMoved(ControlEvent e)
      {
        // noop

      }

      public void controlResized(ControlEvent e)
      {
    	  updateTransform();
      }
    };

    addPaintListener(_painter);
    addControlListener(_scaler);

    _transform = new Transform(getDisplay());
    _inverse = new Transform(getDisplay());
    _inverse.multiply(_transform);
    _inverse.invert();

    /*
     * process all the identifiers already known about
     */
    for (IIdentifier identifier : _descriptor.getIdentifiers())
      add(identifier, _descriptor.getData(identifier));
  }
  
  void setMagnification(float magnification) {
	  System.err.println("magnification="+magnification);
	  _magnification = magnification;
	  updateTransform();
	  redraw();
  }
  
  private void updateTransform() {
    if (_transform != null) {
      _transform.dispose();
      _inverse.dispose();
    }

    _transform = new Transform(getDisplay());
    _inverse = new Transform(getDisplay());

    Rectangle bounds = getBounds();
    double[] res = _descriptor.getResolution();
    
    if (res == null)
    	return;

    // scale
    _transform.scale( bounds.width  / (float) (res[0] * _magnification),
    		         -bounds.height / (float) (res[1] * _magnification));
    // and center
    _transform.translate((float) (res[0] * _magnification) / 2f,
        -(float) (res[1] * _magnification) / 2);

    _inverse.multiply(_transform);
    
    if (bounds.height != 0 && bounds.width != 0)
    	_inverse.invert();
  }

  protected void drawAxes(GC graphics)
  {
    // draw axes
    Color oldC = graphics.getForeground();
    graphics
        .setForeground(graphics.getDevice().getSystemColor(SWT.COLOR_BLACK));
    graphics.setLineWidth(1);

    Point size = getSize();
    graphics.drawLine(0, size.y / 2, size.x, size.y / 2);
    graphics.drawLine(size.x / 2, 0, size.x / 2, size.y);

    graphics.setForeground(oldC);
  }

  @Override
  public void dispose()
  {
    removePaintListener(_painter);
    removeControlListener(_scaler);
    super.dispose();

    synchronized (_features)
    {
      for (Feature feature : _features.values())
        feature.dispose();
      _features.clear();
    }
  }

  public void add(IIdentifier identifier, Map<String, Object> data)
  {
    Feature feature = new Feature(identifier, getDisplay(), data);
    synchronized (_features)
    {
      _features.put(identifier, feature);
    }

    redraw();
  }

  public void remove(IIdentifier identifier)
  {
    synchronized (_features)
    {
      _features.remove(identifier);
    }

    redraw();
  }

  public void update(IIdentifier identifier, Map<String, Object> data)
  {
    Feature feature = null;
    synchronized (_features)
    {
      feature = _features.get(identifier);
    }

    if (feature != null) feature.update(data);

    redraw();
  }

  public void encoded(IIdentifier identifier)
  {
    _encoded = identifier;
    redraw();
  }

  public void found(IIdentifier identifier)
  {
    _encoded = null;
    _found = identifier;
    redraw();
  }

  protected void drawFound(GC graphics, Feature feature)
  {
    Color old = graphics.getForeground();
    int oldW = graphics.getLineWidth();

    graphics.setForeground(graphics.getDevice().getSystemColor(SWT.COLOR_RED));
    graphics.setLineWidth(2);
    graphics.drawPath(feature.getPath());

    graphics.setLineWidth(oldW);
    graphics.setForeground(old);
  }

  protected void drawEncoded(GC graphics, Feature feature)
  {
    Color old = graphics.getBackground();
    int oldW = graphics.getLineWidth();

    graphics.setBackground(graphics.getDevice().getSystemColor(SWT.COLOR_RED));
    graphics.setLineWidth(1);

    graphics.fillPath(feature.getPath());

    graphics.setLineWidth(oldW);
    graphics.setBackground(old);
  }

  protected void renderFeatures(GC graphics)
  {
    drawAxes(graphics);

    Transform oldT = new Transform(graphics.getDevice());
    graphics.getTransform(oldT);
    graphics.setTransform(_transform);

    synchronized (_features)
    {
      for (Feature feature : _features.values())
      {
        feature.render(graphics);
        if (_found != null && _found.equals(feature.getIdentifier()))
          drawFound(graphics, feature);
        if (_encoded != null && _encoded.equals(feature.getIdentifier()))
          drawEncoded(graphics, feature);
      }
    }

    graphics.setTransform(oldT);
    oldT.dispose();
  }

  protected Point2D toVisiconCoordinates(int x, int y)
  {
    float[] xy = new float[] { x, y };
    _inverse.transform(xy);
    return new Point2D(xy[0], xy[1]);
  }

}
