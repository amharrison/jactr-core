package org.jactr.core.utils.collections;

/*
 * default logging
 */
import java.util.Collection;

import org.apache.commons.collections.set.CompositeSet;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class CompositeSetFactory
{

//  static private RecyclableFactory<CompositeSet>       _factory = new PooledRecycableFactory<CompositeSet>(
//      new CollectionPooledObjectFactory<CompositeSet>(CompositeSet::new,
//          CompositeSetFactory::clear));

  static private RecyclableFactory<CompositeSet> _oldFactory = new ThreadLocalFactory<CompositeSet>(
      CompositeSetFactory::newInternal, CompositeSetFactory::clear, null);

  static public CompositeSet newInstance()
  {
    return _oldFactory.newInstance();
  }

  static public void recycle(CompositeSet set)
  {
    _oldFactory.recycle(set);
  }

  @SuppressWarnings("rawtypes")
  static private void clear(CompositeSet set)
  {
    for (Object composite : set.getCollections())
      set.removeComposited((Collection) composite);
  }

  static private CompositeSet newInternal()
  {
    return new CompositeSet();
  }
}
