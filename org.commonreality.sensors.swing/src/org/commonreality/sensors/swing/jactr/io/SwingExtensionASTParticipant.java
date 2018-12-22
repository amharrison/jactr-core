package org.commonreality.sensors.swing.jactr.io;

/*
 * default logging
 */
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.swing.jactr.SwingExtension;
import org.commonreality.sensors.swing.jactr.encoders.ButtonComponentVisualEncoder;
import org.commonreality.sensors.swing.jactr.encoders.LabelComponentVisualEncoder;
import org.commonreality.sensors.swing.jactr.encoders.TextFieldComponentVisualEncoder;
import org.jactr.io.participant.impl.BasicASTParticipant;

public class SwingExtensionASTParticipant extends BasicASTParticipant
{

  public SwingExtensionASTParticipant()
  {
    super("org/commonreality/sensors/swing/jactr/io/swing-types.jactr");
    setInstallableClass(SwingExtension.class);
    Map<String, String> params = new TreeMap<String,String>();
    
    /*
     * we add the default encoders here.. these parameters will be
     * injected if they are missing..
     */
    params.put(ButtonComponentVisualEncoder.class.getName(), "TRUE");
    params.put(LabelComponentVisualEncoder.class.getName(), "TRUE");
    params.put(TextFieldComponentVisualEncoder.class.getName(), "TRUE");
    setParameterMap(params);
  }
}
