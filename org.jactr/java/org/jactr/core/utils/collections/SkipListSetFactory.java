package org.jactr.core.utils.collections;

/*
 * default logging
 */
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jactr.core.utils.recyclable.RecyclableFactory;
import org.jactr.core.utils.recyclable.ThreadLocalFactory;
import org.slf4j.LoggerFactory;

public class SkipListSetFactory
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger                                      LOGGER                 = LoggerFactory
      .getLogger(SkipListSetFactory.class);

  static private final Map<Comparator<?>, RecyclableFactory<ConcurrentSkipListSet<?>>> _factoriesByComparator = new HashMap<Comparator<?>, RecyclableFactory<ConcurrentSkipListSet<?>>>();

  @SuppressWarnings("rawtypes")
  static private RecyclableFactory<ConcurrentSkipListSet<?>> createFactoryForComparator(
      final Comparator<?> comparator)
  {
//    return new AbstractThreadLocalRecyclableFactory<ConcurrentSkipListSet>() {
//
//      @Override
//      protected void cleanUp(ConcurrentSkipListSet obj)
//      {
//        obj.clear();
//      }
//
//      @SuppressWarnings("unchecked")
//      @Override
//      protected ConcurrentSkipListSet instantiate(Object... params)
//      {
//        return new ConcurrentSkipListSet((Comparator) params[0]);
//      }
//
//      @Override
//      protected void release(ConcurrentSkipListSet obj)
//      {
//        // noop
//      }
//    };
//    return new PooledRecycableFactory<ConcurrentSkipListSet<?>>(
//        new CollectionPooledObjectFactory<>(() -> {
//          return new ConcurrentSkipListSet(comparator);
//        }));
//
    return new ThreadLocalFactory<ConcurrentSkipListSet<?>>(() -> {
      return new ConcurrentSkipListSet(comparator);
    }, (obj) -> {
      obj.clear();
    }, null);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static public <T> ConcurrentSkipListSet<T> newInstance(
      Comparator<T> comparator)
  {
    RecyclableFactory<ConcurrentSkipListSet<?>> set = _factoriesByComparator
        .get(comparator);
    if (set == null)
    {
      set = createFactoryForComparator(comparator);
      _factoriesByComparator.put(comparator, set);
    }

    return (ConcurrentSkipListSet<T>) set.newInstance();
  }

  static public <T> void recycle(ConcurrentSkipListSet<T> set)
  {
    if (set == null) return;
    RecyclableFactory<ConcurrentSkipListSet<?>> factory = _factoriesByComparator
        .get(set.comparator());

    if (factory != null)
      factory.recycle(set);
    else
      set.clear(); // at least we clean up, even if we aren't ultimately
                   // responsible
  }
}
