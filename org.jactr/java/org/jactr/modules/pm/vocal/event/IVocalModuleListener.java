package org.jactr.modules.pm.vocal.event;

/*
 * default logging
 */
import java.util.EventListener;

 
import org.slf4j.LoggerFactory;

public interface IVocalModuleListener extends EventListener
{

  public void vocalizationPrepared(VocalModuleEvent event);
  
  public void vocalizationStarted(VocalModuleEvent event);
  
  public void vocalizationCompleted(VocalModuleEvent event);
  
  public void vocalSystemReset(VocalModuleEvent event);
}
