package org.jactr.core.utils.collections;

import java.util.List;

import org.eclipse.collections.impl.factory.Lists;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class FastListFactory
{

//  static private RecyclableFactory<List<?>> _factory = new PooledRecycableFactory<List<?>>(
//      new CollectionPooledObjectFactory<>(Lists.mutable::empty));
//
  static private RecyclableFactory<List<?>> _oldFactory = new ThreadLocalFactory<List<?>>(
      Lists.mutable::empty, (obj) -> {
        obj.clear();
      }, null);

  @SuppressWarnings("unchecked")
  static public <T> List<T> newInstance()
  {
    return (List<T>) _oldFactory.newInstance();
  }

  static public <T> void recycle(List<T> set)
  {
    _oldFactory.recycle(set);
  }
}
