package org.jactr.eclipse.runtime.ui.visicon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.ui.misc.AbstractRuntimeModelViewPart;
import org.jactr.eclipse.runtime.visual.IModelVisiconSessionDataStream;
import org.jactr.eclipse.runtime.visual.IVisualDescriptorListener;
import org.jactr.eclipse.runtime.visual.VisualDescriptor;

public class VisiconView extends AbstractRuntimeModelViewPart
{

  static public final String         ID     = VisiconView.class.getName();

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VisiconView.class);


  // to be notified of data changes
  private IVisualDescriptorListener  _descriptorListener;

  private ZoomSliderContribution zoomSlider;
  
  @Override
  public void init(IViewSite site) throws PartInitException
  {
    super.init(site);

    _descriptorListener = new IVisualDescriptorListener() {

      public void added(final VisualDescriptor descriptor,
          final IIdentifier identifier)
      {
        Display.getDefault().asyncExec(new Runnable() {
          public void run()
          {
            CTabItem tab = getModelTab(descriptor.getModelName());
            VisiconComponent vComp = (VisiconComponent) tab.getControl();
            vComp.add(identifier, descriptor.getData(identifier));
          }
        });
      }

      public void encoded(final VisualDescriptor descriptor,
          final IIdentifier identifier)
      {
        Display.getDefault().asyncExec(new Runnable() {
          public void run()
          {
            CTabItem tab = getModelTab(descriptor.getModelName());
            VisiconComponent vComp = (VisiconComponent) tab.getControl();
            vComp.encoded(identifier);
          }
        });
      }

      public void found(final VisualDescriptor descriptor,
          final IIdentifier identifier)
      {
        Display.getDefault().asyncExec(new Runnable() {
          public void run()
          {
            CTabItem tab = getModelTab(descriptor.getModelName());
            VisiconComponent vComp = (VisiconComponent) tab.getControl();
            if (tab != null) vComp.found(identifier);
          }
        });
      }

      public void removed(final VisualDescriptor descriptor,
          final IIdentifier identifier)
      {
        Display.getDefault().asyncExec(new Runnable() {
          public void run()
          {
            CTabItem tab = getModelTab(descriptor.getModelName());
            VisiconComponent vComp = (VisiconComponent) tab.getControl();
            vComp.remove(identifier);
          }
        });
      }

      public void updated(final VisualDescriptor descriptor,
          final IIdentifier identifier)
      {
        Display.getDefault().asyncExec(new Runnable() {
          public void run()
          {
            CTabItem tab = getModelTab(descriptor.getModelName());
            // if (tab == null) return;
            VisiconComponent vComp = (VisiconComponent) tab.getControl();
            vComp.update(identifier, descriptor.getData(identifier));
          }
        });

      }

    };

  }

  @Override
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);
  }
  
  @Override
protected void configureModelControl() {
	createZoomSlider();
	super.configureModelControl();
}

  private void createZoomSlider() {
    zoomSlider = new ZoomSliderContribution(getTabFolder(),
        VisiconComponent.DEFAULT_MAGNIFICATION);
    getViewSite().getActionBars().getToolBarManager().add(zoomSlider);
    getViewSite().getActionBars().updateActionBars();
  }

  @Override
  public void dispose()
  {
    super.dispose();
  }

  @Override
  protected void newSessionData(ISessionData sessionData)
  {
    deferAdd(sessionData.getModelName(), sessionData, 250);
  }

  private float getMagnification()
  {
    if (zoomSlider != null)
      return zoomSlider.getMagnification();
    else
      return VisiconComponent.DEFAULT_MAGNIFICATION;
  }

  @Override
  protected Composite createModelComposite(String modelName, Object modelData,
      Composite parent)
  {

    ISessionData sessionData = (ISessionData) modelData;
    IModelVisiconSessionDataStream mvsds = (IModelVisiconSessionDataStream) sessionData
        .getDataStream("visicon");
    VisualDescriptor descriptor = null;
    if (mvsds != null) descriptor = mvsds.getRoot();

    if (mvsds == null || descriptor == null)
    {
      if (sessionData.isOpen())
      {
        if (!wasDeferred(modelData))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Deferring add of %s", modelName));
          deferAdd(modelName, modelData, 500);
        }
        else
        {
          if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format(
              "%s Was previous deferred, assuming no data is coming.",
              modelName));
          removeDeferred(modelData);
        }

      }
      else
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug(
            String.format("Session data is closed, ignoring %s", modelName));

        removeDeferred(modelData);
      }
      return null;
    }

    VisiconComponent comp = new VisiconComponent(parent, SWT.NONE, descriptor,
        getMagnification());
    descriptor.add(_descriptorListener);
    return comp;
  }

  @Override
  protected void disposeModelComposite(String modelName, Object modelData,
      Composite content)
  {
    ISessionData sessionData = (ISessionData) modelData;
    IModelVisiconSessionDataStream mvsds = (IModelVisiconSessionDataStream) sessionData
        .getDataStream("visicon");
    VisualDescriptor descriptor = null;
    if (mvsds != null) descriptor = mvsds.getRoot();
    if (descriptor != null) descriptor.remove(_descriptorListener);
    content.dispose();
  }


}
