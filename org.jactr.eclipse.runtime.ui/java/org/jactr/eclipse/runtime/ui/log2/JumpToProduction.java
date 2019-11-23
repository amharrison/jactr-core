package org.jactr.eclipse.runtime.ui.log2;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentUtil;
import org.jactr.core.logging.Logger;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.log2.LogData;
import org.jactr.eclipse.runtime.session.ILocalSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.ui.selection.SessionTimeSelection;
import org.jactr.io2.jactr.modelFragment.Production;

public class JumpToProduction extends Action
{

  private TableViewer _tableViewer;

  public JumpToProduction(TableViewer tableViewer)
  {
    super("Jump to production");
    _tableViewer = tableViewer;
  }

  public String getModelName()
  {
    /*
     * get the current timed selection
     */
    ISelectionProvider service = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
        .getSelectionProvider();

    SessionTimeSelection selection = (SessionTimeSelection) service
        .getSelection();
    return selection.getModelName();
  }

  public String getCurrentProduction(Event event)
  {
    IStructuredSelection selection = (IStructuredSelection) _tableViewer
        .getSelection();

    LogData logData = (LogData) selection.getFirstElement();

    String productionLog = logData.get(Logger.Stream.PROCEDURAL.name());
    // find text that matches: "Fired ...."
    int firedIndex = productionLog.lastIndexOf("Fired ");
    if (firedIndex > 0)
    {
      int start = "Fired ".length() + firedIndex;
      String productionName = productionLog.substring(start,
          productionLog.length() - 1); // trim new line
      return productionName;
    }

    return null;
  }

  public ISession getSession()
  {
    /*
     * get the current timed selection
     */
    ISelectionProvider service = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
        .getSelectionProvider();

    SessionTimeSelection selection = (SessionTimeSelection) service
        .getSelection();
    return selection.getSession();
  }

  public IResource getResource(ISession session, String modelName)
  {
    if (session instanceof ILocalSession) try
    {
      ILaunch launch = ((ILocalSession) session).getLaunch();
      Collection<IResource> files = ACTRLaunchConfigurationUtils
          .getModelFiles(launch.getLaunchConfiguration());
      for (IResource modelFile : files)
        if (ACTRLaunchConfigurationUtils
            .getModelAliases(modelFile, launch.getLaunchConfiguration())
            .contains(modelName))
          return modelFile;
    }
    catch (CoreException ce)
    {
      RuntimePlugin.error("", ce);
    }
    return null;
  }

  public IEditorPart openEditor(IResource resource)
  {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();
    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
        .getDefaultEditor(resource.getName());

    try
    {
      return page.openEditor(new FileEditorInput((IFile) resource),
          desc.getId());
    }
    catch (PartInitException e)
    {
      RuntimePlugin.error("", e);
    }
    return null;

  }

  public IEditorPart openEditor(Resource resource)
  {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();
    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
        .getDefaultEditor(resource.getURI().lastSegment());

    try
    {
      String path = resource.getURI().toPlatformString(true);
      IResource actualFile = ResourcesPlugin.getWorkspace().getRoot()
          .findMember(path);
      if (actualFile != null) return page
          .openEditor(new FileEditorInput((IFile) actualFile), desc.getId());
      return null;
    }
    catch (PartInitException e)
    {
      RuntimePlugin.error("", e);
    }
    return null;

  }

  public void selectProduction(IEditorPart part, final String productionName)
  {
    if (!(part instanceof XtextEditor)) return;
    final XtextEditor editor = (XtextEditor) part;

    XtextDocumentUtil util = new XtextDocumentUtil();

    IXtextDocument document = util.getXtextDocument(editor);
    document.readOnly((resource) -> {
      return selectProduction(resource, productionName, editor);
    });
  }

  protected Object selectProduction(XtextResource resource,
      String productionName, XtextEditor editor)
  {
    for (Resource res : resource.getResourceSet().getResources())
      res.getAllContents().forEachRemaining(obj -> {
        EcoreUtil2.getAllContentsOfType(obj, Production.class)
            .forEach(production -> {
              if (productionName.equals(production.getName()))
              {
                INode objectNode = NodeModelUtils.findActualNodeFor(production);
                if (objectNode != null) ((XtextEditor) openEditor(production.eResource()))
                    .selectAndReveal(objectNode.getOffset(), 0);
              }
            });
      });

    return null;
  }

  @Override
  public void runWithEvent(Event event)
  {
    String productionName = getCurrentProduction(event);
    if (productionName == null) return;

    ISession session = getSession();
    String modelName = getModelName();

    IResource resource = getResource(session, modelName);
    if (resource != null)
    {
      IEditorPart part = openEditor(resource);
      if (part != null) selectProduction(part, productionName);
    }
  }

}
