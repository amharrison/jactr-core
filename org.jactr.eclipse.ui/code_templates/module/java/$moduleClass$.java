package $packageName$;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.model.IModel;
import org.jactr.core.module.AbstractModule;

/**
 * IModule is the entry point to extend an ACT-R model from a theoretical
 * point. Usually, modules are instantiated, have their parameters set and
 * are then attached to the IModel via IModel.install(IModule), which in turn
 * calls IModule.install(IModel). <br>
 * <br>
 * Most behavior is extended by attaching listeners to the model and its contents.
 * Care must be taken when doing this because of the threaded nature of models.<br>
 * <br>
 *
 */
public class $moduleClass$ extends AbstractModule
{

  /**
   * Standard logger used through out jACT-R
   */

  static private final transient Log LOGGER = LogFactory.getLog($moduleClass$.class);
  
  static public final String MODULE_NAME = "$moduleName$";
  
  /**
   * standard 0 argument constructor must always be present.
   * this should do very little.
   *
   */
  public $moduleClass$()
  {
    super(MODULE_NAME);
  }

  /**
   * called after all the chunktypes, chunks, and productions have been installed,
   * but before any instruments or extensions have been installed. If you need to 
   * attach to any other modules it should be done here. However, if you need to 
   * know about production or chunk creation events, you should attach listenes
   * during install(IModel)
   */
  @Override
  public void initialize()
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("initializing "+getClass().getSimpleName());
  }
  
  
  /**
   * 
   * @see org.jactr.core.module.AbstractModule#install(org.jactr.core.model.IModel)
   */
  @Override
  public void install(IModel model)
  {
    super.install(model);
  }
  
  
  /**
   * if you want to install some buffers, replace this code
   */
  protected Collection<IActivationBuffer> createBuffers()
  {
    return Collections.EMPTY_LIST;
  }

  
  /**
   * please, for the love of god, dispose of your resources
   * @see org.jactr.core.module.AbstractModule#dispose()
   */
  @Override
  public void dispose()
  {
    super.dispose();
  }
}


