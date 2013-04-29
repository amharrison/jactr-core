package org.jactr.eclipse.core.compiler;

/*
 * default logging
 */
import java.net.URI;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.jactr.eclipse.core.comp.ICompilationUnit;

public class CompilationUnitSchedulingRule implements ISchedulingRule
{
  
  final private URI _sourceToParse;

  public CompilationUnitSchedulingRule(ICompilationUnit unit)
  {
    _sourceToParse = unit.getSource();
  }

  public URI getSource()
  {
    return _sourceToParse;
  }

  public boolean contains(ISchedulingRule rule)
  {
    if (rule instanceof CompilationUnitSchedulingRule)
      return _sourceToParse.equals(((CompilationUnitSchedulingRule) rule).getSource());
    
    return false;
  }

  public boolean isConflicting(ISchedulingRule rule)
  {
    if (rule instanceof CompilationUnitSchedulingRule)
      return _sourceToParse.equals(((CompilationUnitSchedulingRule) rule).getSource());
    
    return false;
  }

}
