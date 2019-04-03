package org.jactr.core.utils.parameter;

/*
 * default logging
 */
import java.util.function.Consumer;
import java.util.function.Supplier;

 
import org.slf4j.LoggerFactory;

public class LongParameterProcessor extends
    ParameterProcessor<Long>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(LongParameterProcessor.class);

  public LongParameterProcessor(String parameterName,
      Consumer<Long> setFunction, Supplier<Long> getFunction)
  {
    super(parameterName, Long::parseLong, setFunction, (i) -> {
      return Long.toString(i);
    },
        getFunction);
  }

}
