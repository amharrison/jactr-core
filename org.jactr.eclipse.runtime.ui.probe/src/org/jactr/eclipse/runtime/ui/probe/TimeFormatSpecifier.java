package org.jactr.eclipse.runtime.ui.probe;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.chart.model.attribute.impl.NumberFormatSpecifierImpl;

import com.ibm.icu.util.ULocale;

public class TimeFormatSpecifier extends NumberFormatSpecifierImpl
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TimeFormatSpecifier.class);

  @Override
  public String format(double time, ULocale lo)
  {
    double timeInSeconds = time;
    double hours = Math.floor(timeInSeconds / 3600);
    timeInSeconds -= hours * 3600;
    double minutes = Math.floor(timeInSeconds / 60);
    timeInSeconds -= minutes * 60;

    return String.format("%02.0f:%02.0f:%05.2f", hours, minutes,
        timeInSeconds);
  }

  @Override
  public TimeFormatSpecifier copyInstance()
  {
    return new TimeFormatSpecifier();
  }
}
