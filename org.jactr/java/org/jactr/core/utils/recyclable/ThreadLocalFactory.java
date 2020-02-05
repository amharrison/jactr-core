package org.jactr.core.utils.recyclable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadLocalFactory<T>
    extends AbstractThreadLocalRecyclableFactory<T>
{

  private Supplier<T>           _supplier;

  private Function<Object[], T> _function;

  private Consumer<T>           _cleanup;

  private Consumer<T>           _release;

  public ThreadLocalFactory(Function<Object[], T> instantiator,
      Consumer<T> cleanUp, Consumer<T> release)
  {
    _function = instantiator;
    _cleanup = cleanUp;
    _release = release;
  }

  public ThreadLocalFactory(int maxCapacity, Function<Object[], T> instantiator,
      Consumer<T> cleanUp, Consumer<T> release)
  {
    super(maxCapacity);

    _function = instantiator;
    _cleanup = cleanUp;
    _release = release;
  }

  public ThreadLocalFactory(Supplier<T> instantiator, Consumer<T> cleanUp,
      Consumer<T> release)
  {
    _supplier = instantiator;
    _cleanup = cleanUp;
    _release = release;
  }

  public ThreadLocalFactory(int maxCapacity, Supplier<T> instantiator,
      Consumer<T> cleanUp, Consumer<T> release)
  {
    super(maxCapacity);

    _supplier = instantiator;
    _cleanup = cleanUp;
    _release = release;
  }

  @Override
  protected void cleanUp(T obj)
  {
    if (_cleanup != null) _cleanup.accept(obj);

  }

  @Override
  protected void release(T obj)
  {
    if (_release != null) _release.accept(obj);

  }

  @Override
  protected T instantiate(Object... params)
  {
    if (_function != null)
      return _function.apply(params);
    else
      return _supplier.get();
  }

}
