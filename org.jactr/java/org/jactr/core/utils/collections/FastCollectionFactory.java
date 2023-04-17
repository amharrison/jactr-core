package org.jactr.core.utils.collections;

import java.util.Collection;

import org.eclipse.collections.impl.factory.Lists;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class FastCollectionFactory
{
//  static private RecyclableFactory<Collection<?>> _factory    = new PooledRecycableFactory<Collection<?>>(
//      new CollectionPooledObjectFactory<>(Lists.mutable::empty));

  static private RecyclableFactory<Collection<?>> _oldFactory = new ThreadLocalFactory<Collection<?>>(
      Lists.mutable::empty, (obj) -> {
        obj.clear();
      }, null);

  @SuppressWarnings("unchecked")
  static public <T> Collection<T> newInstance()
  {
    return (Collection<T>) _oldFactory.newInstance();
  }

  static public <T> void recycle(Collection<T> set)
  {

    _oldFactory.recycle(set);
  }
}
