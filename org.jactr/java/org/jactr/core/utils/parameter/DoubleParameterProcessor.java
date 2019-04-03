package org.jactr.core.utils.parameter;

/*
 * default logging
 */
import java.util.function.Consumer;
import java.util.function.Supplier;

 
import org.slf4j.LoggerFactory;

public class DoubleParameterProcessor extends
 ParameterProcessor<Double>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DoubleParameterProcessor.class);

  public DoubleParameterProcessor(String parameterName,
      Consumer<Double> setFunction, Supplier<Double> getFunction)
  {
    super(parameterName, Double::parseDouble, setFunction, Number::toString,
        getFunction);
  }

}
