package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.jactr.eclipse.runtime.session.ISession;

public interface ISessionSelectionProvider
{

  public void addListener(ISessionSelectionListener listener, Executor executor);

  public void removeListener(ISessionSelectionListener listener);

  public void select(ISession session);

  public ISession getSelection();
}
