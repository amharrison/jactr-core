package org.jactr.eclipse.runtime.visual;

import org.commonreality.identifier.IIdentifier;


public interface IVisualDescriptorListener
{

  public void added(VisualDescriptor descriptor, IIdentifier identifier);
  public void removed(VisualDescriptor descriptor, IIdentifier identifier);
  public void updated(VisualDescriptor descriptor, IIdentifier identifier);
  public void found(VisualDescriptor descriptor, IIdentifier identifier);
  public void encoded(VisualDescriptor descriptor, IIdentifier identifer);
}
