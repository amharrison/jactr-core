package org.jactr.eclipse.runtime.ui.command;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.playback.SessionArchive;
import org.jactr.eclipse.runtime.probe3.extract.ProbeDataExtractor;
import org.jactr.eclipse.ui.generic.dialog.ListSelectionDialog;

public class ExtractProbeData extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ExtractProbeData.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil
        .getCurrentSelection(event);

    for (Object selected : selection.toArray())
      if (selected instanceof IFolder)
        try
        {
          IFolder folder = (IFolder) selected;
          if (folder.getName().equalsIgnoreCase("sessionData"))
          {
            Collection<IResource> modelIndices = findModelIndicies(folder);

            Collection<IResource> selectedResources = null;

            // some selection
            if (modelIndices.size() > 1)
              selectedResources = select(modelIndices);
            else if (modelIndices.size() == 1)
              selectedResources = modelIndices;
            else
              return null; // nope

            if (selectedResources != null && selectedResources.size() > 0)
              for (IResource selectedResource : selectedResources)
                extractProbeData(selectedResource);
            else
              RuntimePlugin
                  .error("sessionData folder does not contain sessionData.index file");
          }

        }
        catch (Exception e)
        {
          RuntimePlugin.error("Failed to select valid session archive");
          LOGGER.error("failed to pump it up ", e);
        }

    return null;
  }

  public void extractProbeData(IResource sessionArchiveIndex)
  {
    try
    {
      SessionArchive sa = new SessionArchive(sessionArchiveIndex);
      ProbeDataExtractor pde = new ProbeDataExtractor(sa);

      pde.extract();
    }
    catch (Exception e)
    {
      RuntimePlugin.error(String.format("Failed to replay %s ",
          sessionArchiveIndex.getParent().getName()), e);
    }
  }

  /**
   * throw up a dialog
   * 
   * @param modelIndices
   * @return
   */
  private Collection<IResource> select(Collection<IResource> modelIndices)
  {
    ListSelectionDialog lsd = new ListSelectionDialog(
        Display.getCurrent().getActiveShell(),
        "Select Model(s)",
        "Multiple traces were run in this session. Select traces to extract probe data from",
        modelIndices, this.new ContentProvider(), new LabelProvider() {
          @Override
          public String getText(Object element)
          {
            IResource resource = (IResource) element;
            return resource.getParent().getName();
          }
        });

    lsd.setBlockOnOpen(true);
    lsd.create();
    if (lsd.open() == Window.OK)
    {
      Object[] checked = lsd.getCheckedItems();
      ArrayList<IResource> rtn = new ArrayList<IResource>();
      for (Object obj : checked)
        rtn.add((IResource) obj);
      return rtn;
    }

    return null;
  }

  private Collection<IResource> findModelIndicies(IFolder root)
  {
    Collection<IResource> indicies = new ArrayList<IResource>(2);
    try
    {
      for (IResource resource : root.members())
      {
        IFolder f = (IFolder) resource.getAdapter(IFolder.class);
        if (f != null)
        {
          IResource r = f.findMember("sessionData.index");
          if (r != null) indicies.add(r);
        }
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to find model indices ", e);
    }

    return indicies;
  }

  private class ContentProvider extends ArrayContentProvider implements
      ITreeContentProvider
  {

    @Override
    public Object[] getChildren(Object parentElement)
    {
      return null;
    }

    @Override
    public Object getParent(Object element)
    {
      return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
      return false;
    }

  }
}
