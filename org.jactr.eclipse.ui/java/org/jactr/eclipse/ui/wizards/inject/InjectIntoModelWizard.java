package org.jactr.eclipse.ui.wizards.inject;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.ModelExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.core.parser.ProjectSensitiveParserImportDelegate;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionDescriptorLabelProvider;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionWizardPage;
import org.jactr.eclipse.ui.wizards.pages.ToolsExplanationWizardPage;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io.antlr3.misc.DetailedCommonTree;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;
import org.jactr.io.participant.ASTParticipantRegistry;
import org.jactr.io.participant.IASTInjector;
import org.jactr.io.participant.IASTParticipant;
import org.jactr.io.participant.impl.BasicASTInjector;

public class InjectIntoModelWizard extends Wizard implements IWorkbenchWizard
{
  /**
   * Logger definition
   */
  static private final transient Log            LOGGER       = LogFactory
                                                                 .getLog(InjectIntoModelWizard.class);

  private IProjectCompilationUnit               _compilationUnit;

  private Collection<CommonExtensionWizardPage> _commonPages = new ArrayList<CommonExtensionWizardPage>();

  public InjectIntoModelWizard()
  {
  }

  @Override
  public void addPages()
  {
    addPage(new ToolsExplanationWizardPage(
        "toolsExp",
        "Library of Tools",
        "On the following pages you will select what tools you'd like your project to use.\nYou can always change your mind later.",
        "jACT-R uses modular bundles of code to contribute or change your model's behavior.\nYou need to select those tools in order to use them."
            + "\nThis is a convenience to avoid having to directly edit your projects dependencies."));

    CommonExtensionWizardPage inst = new CommonExtensionWizardPage(
        () -> ModuleRegistry.getRegistry().getAllDescriptors(),
        new CommonExtensionDescriptorLabelProvider(), "module", "Modules",
        "Select modules you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);

    inst = new CommonExtensionWizardPage(
        () -> org.jactr.eclipse.core.bundles.registry.ExtensionRegistry
            .getRegistry().getAllDescriptors(),
        new CommonExtensionDescriptorLabelProvider(), "ext", "Extensions",
        "Select runtime extensions that you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);

  }

  @Override
  public boolean performFinish()
  {
    IProject project = _compilationUnit.getResource().getProject();
    /*
     * make sure everyone is closed.. This is done here, otherwise we can get
     * some weird unmodifiable messages.
     */
    for (CommonExtensionWizardPage extPage : _commonPages)
      extPage.ensureDependencies(project);

    ProjectSensitiveParserImportDelegate delegate = new ProjectSensitiveParserImportDelegate();
    delegate.setProject(project);

    ICodeGenerator codeGen = CodeGeneratorFactory
        .getCodeGenerator(_compilationUnit.getResource()
        .getFileExtension());
    ASTSupport astSupport = new ASTSupport();

    CommonTree modelAST = _compilationUnit.getModelDescriptor();

    /*
     * now let's edit the file itself..
     */
    for (CommonExtensionWizardPage extPage : _commonPages)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("for extension"));

      // get the descriptor and the class name, then the ASTParticipant for that
      for (CommonExtensionDescriptor desc : extPage.getSelectedDescriptors())
      {
        String moduleOrExtension = desc.getClassName();
        CommonTree injectedNode = null;
        long insertionPoint = 0;
        /*
         * use the codegen to create the correct bit..
         */
        if (desc instanceof ModuleDescriptor)
        {
          injectedNode = astSupport.createModuleTree(moduleOrExtension);

          for (CommonTree ct : ASTSupport.getTrees(modelAST,
              JACTRBuilder.MODULE))
          {
            DetailedCommonTree dct = (DetailedCommonTree) ct;
            long end = dct.getStopOffset();
            if (end > insertionPoint) insertionPoint = end + 1;
          }
        }
        else if (desc instanceof ModelExtensionDescriptor)
        {
          boolean hasExtsBlock = ASTSupport.getFirstDescendantWithType(
              modelAST, JACTRBuilder.EXTENSIONS) != null;
          Collection<CommonTree> definedExtensions = ASTSupport.getTrees(
              modelAST, JACTRBuilder.EXTENSION);
          /*
           * 
           */
          injectedNode = astSupport.createExtensionTree(moduleOrExtension);
          if (!hasExtsBlock)
          {
            CommonTree exts = astSupport.create(JACTRBuilder.EXTENSIONS);
            exts.addChild(injectedNode);
            injectedNode = exts;
          }

          if (!hasExtsBlock)
          {
            // our insertion point is after the modules block
            DetailedCommonTree dct = (DetailedCommonTree) ASTSupport
                .getFirstDescendantWithType(modelAST, JACTRBuilder.MODULES);

            insertionPoint = dct.getStopOffset() + 1;
          }
          else if(definedExtensions.size()>0)
          for (CommonTree ct : definedExtensions)
          {
            DetailedCommonTree dct = (DetailedCommonTree) ct;
            long end = dct.getStopOffset();
            if (end > insertionPoint) insertionPoint = end + 1;
          }
          else
          {
            /*
             * we have the extensions block, but no extensions..
             * it could be tough to squeeze this in correctly..
             */
          }
        }

        /*
         * if there is a participant that will contribute default parameters,
         * let's grab it.
         */
        IASTParticipant participant = ASTParticipantRegistry
            .getParticipant(moduleOrExtension);
        if (participant != null)
        {
          IASTInjector injector = participant.getInjector(delegate);
          if (injector != null)
            if (injector instanceof BasicASTInjector)
              ((BasicASTInjector) injector).injectParameters(injectedNode);
        }

        Collection<StringBuilder> code = codeGen.generate(injectedNode, false);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Want to insert %s at %d", code,
              insertionPoint));

      }
    }


    return false;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    Object obj = selection.getFirstElement();

    if (obj instanceof IResource)
    {
      IResource resource = (IResource) obj;
      _compilationUnit = CompilationUnitManager.acquire(resource);
    }
  }

}
