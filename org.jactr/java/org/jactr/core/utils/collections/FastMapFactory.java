package org.jactr.core.utils.collections;

import java.util.Map;

import org.eclipse.collections.impl.factory.Maps;
import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;

public class FastMapFactory
{
//  static private RecyclableFactory<Map<?, ?>> _factory = new PooledRecycableFactory<Map<?, ?>>(
//      new MapPooledObjectFactory<>(Maps.mutable::empty));

  static private RecyclableFactory<Map<?, ?>> _oldFactory = new ThreadLocalFactory<Map<?, ?>>(
      Maps.mutable::empty, (obj) -> {
        obj.clear();
      }, null);

  @SuppressWarnings("unchecked")
  static public <K, V> Map<K, V> newInstance()
  {
    return (Map<K, V>) _oldFactory.newInstance();
  }

  static public <K, V> void recycle(Map<K, V> set)
  {
    _oldFactory.recycle(set);
  }
}
