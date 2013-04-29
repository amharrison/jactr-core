package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.probe2.ModelProbeData;

/**
 * probe container that renders to an AWT.image and copies it to an SWT image
 * since SWT out of thread rendering is causing core dumps on macs.
 * 
 * @author harrison
 */
public class AWTProbeContainer extends AbstractProbeContainer
{
  /**
   * Logger definition
   */
  static final transient Log LOGGER = LogFactory
                                        .getLog(AWTProbeContainer.class);

  private BufferedImage      _chartImage;

  Image                      _uiImage;

  private Graphics2D         _chartContext;

  public AWTProbeContainer(Composite parent, ModelProbeData mpd)
  {
    super(parent, mpd);
  }

  @Override
  public void dispose()
  {
    disposeAWTImage();
    if (_uiImage != null && !_uiImage.isDisposed()) _uiImage.dispose();

    super.dispose();
  }

  private void createAWTImage(int width, int height)
  {
    if (isDisposed()) return;
    try
    {
      _contextLock.lock();

      _chartImage = new BufferedImage(width, height,
      // BufferedImage.TYPE_BYTE_INDEXED);
          BufferedImage.TYPE_INT_ARGB);

      _chartBounds = BoundsImpl.create(0, 0, width, height);
      _chartBounds.scale(72d / _chartRenderer.getDisplayServer()
          .getDpiResolution());

      _chartContext = _chartImage.createGraphics();

      _chartRenderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT,
          _chartContext);
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  private void disposeAWTImage()
  {
    try
    {
      _contextLock.lock();

      if (_chartImage != null) _chartImage = null;

      if (_chartContext != null)
      {
        _chartContext.dispose();
        _chartContext = null;
      }
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  @Override
  protected void swapImage()
  {
    try
    {
      _contextLock.lock();

      if (_uiImage != null && !_uiImage.isDisposed()) _uiImage.dispose();
      _uiImage = new Image(getDisplay(), convertToSWT(_chartImage));
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  @Override
  protected IDeviceRenderer getDeviceRenderer()
  {
    try
    {
      return ChartEngine.instance(new PlatformConfig()).getRenderer("dv.SWING");
    }
    catch (ChartException e)
    {
      LOGGER.error(
          "AWTProbeContainer.getDeviceRenderer threw ChartException : ", e);
      return null;
    }
  }

  @Override
  protected void updateRenderContext()
  {
    if (isDisposed()) return;
    try
    {
      _contextLock.lock();

      disposeAWTImage();

      /*
       * copy the old image to the new one
       */
      Rectangle bounds = getClientArea();

      if (bounds.width == 0 || bounds.height == 0)
      {
        /*
         * give it something to work with at least
         */
        bounds.width = 800;
        bounds.height = 500;
      }

      createAWTImage(bounds.width, bounds.height);
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  /**
   * AWT -> SWT conversion copied from
   * http://git.eclipse.org/c/platform/eclipse.
   * platform.swt.git/tree/examples/org
   * .eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet156.java
   * 
   * @param bufferedImage
   * @return
   */
  private ImageData convertToSWT(BufferedImage bufferedImage)
  {
    if (bufferedImage.getColorModel() instanceof DirectColorModel)
    {
      DirectColorModel colorModel = (DirectColorModel) bufferedImage
          .getColorModel();
      PaletteData palette = new PaletteData(colorModel.getRedMask(),
          colorModel.getGreenMask(), colorModel.getBlueMask());
      ImageData data = new ImageData(bufferedImage.getWidth(),
          bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
      for (int y = 0; y < data.height; y++)
        for (int x = 0; x < data.width; x++)
        {
          int rgb = bufferedImage.getRGB(x, y);
          int pixel = palette.getPixel(new RGB(rgb >> 16 & 0xFF,
              rgb >> 8 & 0xFF, rgb & 0xFF));
          data.setPixel(x, y, pixel);
          if (colorModel.hasAlpha()) data.setAlpha(x, y, rgb >> 24 & 0xFF);
        }
      return data;
    }
    else if (bufferedImage.getColorModel() instanceof IndexColorModel)
    {
      IndexColorModel colorModel = (IndexColorModel) bufferedImage
          .getColorModel();
      int size = colorModel.getMapSize();
      byte[] reds = new byte[size];
      byte[] greens = new byte[size];
      byte[] blues = new byte[size];
      colorModel.getReds(reds);
      colorModel.getGreens(greens);
      colorModel.getBlues(blues);
      RGB[] rgbs = new RGB[size];
      for (int i = 0; i < rgbs.length; i++)
        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
      PaletteData palette = new PaletteData(rgbs);
      ImageData data = new ImageData(bufferedImage.getWidth(),
          bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
      data.transparentPixel = colorModel.getTransparentPixel();
      WritableRaster raster = bufferedImage.getRaster();
      // int[] pixelArray = new int[data.width];
      int[] pixelArray = new int[1];
      for (int y = 0; y < data.height; y++)
        // {
        // raster.getPixels(0, y, data.width, 1, pixelArray);
        // data.setPixels(0, y, data.width, pixelArray, 0);
        // }
        for (int x = 0; x < data.width; x++)
        {
          raster.getPixel(x, y, pixelArray);
          data.setPixel(x, y, pixelArray[0]);
        }
      return data;
    }
    return null;
  }

  @Override
  protected Image getUIImage()
  {
    return _uiImage;
  }

}
