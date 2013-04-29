package org.jactr.eclipse.ui.editor.document;

import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.partioner.LispPartitionScanner;
import org.jactr.eclipse.ui.editor.partioner.LispPartitions;

public class LispDocumentProvider extends FileDocumentProvider
{

  IDocument document;

  // default documentProvider with modified createDocument()
  public LispDocumentProvider()
  {
    super();
  }

  @Override
  protected IDocument createDocument(Object element) throws CoreException
  {
    document = super.createDocument(element);
    
    /**
     * in case we are opening from somewhere else on the file system outside of
     * the workspace
     */
    if (document == null)
    {
      if (element instanceof IURIEditorInput)
      {
        IURIEditorInput input = (IURIEditorInput) element;
        document = createEmptyDocument();
        try
        {
          setDocumentContent(document, input.getURI().toURL().openStream(),
              null);
        }
        catch (Exception e)
        {
          throw new CoreException(new Status(IStatus.ERROR, UIPlugin.ID,
              "Could not open " + input.getURI(), e));
        }
      }
    }
    
    IDocumentPartitioner partitioner = new FastPartitioner(
        LispPartitionScanner.getInstance(), LispPartitions.ALL_PARTITIONS);

    document.setDocumentPartitioner(partitioner);
    partitioner.connect(document);
    return document;
  }

  /**
   * need to pass back the {@link SynchronizableDocument} so that positions can
   * be protected during concurrent access. a bug in
   * {@link AbstractDocument#getPositions(String)} makes it possible that a
   * position may be null
   */
  protected IDocument createEmptyDocument()
  {
    SynchronizableDocument doc = new SynchronizableDocument();
    doc.setLockObject(new Object());
    return doc;
  }
  
}
