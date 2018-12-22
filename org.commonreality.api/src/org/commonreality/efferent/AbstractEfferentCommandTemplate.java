package org.commonreality.efferent;

/*
 * default logging
 */
import org.commonreality.agents.IAgent;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.manager.IRequestableObjectManager;

public abstract class AbstractEfferentCommandTemplate<E extends IEfferentCommand> implements
    IEfferentCommandTemplate<E>
{

  /**
   * 
   */
  private static final long serialVersionUID = -3122149482402292761L;


  private String _name;
  private String _description;
  
  public AbstractEfferentCommandTemplate(String name, String description)
  {
    _name = name;
    _description = description;
  }
  
  public String getDescription()
  {
    return _description;
  }

  public String getName()
  {
    return _name;
  }
  
  abstract protected E create(IIdentifier commandId, IIdentifier muscleId);
  
  abstract protected void configure(E command, IAgent agent, IEfferentObject object);
  
  
  public E instantiate(IAgent agent, IEfferentObject object)
      throws Exception
  {
    
    /**
     * first, we need to get a unique ID for the IEfferentCommand and 
     * we do this by snagging the IEfferentCommandManager
     */
    IEfferentCommandManager ecm = agent.getEfferentCommandManager();
    if(!(ecm instanceof IRequestableObjectManager))
      throw new IllegalStateException("IEfferentCommandManager is not requetable, no clue why. Can't create command");
    
    IIdentifier commandId = ((IRequestableObjectManager)ecm).requestIdentifier(object.getIdentifier().getSensor());
    
    E command = create(commandId, object.getIdentifier());
    configure(command, agent, object);
    
    return command;
  }
}
