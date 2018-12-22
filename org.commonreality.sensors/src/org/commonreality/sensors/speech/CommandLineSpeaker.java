package org.commonreality.sensors.speech;

/*
 * default logging
 */
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.object.IAgentObject;

public class CommandLineSpeaker implements ISpeaker
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER    = LogFactory
                                                   .getLog(CommandLineSpeaker.class);

  static public final String         PATH      = "Path";

  static public final String         ARGUMENTS = "Arguments";

  static public final String         SCRIPT    = "${script}";

  private String                     _path;

  private String                     _arguments;

  /*
   * platform specific defaults (non-Javadoc)
   * @see
   * org.commonreality.sensors.speech.ISpeaker#configure(org.commonreality.sensors
   * .speech.DefaultSpeechSensor, java.util.Map)
   */

  public CommandLineSpeaker()
  {
    guessDefaults();
  }

  private void guessDefaults()
  {
    String os = System.getProperty("os.name");
    if (os.contains("Mac"))
      macDefaults();
    else if (os.contains("Windows"))
      winDefaults();
    else
      nixDefaults();
  }

  private void macDefaults()
  {
    nixDefaults();
  }

  private void winDefaults()
  {
    _path = "C:\\Progra~1\\Cepstral\\bin\\swift.exe";
    _arguments = "";
  }

  private void nixDefaults()
  {
    _path = "/usr/bin/say";
    _arguments = "";
  }

  /**
   * configure the speaker with path and arguments. sensor can be null
   */
  public void configure(DefaultSpeechSensor sensor, Map<String, String> options)
  {
    if (options.containsKey(PATH)) _path = options.get(PATH);
    if (options.containsKey(ARGUMENTS)) _arguments = options.get(ARGUMENTS);
  }

  /**
   * fire off a balistic speech. if you want to track it, use #execute(String)
   */
  public void speak(IAgentObject speaker, VocalizationCommand vocalization)
  {
    execute(vocalization.getText());
  }

  /**
   * speak text
   * 
   * @param text
   * @return
   */
  public Process execute(String text)
  {
    StringBuilder command = new StringBuilder(_path);
    command.append(" ");

    int scriptIndex = _arguments.indexOf(SCRIPT);
    // default.. just quote and append
    if (scriptIndex < 0)
      command.append("\"").append(text).append("\"");
    else
      // otherwise, we assume they know what they are doing
      command.append(_arguments.replaceAll(SCRIPT, text));
    try
    {
      return Runtime.getRuntime().exec(command.toString());
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("CommandLineSpeaker.execute threw IOException : ", e);
      return null;
    }
  }

}
