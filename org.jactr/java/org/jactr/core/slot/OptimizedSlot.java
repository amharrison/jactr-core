package org.jactr.core.slot;

import java.util.Objects;

public record OptimizedSlot(String name, Object value) implements ISlot
{
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public boolean isVariable()
  {
    return isVariableValue();
  }

  @Override
  public boolean isVariableValue()
  {
    return (value instanceof String str) && (str.charAt(0) == '=')
        && (str.indexOf(' ') == -1);
  }

  @Override
  public Object getValue()
  {
    return value;
  }

  @Override
  public boolean equalValues(Object value2)
  {
    return Objects.equals(value, value2);
  }

  public OptimizedSlot clone()
  {
    return new OptimizedSlot(name, value);
  }
}
