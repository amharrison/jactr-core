package org.jactr.io2.ast;

import org.jactr.core.model.IModel;

public interface IASTGenerator
{

  public boolean generates(String format);

  public Object generate(IModel model, String format, boolean trimIfPossible);
}
