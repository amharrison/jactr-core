package org.jactr.eclipse.runtime.visual;

/*
 * default logging
 */

public interface IVisualTraceCenterListener
{

  public void modelAdded(String modelName, VisualDescriptor desc);
  
  public void modelRemoved(String modelName, VisualDescriptor desc);
}
