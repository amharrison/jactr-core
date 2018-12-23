package org.jactr.tools.itr;

import java.util.List;

import org.jactr.core.utils.parameter.IParameterized;
import org.jactr.io2.compilation.ICompilationUnit;


/**
 * general interface for modifying parameters
 * @author harrison
 *
 */
public interface IParameterModifier extends IParameterized
{
  
  static public final String PARAMETER_NAME = "ParameterName";
  static public final String PARAMETER_VALUES = "ParameterValues";

  public List<String> getParameterValues();
  
  public String getParameterName();
  
  public String getParameterDisplayName();

  public void setParameter(ICompilationUnit modelDescriptor, int parameterValueIndex);
  
  public void setParameter(ICompilationUnit modelDescriptor, String parameterValue);
}
