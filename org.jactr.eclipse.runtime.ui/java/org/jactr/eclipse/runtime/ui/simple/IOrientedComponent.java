package org.jactr.eclipse.runtime.ui.simple;

/*
 * default logging
 */
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.session.ISession;

public interface IOrientedComponent
{

  public void dispose();

  public void createPartControl(Composite parent);

  public void setData(ISession session, String modelName, double time,
      boolean isPostConflictResolution);

  public void noAST();

  public void setFocus();
}
