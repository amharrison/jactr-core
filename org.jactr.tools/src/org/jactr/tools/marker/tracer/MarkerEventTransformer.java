package org.jactr.tools.marker.tracer;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.event.IACTREvent;
import org.jactr.tools.marker.impl.MarkerEvent;
import org.jactr.tools.tracer.transformer.IEventTransformer;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class MarkerEventTransformer implements IEventTransformer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(MarkerEventTransformer.class);

  public ITransformedEvent transform(IACTREvent actrEvent)
  {
    return new MarkerTransformedEvent((MarkerEvent) actrEvent);
  }

}
