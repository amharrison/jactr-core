package org.jactr.eclipse.runtime.ui.visicon;

/*
 * default logging
 */
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jactr.eclipse.runtime.ui.misc.AbstractRuntimeModelViewPart;
import org.jactr.eclipse.runtime.visual.IVisualDescriptorListener;
import org.jactr.eclipse.runtime.visual.IVisualTraceCenterListener;
import org.jactr.eclipse.runtime.visual.VisualDescriptor;
import org.jactr.eclipse.runtime.visual.VisualTraceCenter;

public class VisiconView extends AbstractRuntimeModelViewPart
{

  static public final String         ID     = VisiconView.class.getName();

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VisiconView.class);

  // to be notified of new models
  private IVisualTraceCenterListener _traceCenterListener;

  // to be notified of data changes
  private IVisualDescriptorListener  _descriptorListener;

  private ZoomSliderContribution zoomSlider;
  
  @Override
  public void init(IViewSite site) throws PartInitException
  {
    super.init(site);

    _traceCenterListener = new IVisualTraceCenterListener() {

      public void modelAdded(final String commonName,
          final VisualDescriptor desc)
      {
        Display.getDefault().asyncExec(new Runnable() {

          public void run()
          {
            addModelData(commonName, desc);
          }
        });
      }

      public void modelRemoved(String modelName, final VisualDescriptor desc)
      {
        Display.getDefault().asyncExec(new Runnable() {

          public void run()
          {
            removeModelData(desc);
          }
        });
      }
    };

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

    VisualTraceCenter.get().add(_traceCenterListener);

    /*
     * now we need to add all the existing data. we cant do this in init because
     * the control that contains the components isn't created until
     * createPartControl
     */
    FastList<VisualDescriptor> container = FastList.newInstance();

    for (VisualDescriptor desc : VisualTraceCenter.get()
        .getAllRuntimeTraceData(container))
      addModelData(desc.getModelName(), desc);

    FastList.recycle(container);
    
  }
  
  @Override
protected void configureModelControl() {
	createZoomSlider();
	super.configureModelControl();
}

  private void createZoomSlider() {
	  zoomSlider = new ZoomSliderContribution(getTabFolder(), VisiconComponent.DEFAULT_MAGNIFICATION);
	  getViewSite().getActionBars().getToolBarManager().add(zoomSlider);
	  getViewSite().getActionBars().updateActionBars();
  }

  @Override
  public void dispose()
  {
    VisualTraceCenter.get().remove(_traceCenterListener);
    super.dispose();
  }

  @Override
  protected Composite createModelComposite(String modelName, Object modelData,
      Composite parent)
  {
    if (!(modelData instanceof VisualDescriptor)) return null;

    VisualDescriptor desc = (VisualDescriptor) modelData;
    VisiconComponent comp = new VisiconComponent(parent, SWT.NONE, desc, getMagnification());
    desc.add(_descriptorListener);
    return comp;
  }
  
  private float getMagnification() {
	  if(zoomSlider != null)
		  return zoomSlider.getMagnification();
	  else
		  return VisiconComponent.DEFAULT_MAGNIFICATION;
  }

  @Override
  protected void disposeModelComposite(String modelName, Object modelData,
      Composite content)
  {
    if (modelData != null)
      ((VisualDescriptor) modelData).remove(_descriptorListener);
  }


}
