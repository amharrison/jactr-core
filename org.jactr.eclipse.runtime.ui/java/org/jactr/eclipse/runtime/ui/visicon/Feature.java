package org.jactr.eclipse.runtime.ui.visicon;

/*
 * default logging
 */
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.visual.geom.Dimension2D;
import org.commonreality.modalities.visual.geom.Point2D;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.jactr.modules.pm.visual.memory.impl.map.DimensionFeatureMap;
import org.jactr.modules.pm.visual.memory.impl.map.HeadingFeatureMap;
import org.jactr.modules.pm.visual.memory.impl.map.PitchFeatureMap;
import org.jactr.modules.pm.visual.memory.impl.map.VisibilityFeatureMap;

public class Feature
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(Feature.class);

  private IIdentifier                _identifier;

  private Path                       _path;

  private Color                      _color;

  private boolean                    _isVisible;

  private Map<String, Object>        _allData;

  public Feature(IIdentifier identifier, Device device, Map<String, Object> data)
  {
    _allData = new TreeMap<String, Object>();
    _identifier = identifier;
    _color = device.getSystemColor(SWT.COLOR_BLACK);
    _path = update(device, data);
  }

  public IIdentifier getIdentifier()
  {
    return _identifier;
  }

  protected Path getPath()
  {
    return _path;
  }

  public void update(Map<String, Object> data)
  {
    Device device = null;
    synchronized (_path)
    {
      device = _path.getDevice();
    }

    Path old = _path;
    _path = update(device, data);

    synchronized (old)
    {
      old.dispose();
    }
  }

  protected Path update(Device device, Map<String, Object> data)
  {
    synchronized (data)
    {
      _allData.putAll(data);
    }

    Path path = new Path(device);

    Dimension2D size = getSize();
    Point2D center = getCenter();

    if (size != null && center != null)
    {
      float x = (float) (center.getX() - size.getWidth() / 2);
      float y = (float) (center.getY() - size.getHeight() / 2);
      float width = (float) size.getWidth();
      float height = (float) size.getHeight();
      path.addRectangle(x, y, width, height);
    }

    Color color = getColor();
    if (color != null) _color = color;

    Boolean vis = getVisibility();
    if (vis != null) _isVisible = vis;

    return path;
  }

  public void render(GC graphics)
  {
    if (!_isVisible) return;

    Color old = graphics.getBackground();
    graphics.setBackground(_color);
    graphics.fillPath(_path);
    graphics.setBackground(old);
  }

  public void dispose()
  {
    _allData.clear();
    _path.dispose();
    _path = null;
  }

  public boolean contains(Point2D point)
  {
    Point2D center = getCenter();
    Dimension2D size = getSize();
    if (center == null || size == null) return false;
    return point.getX() <= center.getX() + size.getWidth() / 2
        && point.getX() >= center.getX() - size.getWidth() / 2
        && point.getY() <= center.getY() + size.getHeight() / 2
        && point.getY() >= center.getY() - size.getHeight() / 2;
  }

  public double getArea()
  {
    Dimension2D size = getSize();
    if (size == null) return -1;
    return size.getHeight() * size.getWidth();
  }

  public String getToolTip()
  {
    StringBuilder sb = new StringBuilder("ID : ");
    sb.append(_identifier);

    for (Map.Entry<String, Object> entry : _allData.entrySet())
    {
      sb.append("\n").append(entry.getKey()).append(" : ");
      Object value = entry.getValue();
      if (value != null && value.getClass().isArray())
      {
        Object[] arr = (Object[]) value;
        for (Object tmp : arr)
          sb.append(tmp).append(" ");
      }
      else
        sb.append(value);
    }
    return sb.toString();
  }

  protected Point2D getCenter()
  {
    try
    {
      double pitch = (Double) _allData.get(PitchFeatureMap.class
          .getSimpleName());
      double heading = (Double) _allData.get(HeadingFeatureMap.class
          .getSimpleName());
      return new Point2D(heading, pitch);
    }
    catch (Exception e)
    {
      return new Point2D(0, 0);
    }
  }

  protected Dimension2D getSize()
  {
    try
    {
      return (Dimension2D) _allData.get(DimensionFeatureMap.class
          .getSimpleName());
    }
    catch (Exception e)
    {
      return new Dimension2D(0, 0);
    }
  }

  protected Color getColor()
  {
    return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
  }

  protected Boolean getVisibility()
  {
    try
    {
      return (Boolean) _allData.get(VisibilityFeatureMap.class.getSimpleName());
    }
    catch (Exception e)
    {
      return false;
    }
  }
}
