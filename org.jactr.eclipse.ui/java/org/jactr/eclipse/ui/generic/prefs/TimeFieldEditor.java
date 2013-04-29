package org.jactr.eclipse.ui.generic.prefs;

/*
 * default logging
 */
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

/**
 * loads a long from the preference store as a Date() relative to epoch.
 * Uses {@link DateTime} to control the time.
 * @author harrison
 *
 */
public class TimeFieldEditor extends FieldEditor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TimeFieldEditor.class);
  
  private Calendar _timeValue = Calendar.getInstance();
  private DateTime _timeControl;
  
  public TimeFieldEditor(String preferenceName, String label, Composite parent)
  {
    super(preferenceName, label, parent);
  }

  @Override
  protected void adjustForNumColumns(int numColumns)
  {
    GridData gd = (GridData) _timeControl.getLayoutData();
    gd.horizontalSpan = numColumns - 1;
    // We only grab excess space if we have to
    // If another field editor has more columns then
    // we assume it is setting the width.
    gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
  }

  @Override
  protected void doFillIntoGrid(Composite parent, int numColumns)
  {
    getLabelControl(parent);
    
    if(_timeControl==null)
      _timeControl = new DateTime(parent, SWT.TIME | SWT.SHORT);
    
    GridData gd = new GridData();
    gd.horizontalSpan = numColumns - 1;
    gd.horizontalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    _timeControl.setLayoutData(gd);
  }

  protected void set(long timeMS)
  {
    _timeValue.setTimeInMillis(timeMS);
    _timeControl.setHours(_timeValue.get(Calendar.HOUR_OF_DAY));
    _timeControl.setMinutes(_timeValue.get(Calendar.MINUTE));
    _timeControl.setSeconds(_timeValue.get(Calendar.SECOND));    
  }
  
  public void setEnabled(boolean enabled, Composite parent)
  {
    super.setEnabled(enabled, parent);
    _timeControl.setEnabled(enabled);
  }
  
  @Override
  protected void doLoad()
  {
    set(getPreferenceStore().getLong(getPreferenceName()));
  }

  @Override
  protected void doLoadDefault()
  {
    set(getPreferenceStore().getDefaultLong(getPreferenceName()));
  }

  @Override
  protected void doStore()
  {
    _timeValue.set(Calendar.HOUR_OF_DAY, _timeControl.getHours());
    _timeValue.set(Calendar.MINUTE, _timeControl.getMinutes());
    _timeValue.set(Calendar.SECOND, _timeControl.getSeconds());
    
    getPreferenceStore().setValue(getPreferenceName(), _timeValue.getTimeInMillis());
  }

  @Override
  public int getNumberOfControls()
  {
    return 2;
  }

}
