package org.jactr.eclipse.ui.generic.dialog;

/*
 * default logging
 */
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.content.FlatTreeContentProvider;

public class ActivationBufferSelectionDialog extends ListSelectionDialog
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ActivationBufferSelectionDialog.class);

  public ActivationBufferSelectionDialog(Shell parentShell, String title,
      String message, Collection<CommonTree> buffers)
  {
    super(parentShell, title, message, buffers,
        new FlatTreeContentProvider(), new ACTRLabelProvider());
  }


}
