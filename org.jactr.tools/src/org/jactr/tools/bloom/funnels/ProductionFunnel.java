package org.jactr.tools.bloom.funnels;

import org.jactr.core.production.IProduction;
import org.jactr.core.production.ISymbolicProduction;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public enum ProductionFunnel implements Funnel<IProduction> {
  INSTANCE;

  @Override
  public void funnel(IProduction from, PrimitiveSink into)
  {
    /*
     * we only check the conditions and actions. names and parameters are
     * ignored.
     */
    ISymbolicProduction sProd = from.getSymbolicProduction();
    sProd.getConditions().forEach(c -> {
      ConditionFunnel.INSTANCE.funnel(c, into);
    });
    sProd.getActions().forEach(a -> {
      ActionFunnel.INSTANCE.funnel(a, into);
    });
  }

}
