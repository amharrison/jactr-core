package org.jactr.tools.itr;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io2.compilation.ICompilationUnit;
import org.slf4j.LoggerFactory;

public class ChunkSlotModifier extends AbstractParameterModifier
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkSlotModifier.class);

  static public final String   CHUNK_PATTERN     = "ChunkPattern";

  static public final String   CHUNKTYPE_PATTERN = "ChunkTypePattern";

  protected Pattern                               _chunk;

  protected Pattern                               _chunkType        = Pattern
      .compile(".*");

  @Override
  public String getParameterDisplayName()
  {
    return String.format("%s.%s.%s", _chunkType, _chunk, getParameterName());
  }

  @Override
  public Collection<String> getSetableParameters()
  {
    Collection<String> rtn = super.getSetableParameters();
    rtn.add(CHUNK_PATTERN);
    rtn.add(CHUNKTYPE_PATTERN);
    return rtn;
  }

  @Override
  public void setParameter(String key, String value)
  {
    value = value.toLowerCase();
    if (CHUNK_PATTERN.equalsIgnoreCase(key))
      try
      {
        _chunk = Pattern.compile(value);
      }
      catch (PatternSyntaxException e)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER
              .error(String
                  .format(
                      "Could not compile chunk pattern %s using .* instead ",
                      value), e);
        _chunk = Pattern.compile(".*");
      }
    else if (CHUNKTYPE_PATTERN.equalsIgnoreCase(key))
      try
      {
        _chunkType = Pattern.compile(value);
      }
      catch (PatternSyntaxException e)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER.error(String
              .format(
                  "Could not compile chunktype pattern %s using .* instead ",
                  value), e);
        _chunkType = Pattern.compile(".*");
      }
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
    Map<String, CommonTree> chunktypes = ASTSupport.getMapOfTrees(
        modelDescriptor, JACTRBuilder.CHUNK_TYPE);
    for (String chunkTypeName : chunktypes.keySet())
      if (_chunkType.matcher(chunkTypeName).matches())
      {
        Map<String, CommonTree> chunks = ASTSupport.getMapOfTrees(chunktypes
            .get(chunkTypeName), JACTRBuilder.CHUNK);

        for (String chunkName : chunks.keySet())
          if (_chunk.matcher(chunkName).matches())
          {
            Map<String, CommonTree> slots = ASTSupport.getMapOfTrees(chunks.get(chunkName),
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
  }

}
