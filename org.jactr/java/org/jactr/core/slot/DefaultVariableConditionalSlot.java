package org.jactr.core.slot;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;


public class DefaultVariableConditionalSlot extends DefaultConditionalSlot
    implements IMutableVariableNameSlot
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultVariableConditionalSlot.class);
  
  public DefaultVariableConditionalSlot(ISlot slot)
  {
    super(slot);
  }
  
  public DefaultVariableConditionalSlot(String name, Object value)
  {
    super(name, value);
  }

  public DefaultVariableConditionalSlot(String name, int condition, Object value)
  {
    super(name, condition, value);
  }
  
  @Override
  public DefaultVariableConditionalSlot clone()
  {
    return new DefaultVariableConditionalSlot(this);
  }


  public void setName(String name)
  {
    assert name!=null : "Slot names may never be null";
    
    if(!isVariableName()) return;
    setNameInternal(name);
  }


  public boolean isVariableName()
  {
    return getName().startsWith("=");
  }

  /**
   * if the name is still variablized, return false
   * @param test
   * @return
   * @see org.jactr.core.slot.DefaultConditionalSlot#matchesCondition(java.lang.Object)
   */
  @Override
  public boolean matchesCondition(Object test)
  {
    if(isVariableName()) return false;
    return super.matchesCondition(test);
  }
}
