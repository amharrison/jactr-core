
package $packageName$;

import java.util.Map;
import java.util.TreeMap;

import org.jactr.io.participant.impl.BasicASTParticipant;

/**
 * The IASTParticipant is responsible for providing IASTInjector and IASTTrimmers,
 * which modify the abstract syntax trees describing models. This participant
 * takes the location of a model descriptor (with no modules) and installs the contents
 * into the model passed to it.<br>
 * <br>
 * All you need to do is create the model file and set its location to DEFAULT_LOCATION<br>
 * <br>
 * If your module has parameters (implements IParameterized), you can set the
 * default values via createParameterMap()
 */
public class ModuleASTParticipant extends BasicASTParticipant
{

  /**
   * default location of the model content to import or trim
   */
  static private final String DEFAULT_LOCATION = "$packageLocation$/module-content.jactr";
  
  /**
   * must be a zero arg constructor
   *
   */
  public ModuleASTParticipant()
  {
    super(ModuleASTParticipant.class.getClassLoader().getResource(DEFAULT_LOCATION));
    setModuleClass($moduleClass$.class);
    setParameterMap(createParameterMap());
  }

  private Map<String,String> createParameterMap()
  {
    return new TreeMap<String,String>();
  }
}


