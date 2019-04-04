package org.jactr.eclipse.core.parser;

/*
 * default logging
 */
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.collections.impl.factory.Lists;
import org.jactr.io.IOUtilities;
import org.jactr.io.parser.IParserImportDelegate;
import org.jactr.io.participant.impl.BasicASTParticipant;

public class IDEBasicASTParticipant extends BasicASTParticipant
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(IDEBasicASTParticipant.class);

  public IDEBasicASTParticipant(URL modelDescriptor)
  {
    super(modelDescriptor);
  }

  @Override
  protected CommonTree load(URL importModel, IParserImportDelegate importer)
      throws IOException
  {
    // CorePlugin.debug("Attempting to load from " + importModel);

    if (importModel == null) return null;

    List<Exception> warnings = Lists.mutable.empty();
    List<Exception> errors = Lists.mutable.empty();

    CommonTree defaults = IOUtilities.loadModelFile(importModel, importer,
        warnings,
        errors);



    return defaults;
  }
}
