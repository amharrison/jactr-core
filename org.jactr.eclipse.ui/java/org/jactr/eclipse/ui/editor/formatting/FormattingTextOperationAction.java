package org.jactr.eclipse.ui.editor.formatting;

/*
 * default logging
 */
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * different TextOperationAction that formats afterwards. Lifted directly from
 * {@link TextOperationAction}.
 * 
 * @author harrison
 */
public class FormattingTextOperationAction extends TextEditorAction
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                         .getLog(FormattingTextOperationAction.class);

  /** The text operation code */
  private int                        fOperationCode  = -1;

  /** The text operation target */
  private ITextOperationTarget       fOperationTarget;

  /**
   * Indicates whether this action can be executed on read only editors
   * 
   * @since 2.0
   */
  private boolean                    fRunsOnReadOnly = false;

  /**
   * Flag to prevent running twice trough {@link #update()} when creating this
   * action.
   * 
   * @since 3.2
   */
  private boolean                    fAllowUpdate    = false;

  /**
   * Creates and initializes the action for the given text editor and operation
   * code. The action configures its visual representation from the given
   * resource bundle. The action works by asking the text editor at the time for
   * its text operation target adapter (using
   * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
   * operation with the given opcode.
   * 
   * @param bundle
   *          the resource bundle
   * @param prefix
   *          a prefix to be prepended to the various resource keys (described
   *          in <code>ResourceAction</code> constructor), or <code>null</code>
   *          if none
   * @param editor
   *          the text editor
   * @param operationCode
   *          the operation code
   * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
   */
  public FormattingTextOperationAction(ResourceBundle bundle, String prefix,
      ITextEditor editor, int operationCode)
  {
    super(bundle, prefix, editor);
    fOperationCode = operationCode;
    fAllowUpdate = true;
    update();
  }

  /**
   * Creates and initializes the action for the given text editor and operation
   * code. The action configures its visual representation from the given
   * resource bundle. The action works by asking the text editor at the time for
   * its text operation target adapter (using
   * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
   * operation with the given opcode.
   * 
   * @param bundle
   *          the resource bundle
   * @param prefix
   *          a prefix to be prepended to the various resource keys (described
   *          in <code>ResourceAction</code> constructor), or <code>null</code>
   *          if none
   * @param editor
   *          the text editor
   * @param operationCode
   *          the operation code
   * @param runsOnReadOnly
   *          <code>true</code> if action can be executed on read-only files
   * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
   * @since 2.0
   */
  public FormattingTextOperationAction(ResourceBundle bundle, String prefix,
      ITextEditor editor, int operationCode, boolean runsOnReadOnly)
  {
    super(bundle, prefix, editor);
    fOperationCode = operationCode;
    fRunsOnReadOnly = runsOnReadOnly;
    fAllowUpdate = true;
    update();
  }

  /**
   * The <code>TextOperationAction</code> implementation of this
   * <code>IAction</code> method runs the operation with the current operation
   * code.
   */
  @Override
  public void run()
  {
    if (fOperationCode == -1 || fOperationTarget == null) return;

    ITextEditor editor = getTextEditor();
    if (editor == null) return;

    if (!fRunsOnReadOnly && !validateEditorInputState()) return;

    Display display = null;

    IWorkbenchPartSite site = editor.getSite();
    Shell shell = site.getShell();
    if (shell != null && !shell.isDisposed()) display = shell.getDisplay();

    BusyIndicator.showWhile(display, new Runnable() {
      public void run()
      {
        fOperationTarget.doOperation(fOperationCode);
        if (fOperationTarget.canDoOperation(ISourceViewer.FORMAT))
          fOperationTarget.doOperation(ISourceViewer.FORMAT);
      }
    });
  }

  /**
   * The <code>TextOperationAction</code> implementation of this
   * <code>IUpdate</code> method discovers the operation through the current
   * editor's <code>ITextOperationTarget</code> adapter, and sets the enabled
   * state accordingly.
   */
  @Override
  public void update()
  {
    if (!fAllowUpdate) return;

    super.update();

    if (!fRunsOnReadOnly && !canModifyEditor())
    {
      setEnabled(false);
      return;
    }

    ITextEditor editor = getTextEditor();
    if (fOperationTarget == null && editor != null && fOperationCode != -1)
      fOperationTarget = (ITextOperationTarget) editor
          .getAdapter(ITextOperationTarget.class);

    boolean isEnabled = fOperationTarget != null
        && fOperationTarget.canDoOperation(fOperationCode);
    setEnabled(isEnabled);
  }

  /*
   * @see TextEditorAction#setEditor(ITextEditor)
   */
  @Override
  public void setEditor(ITextEditor editor)
  {
    super.setEditor(editor);
    fOperationTarget = null;
  }
}
