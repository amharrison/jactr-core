package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.probe2.ModelProbeData;

public class SWTProbeContainer extends AbstractBIRTProbeContainer
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SWTProbeContainer.class);

  private Image                      _chartImage;

  private Image                      _uiImage;

  private GC                         _chartContext;

  public SWTProbeContainer(Composite parent, ModelProbeData mpd)
  {
    super(parent, mpd);
  }

  @Override
  protected void updateRenderContext()
  {
    try
    {
      _contextLock.lock();

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Updating render context"));

      Image old = _uiImage;

      if (_chartImage != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Disposing chart image"));

        _chartImage.dispose();
      }

      if (_chartContext != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Disposing chart context"));

        _chartContext.dispose();
      }

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

      _uiImage = new Image(getDisplay(), bounds);

      if (old != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Disposing old images"));
        GC tmp = new GC(_uiImage);
        tmp.drawImage(old, 0, 0);
        old.dispose();
        tmp.dispose();
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("creating new images and context"));

      _chartImage = new Image(getDisplay(), bounds);
      _chartContext = new GC(_chartImage);

      _chartBounds = BoundsImpl.create(0, 0, bounds.width, bounds.height);
      _chartBounds.scale(72d / _chartRenderer.getDisplayServer()
          .getDpiResolution());

      _chartRenderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT,
          _chartContext);

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

      if (_uiImage == null || _chartImage == null || _uiImage.isDisposed()
          || _chartImage.isDisposed())
        if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format("null or disposed images detected? WTF?"));

      Image tmp = _uiImage;
      _uiImage = _chartImage;
      _chartImage = tmp;

      if (_chartContext != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("disposing chartContext"));
        _chartContext.dispose();
      }

      _chartContext = new GC(_chartImage);

      _chartRenderer.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT,
          _chartContext);
    }
    finally
    {
      _contextLock.unlock();
    }
  }

  @Override
  public void dispose()
  {
    if (_uiImage != null && !_uiImage.isDisposed()) _uiImage.dispose();
    if (_chartImage != null && !_chartImage.isDisposed())
      _chartImage.dispose();
    super.dispose();
  }

  @Override
  protected Image getUIImage()
  {
    return _uiImage;
  }

  @Override
  protected IDeviceRenderer getDeviceRenderer()
  {
    try
    {
      return ChartEngine.instance(new PlatformConfig()).getRenderer("dv.SWT");
    }
    catch (ChartException e)
    {
      LOGGER.error(
          "AWTProbeContainer.getDeviceRenderer threw ChartException : ", e);
      return null;
    }
  }
}
