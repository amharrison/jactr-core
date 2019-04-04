package org.jactr.eclipse.runtime.ui.launch;

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;

public class ACTRLaunchShortcut implements ILaunchShortcut, ILaunchShortcut2
{

  public ACTRLaunchShortcut()
  {

  }

  @Override
  public void launch(ISelection selection, String mode)
  {
    buildLaunchConfiguration(getLaunchableResource(selection), mode)
        .ifPresent(lc -> {
          try
          {
            lc.launch(mode, new NullProgressMonitor());
          }
          catch (CoreException e)
          {
          }
        });
  }

  @Override
  public void launch(IEditorPart editor, String mode)
  {
    buildLaunchConfiguration(getLaunchableResource(editor), mode)
        .ifPresent(lc -> {
          try
          {
            lc.launch(mode, new NullProgressMonitor());
          }
          catch (CoreException e)
          {
          }
        });
  }

  protected Optional<ILaunchConfiguration> buildLaunchConfiguration(
      IResource resource, String mode)
  {
    try
    {
      ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType type = manager
          .getLaunchConfigurationType(ACTRSession.LAUNCH_TYPE);

      ILaunchConfigurationWorkingCopy working = type.newInstance(null,
          resource.getName());

      working.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
          resource.getProject().getName());

      working.setAttribute(ACTRLaunchConstants.ATTR_MODEL_FILES,
          resource.getProjectRelativePath().toString());
      working.setAttribute(ACTRLaunchConstants.ATTR_MODEL_ALIASES
          + resource.getFullPath().toOSString(), "model");

      ACTRLaunchConfigurationUtils.setupPermanentAttributes(working);
      ACTRLaunchConfigurationUtils.setupPersistentAttributes(working);

      return Optional.of(working.doSave());
    }
    catch (Exception e)
    {
      return Optional.empty();
    }
  }

  protected ILaunchConfiguration[] getLaunchConfigurationsReferencing(
      IResource resource)
  {
    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type = manager
        .getLaunchConfigurationType(ACTRSession.LAUNCH_TYPE);

    ArrayList<ILaunchConfiguration> rtn = new ArrayList<>();
    try
    {
      ILaunchConfiguration[] configurations = manager
          .getLaunchConfigurations(type);
      // now find all those that reference the selection

      for (ILaunchConfiguration config : configurations)
        if (ACTRLaunchConfigurationUtils.getModelFiles(config)
            .contains(resource))
          rtn.add(config);
    }
    catch (CoreException e)
    {

    }
    return rtn.stream().toArray(ILaunchConfiguration[]::new);
  }

  @Override
  public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection)
  {
    IResource launchable = getLaunchableResource(selection);

    return getLaunchConfigurationsReferencing(launchable);
  }

  @Override
  public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart)
  {
    IResource launchable = getLaunchableResource(editorpart);
    return getLaunchConfigurationsReferencing(launchable);
  }

  @Override
  public IResource getLaunchableResource(ISelection selection)
  {
    Object element = ((StructuredSelection) selection).getFirstElement();
    if (element instanceof IFile) return (IResource) element;
    return null;
  }

  @Override
  public IResource getLaunchableResource(IEditorPart editorpart)
  {
    if (editorpart instanceof ACTRModelEditor)
      return ((ACTRModelEditor) editorpart).getResource();
    return null;
  }

}
