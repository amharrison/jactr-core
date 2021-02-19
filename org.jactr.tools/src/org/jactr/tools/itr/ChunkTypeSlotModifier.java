package org.jactr.tools.itr;

/*
 * default logging
 */
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io2.compilation.ICompilationUnit;
import org.slf4j.LoggerFactory;

public class ChunkTypeSlotModifier extends AbstractParameterModifier
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkTypeSlotModifier.class);

  static public final String         CHUNK_TYPE = "ChunkType";

  protected String                                _chunkTypeName;

  @Override
  public void setParameter(String key, String value)
  {
    if (CHUNK_TYPE.equalsIgnoreCase(key))
      _chunkTypeName = value;
    else
      super.setParameter(key, value);
  }

  @Override
  protected void setParameter(ICompilationUnit modelDescriptor,
      String parameter, String value)
  {
    if (modelDescriptor.getAST() instanceof CommonTree)
      setParameter((CommonTree) modelDescriptor.getAST(), parameter, value);
    else
      throw new RuntimeException("not implemented yet");
  }

  protected void setParameter(CommonTree modelDescriptor, String parameter,
      String value)
  {
    Map<String, CommonTree> chunkTypes = ASTSupport.getMapOfTrees(
        modelDescriptor, JACTRBuilder.CHUNK_TYPE);
    CommonTree chunkTypeDesc = chunkTypes.get(_chunkTypeName);
    if (chunkTypeDesc == null) return;

    Map<String, CommonTree> slots = ASTSupport.getMapOfTrees(chunkTypeDesc,
        JACTRBuilder.SLOT);
    CommonTree slotDesc = slots.get(getParameterName());
    if (slotDesc == null) return;

    /*
     * try as a number first, then string. compiler should resolve the string if
     * it is a chunk identifier
     */
    slotDesc.deleteChild(2);
    ASTSupport support = new ASTSupport();
    try
    {
      Double.parseDouble(value);
      slotDesc.addChild(support.create(JACTRBuilder.NUMBER, value));
    }
    catch (NumberFormatException nfe)
    {
      slotDesc.addChild(support.create(JACTRBuilder.STRING, value));
    }
  }

}
