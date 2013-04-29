package org.jactr.eclipse.core.concurrent;

/*
 * default logging
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IRunnableWithProgress
{
  public IStatus run(IProgressMonitor monitor);
}
