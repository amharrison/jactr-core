package org.commonreality.modalities.vocal;

import org.commonreality.efferent.AbstractEfferentCommand;
import org.commonreality.identifier.IIdentifier;

public class VocalizationCommand extends AbstractEfferentCommand
{
  /**
   * 
   */
  private static final long serialVersionUID = 7730175468306832689L;


  
  public VocalizationCommand(IIdentifier identifier)
  {
    super(identifier);
  }
  
  public VocalizationCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    this(identifier);
    setEfferentIdentifier(efferentId);
  }

  
  public void setText(String text)
  {
    setProperty(VocalConstants.VOCALIZATON, text);
  }
  
  public String getText()
  {
    return (String) getProperty(VocalConstants.VOCALIZATON);
  }

}
