package $packageName$;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.object.manager.event.IAfferentListener;
import org.commonreality.object.manager.event.IEfferentListener;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.model.IModel;
import org.jactr.core.model.event.IModelListener;
import org.jactr.core.model.event.ModelEvent;
import org.jactr.core.model.event.ModelListenerAdaptor;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.AbstractPerceptualModule;

/**
 * IModule is the entry point to extend an ACT-R model from a theoretical point.
 * Usually, modules are instantiated, have their parameters set and are then
 * attached to the IModel via IModel.install(IModule), which in turn calls
 * IModule.install(IModel). <br>
 * <br>
 * Most behavior is extended by attaching listeners to the model and its
 * contents. Care must be taken when doing this because of the threaded nature
 * of models.<br>
 * <br>
 */
public class $moduleClass$ extends AbstractPerceptualModule
{

  /**
   * Standard logger used through out jACT-R
   */

  static private final transient Log   LOGGER      = LogFactory
                                                       .getLog($moduleClass$.class);

  static public final String           MODULE_NAME = "$moduleName$";

  /**
   * if you need to be notified when an IAfferentObject is added, modified, or removed
   * you've instantiate one of these and attach it down in #install
   */
  private IAfferentListener _afferentListener;
  
  /**
   * if you need to be notified when an IEfferentObject is aded,removed or modified
   * you'd instantiate one of these and attach it down in #install
   */
  private IEfferentListener _efferentListener;

  /**
   * standard 0 argument constructor must always be present. this should do very
   * little.
   */
  public $moduleClass$()
  {
    super(MODULE_NAME);
  }

  /**
   * called after all the chunktypes, chunks, and productions have been
   * installed, but before any instruments or extensions have been installed. If
   * you need to attach to any other modules it should be done here. However, if
   * you need to know about production or chunk creation events, you should
   * attach listenes during install(IModel)
   */
  @Override
  public void initialize()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("initializing " + getClass().getSimpleName());
  }

  /**
   * @see org.jactr.core.module.AbstractModule#install(org.jactr.core.model.IModel)
   */
  @Override
  public void install(IModel model)
  {
    super.install(model);

    IModelListener startUp = new ModelListenerAdaptor() {

      /**
       * called once the connection to common reality is established. Once this
       * occurs, we can get access to the common reality executor
       * 
       * @see org.jactr.core.model.event.ModelListenerAdaptor#modelConnected(org.jactr.core.model.event.ModelEvent)
       */
      public void modelConnected(ModelEvent event)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Connected to common reality, attaching listeners");

        

        /*
         * all AbstractPerceptualModules within a single model share a common
         * executor (on a separate thread) that is to be used to process events
         * coming from Common Reality. This executor is only available after
         * modelConnected()
         */
        Executor executor = getCommonRealityExecutor();

        /*
         * the agent interface is how we communicate with common reality
         */
        IAgent agentInterface = ACTRRuntime.getRuntime()
            .getConnector().getAgent(event.getSource());
        
        /*
         * now, whenever an event comes from common reality to
         * the agent interface, we will receive notification of the changes
         * on the common reality executor thread.
         */
        //agentInterface.addListener(_afferentListener, executor);
        //agentInterface.addListener(_efferentListener, executor);
      }
    };

    /*
     * we attach this listener with the inline executor - i.e. it will be called
     * on the same thread that issued the event (the ModelThread), immediately
     * after it occurs.
     */
    model.addListener(startUp, ExecutorServices.INLINE_EXECUTOR);
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
   * 
   * @see org.jactr.core.module.AbstractModule#dispose()
   */
  @Override
  public void dispose()
  {
    super.dispose();
  }
}
