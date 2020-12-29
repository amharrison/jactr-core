package org.jactr.modules.pm.visual.memory.impl.encoder;

import org.commonreality.modalities.visual.IVisualPropertyHandler;

public class GUIEncoder extends ExtensibleVisualEncoder
{

  public GUIEncoder(String chunkTypeName, String crType)
  {
	  super(chunkTypeName, crType);
	  addFeatureHandler(IVisualPropertyHandler.TEXT, "text");
  }

}
