package org.jactr.eclipse.ui.wizards.pages;

/*
 * default logging
 */
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.InstrumentDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.ModelExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;
import org.jactr.eclipse.ui.images.JACTRImages;

public class CommonExtensionDescriptorLabelProvider extends LabelProvider
    implements IFontProvider, IColorProvider
{
  /**
   * Logger definition
   */
  static private final transient Log                  LOGGER = LogFactory
                                                                 .getLog(CommonExtensionDescriptorLabelProvider.class);

  private Predicate<CommonExtensionDescriptor>        _shouldHighlight;

  private Predicate<CommonExtensionDescriptor>        _isAvailable;

  private Function<CommonExtensionDescriptor, String> _decorateLabel;

  private Font                                        _defaultFont;

  private Font                                        _availableFont;

  private Font                                        _highlightFont;

  private Color                                       _highlightColor;

  private Color                                       _availableColor;

  public CommonExtensionDescriptorLabelProvider()
  {
    this((ced) -> false, (ced) -> false, (ced) -> "");
  }

  public CommonExtensionDescriptorLabelProvider(
      Predicate<CommonExtensionDescriptor> highlight,
      Predicate<CommonExtensionDescriptor> available,
      Function<CommonExtensionDescriptor, String> decorator)
  {
    if (highlight == null) highlight = (ced) -> false;
    if (available == null) available = (ced) -> false;
    if (decorator == null) decorator = (ced) -> "";

    _shouldHighlight = highlight;
    _isAvailable = available;
    _decorateLabel = decorator;

    String rootFontName = JFaceResources.DIALOG_FONT;
    _defaultFont = JFaceResources.getFontRegistry().get(rootFontName);
    _highlightFont = JFaceResources.getFontRegistry().getBold(rootFontName);
    _availableFont = JFaceResources.getFontRegistry().getItalic(rootFontName);

    _availableColor = JFaceResources.getColorRegistry().get(
        JFacePreferences.COUNTER_COLOR);
    _highlightColor = JFaceResources.getColorRegistry().get(
        JFacePreferences.HYPERLINK_COLOR);
  }

  @Override
  public String getText(Object element)
  {
    CommonExtensionDescriptor ced = (CommonExtensionDescriptor) element;
    String label = String.format("%s %s", ced.getName(),
        _decorateLabel.apply(ced));
    return label;
  }

  @Override
  public Font getFont(Object element)
  {
    CommonExtensionDescriptor ced = (CommonExtensionDescriptor) element;
    if (_shouldHighlight.test(ced)) return _highlightFont;
    if (_isAvailable.test(ced)) return _availableFont;
    return _defaultFont;
  }

  @Override
  public Color getForeground(Object element)
  {
    CommonExtensionDescriptor ced = (CommonExtensionDescriptor) element;
    if (_shouldHighlight.test(ced)) return _highlightColor;
    if (_isAvailable.test(ced)) return _availableColor;
    return null;
  }

  @Override
  public Color getBackground(Object element)
  {
    return null;
  }

  @Override
  public Image getImage(Object element)
  {
    CommonExtensionDescriptor ced = (CommonExtensionDescriptor) element;
    /*
     * use the type to determine the icon
     */
    if (ced instanceof ModelExtensionDescriptor)
      return JACTRImages.getImage(JACTRImages.EXTENSION);
    if (ced instanceof InstrumentDescriptor)
      return JACTRImages.getImage(JACTRImages.TOOL);
    if (ced instanceof ModuleDescriptor)
      return JACTRImages.getImage(JACTRImages.LIBRARY);
    return null;
  }

}
