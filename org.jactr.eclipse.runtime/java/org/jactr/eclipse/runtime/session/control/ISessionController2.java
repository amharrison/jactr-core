package org.jactr.eclipse.runtime.session.control;

import org.eclipse.jface.viewers.ITreeContentProvider;

/*
 * default logging
 */

/**
 * extension that adds support for runTo, runFor
 * 
 * @author harrison
 */
public interface ISessionController2 extends ISessionController
{

  public ITreeContentProvider getRunToContentProvider();

  public boolean canRunTo(Object destination);

  public void runTo(Object destination) throws Exception;

  public ITreeContentProvider getRunForContentProvider();

  public boolean canRunFor(Object duration);

  public void runFor(Object duration) throws Exception;

}
