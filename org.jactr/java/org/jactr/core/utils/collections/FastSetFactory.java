package org.jactr.core.utils.collections;

import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class FastSetFactory
{
//  static private RecyclableFactory<Set<?>> _factory = new PooledRecycableFactory<Set<?>>(
//      new CollectionPooledObjectFactory<>(Sets.mutable::empty));

  static private RecyclableFactory<Set<?>> _oldFactory = new ThreadLocalFactory<Set<?>>(
      Sets.mutable::empty, (obj) -> {
        obj.clear();
      }, null);

  @SuppressWarnings("unchecked")
  static public <T> Set<T> newInstance()
  {
    return (Set<T>) _oldFactory.newInstance();
  }

  static public <T> void recycle(Set<T> set)
  {
    _oldFactory.recycle(set);
  }
}
