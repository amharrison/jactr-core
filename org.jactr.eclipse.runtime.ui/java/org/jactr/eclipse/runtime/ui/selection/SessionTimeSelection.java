package org.jactr.eclipse.runtime.ui.selection;

/*
 * default logging
 */
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jactr.eclipse.runtime.session.ISession;

/**
 * structured selection for a session/time pair.
 * 
 * @author harrison
 */
public class SessionTimeSelection implements IStructuredSelection
{
  /**
   * Logger definition
   */
  static private final transient Log       LOGGER = LogFactory
                                                      .getLog(SessionTimeSelection.class);

  static public final SessionTimeSelection EMPTY  = new SessionTimeSelection(
                                                      null, null, Double.NaN);

  private ISession                         _session;

  private String                           _model;

  private double                           _time  = Double.NaN;


  public SessionTimeSelection(ISession session, String modelName,
 double time)
  {
    _session = session;
    _model = modelName;
    _time = time;
  }

  public boolean isEmpty()
  {
    return _session == null && _model == null && Double.isNaN(_time);
  }

  /**
   * returns the session
   */
  public Object getFirstElement()
  {
    return _session;
  }

  public ISession getSession()
  {
    return _session;
  }

  public String getModelName()
  {
    return _model;
  }

  public double getTime()
  {
    return _time;
  }


  public Iterator iterator()
  {
    return toList().iterator();
  }

  public int size()
  {
    if (isEmpty()) return 0;
    return 2;
  }

  public Object[] toArray()
  {
    if (isEmpty()) return new Object[0];

    return new Object[] { _session, _model, _time };
  }

  public List toList()
  {
    return Arrays.asList(toArray());
  }

}
