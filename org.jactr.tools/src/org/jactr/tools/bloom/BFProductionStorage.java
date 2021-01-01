package org.jactr.tools.bloom;

import org.jactr.core.module.procedural.storage.DefaultProductionStorage;
import org.jactr.core.production.IProduction;
import org.jactr.tools.bloom.funnels.ProductionFunnel;

import com.google.common.hash.BloomFilter;

/**
 * provides a bloom filter front end onto the default production storage. This
 * can radically accelerate the addition of new productions by by-passing the
 * expensive {@link #checkForExistingProduction(IProduction)} call.
 * 
 * @author harrison
 */
public class BFProductionStorage extends DefaultProductionStorage
{

  BloomFilter<IProduction> _bloomFilter;

  boolean                  _needsReindexing = false;

  public BFProductionStorage()
  {
    _bloomFilter = BloomFilter.create(ProductionFunnel.INSTANCE, 1_000_000);
  }

  public boolean needsReindexing()
  {
    return _needsReindexing;
  }

  public void reindex(long maxCapacity)
  {
    _bloomFilter = BloomFilter.create(ProductionFunnel.INSTANCE, maxCapacity);
  }

  @Override
  protected void index(IProduction production)
  {
    _bloomFilter.put(production);
    super.index(production);
  }

  @Override
  protected void unindex(IProduction production)
  {
    _needsReindexing = true;
    super.unindex(production);
  }

  @Override
  protected IProduction checkForExistingProduction(IProduction production)
  {
    if (_bloomFilter.mightContain(production))
      return super.checkForExistingProduction(production);
    else
      return null;
  }
}
