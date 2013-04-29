/*
 * Created on Mar 23, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.wizards.templates;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.ui.templates.TemplateOption;

/**
 * basic template that contributes a jACT-R Module
 */
public class InstrumentContributorTemplateSection extends
    BaseACTRContributorTemplateSection
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER                     = LogFactory
                                                                    .getLog(InstrumentContributorTemplateSection.class);

  static final public String         KEY_INSTRUMENT_NAME        = "instrumentName";

  static final public String         KEY_INSTRUMENT_CLASS       = "instrumentClass";

  static final public String         DEFAULT_INSTRUMENT_PACKAGE = "edu.yourUniversity.jactr.instruments";

  static final public String         DEFAULT_INSTRUMENT_CLASS   = "YourInstrument";

  static private final String        EXTENSION_POINT            = "org.jactr.instruments";

  public InstrumentContributorTemplateSection()
  {
    setPageCount(1);
    TemplateOption option = addOption(KEY_INSTRUMENT_NAME, "Instrument name",
        "MyInstrument", 0);
    option.setRequired(false);

    option = addOption(KEY_PACKAGE_NAME, "Instrument package",
        DEFAULT_INSTRUMENT_PACKAGE, 0);
    option.setRequired(true);

    option = addOption(KEY_INSTRUMENT_CLASS, "Instrument class name",
        DEFAULT_INSTRUMENT_CLASS, 0);
    option.setRequired(true);
  }

  @Override
  public void addPages(Wizard wizard)
  {
    WizardPage page = createPage(0, null);
    page.setTitle("Instrument contribution");
    page
        .setDescription("Create a new named instrument to record some aspect of a running model");
    wizard.addPage(page);
    markPagesAdded();
  }

  @Override
  protected void updateModel(IProgressMonitor monitor) throws CoreException
  {
    
    IPluginBase plugin = model.getPluginBase();
    IPluginExtension extension = createExtension(EXTENSION_POINT, true); //$NON-NLS-1$
    IPluginModelFactory factory = model.getPluginFactory();

    IPluginElement instrument = factory.createElement(extension);
    instrument.setName("instrument"); //$NON-NLS-1$
    instrument.setAttribute("name", getStringOption(KEY_INSTRUMENT_NAME)); //$NON-NLS-1$ //$NON-NLS-2$
    instrument.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + "."
        + getStringOption(KEY_INSTRUMENT_CLASS));

    IPluginElement parameter = factory.createElement(instrument);
    parameter.setName("parameter");
    parameter.setAttribute("name", "attach");
    parameter.setAttribute("value", "all");
    instrument.add(parameter);

    IPluginElement description = factory.createElement(instrument);
    description.setName("description");
    description.setText("This is " + getStringOption(KEY_INSTRUMENT_NAME));
    instrument.add(description);

    extension.add(instrument);

    if (!extension.isInTheModel()) plugin.add(extension);


    exportPackages(Collections.singleton(getStringOption(KEY_PACKAGE_NAME)));
  }

  @Override
  public String getLabel()
  {
    return "Contributes jACT-R Instrument";
  }

  @Override
  public String[] getNewFiles()
  {
    return new String[0];
  }

  @Override
  public String getSectionId()
  {
    return "instrument";
  }

  public String getUsedExtensionPoint()
  {
    return EXTENSION_POINT;
  }

  @Override
  public void validateOptions(TemplateOption changed)
  {
    boolean error = false;
    for (TemplateOption option : getOptions(0))
      if (option.isRequired() && option.isEnabled() && option.isEmpty())
      {
        flagMissingRequiredOption(option);
        error = true;
      }

    if (!error) resetPageState();
  }

  @Override
  public void initializeFields(IPluginModelBase model)
  {
  }

}
