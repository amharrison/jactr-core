package org.commonreality.sensors.swing.jactr;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.swing.jactr.encoders.ButtonComponentVisualEncoder;
import org.commonreality.sensors.swing.jactr.encoders.LabelComponentVisualEncoder;
import org.commonreality.sensors.swing.jactr.encoders.TextFieldComponentVisualEncoder;
import org.jactr.core.extensions.IExtension;
import org.jactr.core.extensions.IllegalExtensionStateException;
import org.jactr.core.model.IModel;
import org.jactr.core.utils.parameter.BooleanParameterHandler;
import org.jactr.core.utils.parameter.ClassNameParameterHandler;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.jactr.modules.pm.common.memory.IPerceptualEncoder;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;

/**
 * basic extension that automatically injects the appropriate encoders into a
 * visual model. The default set of encoders is determined by the SwingExtensionASTParticipant
 * 
 * @author harrison
 */
public class SwingExtension implements IExtension
{

  /**
   * Logger definition
   */
  static private final transient Log     LOGGER    = LogFactory
                                                       .getLog(SwingExtension.class);

  private IModel                         _model;

  private Collection<IPerceptualEncoder> _encodersToInstall = new ArrayList<IPerceptualEncoder>();
  
  public SwingExtension()
  {
    /*
     * create the default set of encoders
     */
//    _encodersToInstall.add(new ButtonComponentVisualEncoder());
//    _encodersToInstall.add(new LabelComponentVisualEncoder());
//    _encodersToInstall.add(new TextFieldComponentVisualEncoder());
  }

  public IModel getModel()
  {
    return _model;
  }

  public String getName()
  {
    return "swing-extension";
  }

  public void install(IModel model)
  {
    _model = model;
  }

  public void uninstall(IModel model)
  {
    _model = null;
  }

  public String getParameter(String key)
  {
    return null;
  }

  public Collection<String> getPossibleParameters()
  {
    return getSetableParameters();
  }

  @SuppressWarnings("unchecked")
  public Collection<String> getSetableParameters()
  {
    return Collections.EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public void setParameter(String key, String value)
  {
    ClassNameParameterHandler cph = ParameterHandler.classInstance();
    BooleanParameterHandler bph = ParameterHandler.booleanInstance();
    if (key.indexOf('.') != -1)
      try
      {
        Class clazz = cph.coerce(key);
        Boolean install = bph.coerce(value);
        
        if(install)
         _encodersToInstall.add((IPerceptualEncoder) clazz.newInstance());
        else
        {
          for(Iterator<IPerceptualEncoder> itr = _encodersToInstall.iterator();itr.hasNext();)
          {
            IPerceptualEncoder encoder = itr.next();
            if(clazz.isAssignableFrom(encoder.getClass()))
              itr.remove();
          }
        }
      }
      catch (Exception e)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Failed set " + key + " = " + value, e);
      }
  }

  public void initialize() throws Exception
  {
    installEncoders(_model);
  }

  protected void installEncoders(IModel model)
  {
    IVisualModule visualModule = (IVisualModule) model
        .getModule(IVisualModule.class);
    if (visualModule == null)
      throw new IllegalExtensionStateException("No IVisualModule installed.");

    IVisualMemory visualMemory = visualModule.getVisualMemory();

    for (IPerceptualEncoder encoder : _encodersToInstall)
      try
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Installing %s", encoder.getClass().getSimpleName()));
        
        visualMemory.addEncoder(encoder);
      }
      catch (Exception e)
      {
        LOGGER.error(String.format("Failed to install encoder %s", encoder), e);
      }
  }

}
