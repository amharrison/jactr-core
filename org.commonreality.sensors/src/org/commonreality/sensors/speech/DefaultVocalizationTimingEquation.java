/**
 * Copyright (C) 1999-2007, Anthony Harrison anh23@pitt.edu This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.sensors.speech;

/*
 * default logging
 */
import java.util.Map;

import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.sensors.handlers.ICommandTimingEquation;
import org.commonreality.time.impl.BasicClock;

 class DefaultVocalizationTimingEquation implements
    ICommandTimingEquation
{
  private final Map<String, String> equationOptions;

  DefaultVocalizationTimingEquation(
      Map<String, String> equationOptions)
  {
    this.equationOptions = equationOptions;
  }

  public double computeTimings(DeltaTracker<IMutableObject> command)
  {
    String text = ((VocalizationCommand) command.get()).getText().trim();
    double charactersPerSecond = 20.0;

    if (equationOptions.containsKey("CharactersPerSecond"))
      try
      {
        charactersPerSecond = Double.parseDouble(equationOptions
            .get("CharactersPerSecond"));
      }
      catch (Exception e)
      {

      }

    double duration = 0;

    if (text.length() != 0)
      duration = text.length() / charactersPerSecond;

    if (duration < 0.01) duration = 0.01;

    duration = BasicClock.constrainPrecision(duration);

    command.setProperty(IEfferentCommand.ESTIMATED_DURATION, duration);
    return duration;
  }
}