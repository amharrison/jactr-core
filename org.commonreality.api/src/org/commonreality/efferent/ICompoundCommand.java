package org.commonreality.efferent;

import java.util.Collection;

/*
 * default logging
 */

public interface ICompoundCommand extends IEfferentCommand
{

  public static final String IS_COMPOUND = "IEfferentCommand.isCompound";
  public static final String COMPONENTS = "IEfferentCommand.components";
  
  public boolean isCompound();
  
  public Collection<IEfferentCommand> getComponents();
  
  public void add(IEfferentCommand command);
}
