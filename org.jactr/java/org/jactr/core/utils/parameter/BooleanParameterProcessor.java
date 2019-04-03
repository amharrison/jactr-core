package org.jactr.core.utils.parameter;

/*
 * default logging
 */
import java.util.function.Consumer;
import java.util.function.Supplier;

 
import org.slf4j.LoggerFactory;

public class BooleanParameterProcessor extends ParameterProcessor<Boolean>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(BooleanParameterProcessor.class);

  public BooleanParameterProcessor(String parameterName,
      Consumer<Boolean> setFunction, Supplier<Boolean> getFunction)
  {
    super(parameterName, Boolean::parseBoolean, setFunction, b -> b.toString(),
        getFunction);
  }

}
