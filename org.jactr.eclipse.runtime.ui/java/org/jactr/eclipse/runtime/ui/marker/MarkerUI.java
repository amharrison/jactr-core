package org.jactr.eclipse.runtime.ui.marker;

/*
 * default logging
 */
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.jactr.eclipse.runtime.ui.UIPlugin;

public class MarkerUI
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(MarkerUI.class);

  private final ColorRegistry        _colorRegistry;

  static private final String        COLOR_SUFFIX = ".markerColor";

  static public final String         ALL_MARKERS  = "marker.allMakers";

  static private final MarkerUI      _instance    = new MarkerUI();

  static public MarkerUI getInstance()
  {
    return _instance;
  }

  private MarkerUI()
  {
    _colorRegistry = new ColorRegistry();
    loadColorRegistry();
    installListener();
  }

  private void installListener()
  {
    IPreferenceStore preferenceStore = UIPlugin.getDefault()
        .getPreferenceStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent event)
      {
        // ensure our
        if (event.getProperty().endsWith(COLOR_SUFFIX))
        {
          RGB rgb = PreferenceConverter.getColor(
              (IPreferenceStore) event.getSource(), event.getProperty());
          String type = event.getProperty();
          type = type.substring(0, type.lastIndexOf(COLOR_SUFFIX));
          _colorRegistry.put(type, rgb);
        }
      }

    });
  }

  protected void loadColorRegistry()
  {
    IPreferenceStore preferenceStore = UIPlugin.getDefault()
        .getPreferenceStore();

    Set<String> allTypes = getStoredMarkerTypes();

    for (String type : allTypes)
    {
      RGB rgb = PreferenceConverter.getColor(preferenceStore, type
          + COLOR_SUFFIX);
      _colorRegistry.put(type, rgb);
    }

  }

  protected void updatePreferences(String markerType, RGB newRGB)
  {
    IPreferenceStore preferenceStore = UIPlugin.getDefault()
        .getPreferenceStore();

    PreferenceConverter.setValue(preferenceStore, markerType + COLOR_SUFFIX,
        newRGB);
    Set<String> allTypes = getStoredMarkerTypes();
    allTypes.add(markerType);
    setStoredMarkerTypes(allTypes);
  }

  static public Set<String> getStoredMarkerTypes()
  {
    IPreferenceStore preferenceStore = UIPlugin.getDefault()
        .getPreferenceStore();
    String allTypes = preferenceStore.getString(ALL_MARKERS);
    StringTokenizer tokenizer = new StringTokenizer(allTypes, ",");

    TreeSet<String> types = new TreeSet<String>();
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      token = token.trim();
      types.add(token);
    }

    return types;
  }

  protected void setStoredMarkerTypes(Set<String> allTypes)
  {
    StringBuilder sb = new StringBuilder();
    for (String type : allTypes)
      sb.append(type).append(",");
    sb.deleteCharAt(sb.length() - 1);

    IPreferenceStore preferenceStore = UIPlugin.getDefault()
        .getPreferenceStore();
    preferenceStore.setValue(ALL_MARKERS, sb.toString());
  }

  public RGB getRGB(String markerType, boolean createIfMissing)
  {
    RGB rgb = _colorRegistry.getRGB(markerType);
    if (rgb == null && createIfMissing)
    {
      /**
       * use HSV to define. Hue is random (0-360), Saturation is random
       * (0.1-0.5), value/lightness will be random (0.75-1)
       */
      RGB newRGB = new RGB((float) (Math.random() * 360),
          (float) (Math.random() * 0.4 + 0.1),
          (float) (Math.random() * 0.25 + 0.75));
      _colorRegistry.put(markerType, newRGB);
      updatePreferences(markerType, newRGB);
      rgb = newRGB;
    }

    return rgb;
  }

  public Color getColor(String markerType, boolean createIfMissing)
  {
    Color color = _colorRegistry.get(markerType);
    if (color == null && createIfMissing)
    {
      getRGB(markerType, createIfMissing);
      color = _colorRegistry.get(markerType);
    }

    return color;
  }
}
