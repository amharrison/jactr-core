package org.commonreality.sensors.speech;

/*
 * default logging
 */
import java.util.Map;

import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.handlers.ICommandHandlerDelegate;

/**
 * delegate interface to actually perform some vocalization
 * 
 * @author harrison
 */
public interface ISpeaker
{
  /**
   * called to actually invoke a speach event. This is called by
   * {@link VocalizationCommandHandler#start(org.commonreality.efferent.IEfferentCommand, IAgentObject, org.commonreality.sensors.handlers.EfferentCommandHandler)}
   * and as such, has the same restrictions as
   * {@link ICommandHandlerDelegate#start(org.commonreality.efferent.IEfferentCommand, IAgentObject, org.commonreality.sensors.handlers.EfferentCommandHandler)}
   * that
   * {@link EfferentCommandHandler#completed(org.commonreality.efferent.IEfferentCommand, Object)}
   * should <b>not</b> be called from within.
   * 
   * @param speaker
   * @param vocalization
   */
  public void speak(IAgentObject speaker, VocalizationCommand vocalization);

  /**
   * allow the speaker to be configured.
   * 
   * @param sensor
   * @param options
   *          configuration map provided to speech sensor
   */
  public void configure(DefaultSpeechSensor sensor, Map<String, String> options);
}
