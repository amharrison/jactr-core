package org.jactr.eclipse.core.parser;

/*
 * default logging
 */
import java.io.IOException;
import java.net.URL;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    FastList<Exception> warnings = FastList.newInstance();
    FastList<Exception> errors = FastList.newInstance();

    CommonTree defaults = IOUtilities.loadModelFile(importModel, importer,
        warnings,
        errors);

    // CorePlugin.debug(String.format("[%d/%d] Load returned ast %s",
    // warnings.size(), errors.size(),
    // defaults != null ? defaults.toStringTree() : null));

    // for (Exception warn : warnings)
    // CorePlugin.debug("load warning : " + warn.getMessage(), warn);
    //
    // for (Exception warn : errors)
    // CorePlugin.debug("load error : " + warn.getMessage(), warn);

    FastList.recycle(warnings);
    FastList.recycle(errors);

    return defaults;
  }
}
