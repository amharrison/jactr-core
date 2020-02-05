package org.jactr.core.utils.collections;

/*
 * default logging
 */
import java.util.Collection;

import org.apache.commons.collections.collection.CompositeCollection;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class CompositeCollectionFactory
{

//  static private RecyclableFactory<CompositeCollection> _factory = new PooledRecycableFactory<CompositeCollection>(
//      new CollectionPooledObjectFactory<CompositeCollection>(
//          CompositeCollection::new,
//          CompositeCollectionFactory::clear));

  static private RecyclableFactory<CompositeCollection> _oldFactory = new ThreadLocalFactory<CompositeCollection>(
      CompositeCollectionFactory::newInternal,
      CompositeCollectionFactory::clear, null);

  static public CompositeCollection newInstance()
  {
    return _oldFactory.newInstance();
  }

  static public void recycle(CompositeCollection set)
  {
    _oldFactory.recycle(set);
  }

  @SuppressWarnings("rawtypes")
  static private void clear(CompositeCollection set)
  {
    for (Object composite : set.getCollections())
      set.removeComposited((Collection) composite);
  }

  static private CompositeCollection newInternal()
  {
    return new CompositeCollection();
  }
}
