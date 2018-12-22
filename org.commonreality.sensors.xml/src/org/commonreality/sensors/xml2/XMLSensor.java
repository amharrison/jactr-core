package org.commonreality.sensors.xml2;

/*
 * default logging
 */
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.sensors.AbstractSensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLSensor extends AbstractSensor
{
  /**
   * Logger definition
   */
  static private final transient Log                          LOGGER            = LogFactory
                                                                                    .getLog(XMLSensor.class);

  static public final String                                  DATA_URL          = "XMLSensor.DataURI";

  private CompletableFuture<Double>                           _currentTimeRequest;

  private List<Element>                                       _processingFrames = new ArrayList<>();

  private Element                                             _lastElementAdded;

  /*
   * all processing will occur on this thread.
   */
  private ExecutorService                                     _executor;

  private ThreadLocal<Map<IIdentifier, Collection<IMessage>>> _dataMap          = new ThreadLocal<Map<IIdentifier, Collection<IMessage>>>();

  private XMLProcessor                                        _processor;

  private volatile double                                     _nextTimeGuess    = 0;

  @Override
  public String getName()
  {
    return "XMLSensor";
  }

  @Override
  public void start() throws Exception
  {
    super.start();


    requestTimeUpdate(0);
  }

  @Override
  public void shutdown() throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Shuttingdown");
    try
    {
      if (stateMatches(State.STARTED)) stop();
    }
    finally
    {
      if (_executor != null && _executor != getPeriodicExecutor())
        _executor.shutdown();
      _executor = null;
      super.shutdown();
    }
  }

  @Override
  public void configure(Map<String, String> options) throws Exception
  {
    _nextTimeGuess = 0;
    _processor = new XMLProcessor(this);
    _executor = getPeriodicExecutor();

    if (options.containsKey(DATA_URL)) load(options.get(DATA_URL));
  }

  public void addFrame(Element frame)
  {
    Element last = null;
    synchronized (_processingFrames)
    {
      last = _lastElementAdded;
    }

    /*
     * lock frame time. relative times are resolve based on the last added frame
     * otherwise, relatives can just keep getting pushed off.
     */
    if (frame.hasAttribute("relative"))
    {
      double offset = Double.valueOf(frame.getAttribute("relative"));
      double relativeTo = 0;
      if (getClock() != null) // totally possible during config call.
        relativeTo = getClock().getTime(); // by default
      if (last != null) relativeTo = _processor.getTime(last);

      frame.removeAttribute("relative");
      frame.setAttribute("value", Double.toString(offset + relativeTo));
    }

    synchronized (_processingFrames)
    {
      _processingFrames.add(frame);
      _lastElementAdded = frame;
    }
  }

  /**
   * load the xml file into the sensor
   * 
   * @param resource
   */
  public void load(URL resource)
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      Document doc = parser.parse(resource.openStream());

      Element root = doc.getDocumentElement();
      NodeList nl = root.getElementsByTagName("time");
      for (int i = 0; i < nl.getLength(); i++)
      {
        Node node = nl.item(i);
        if (node instanceof Element) addFrame((Element) node);
      }
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER
            .warn(String
                .format(
                    "Could not open XMLSensor resource %s (%s) Does your project have org.commonreality.sensors.xml as an Eclipse-Buddy?",
                    resource, e));
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Caused by", e);
    }
  }

  protected void load(String dataURI)
  {
    dataURI = dataURI.trim();
    if (dataURI.length() == 0) return;

    URL url = getClass().getClassLoader().getResource(dataURI);
    try
    {
      if (url == null) url = new URI(dataURI).toURL();

      load(url);
    }
    catch (URISyntaxException e)
    {
      LOGGER.warn("Invalid uri " + dataURI + " ", e);
    }
    catch (MalformedURLException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("XMLSensor.load threw MalformedURLException : ", e);
    }
  }

  /**
   * returns what we should signal our time update for
   * 
   * @return
   */
  protected double getEstimatedTargetTime(double current)
  {
    return _nextTimeGuess;
  }

  /**
   * force the processing of the queue, regardless of where we are in the
   * time/processes cycle. That is, if a time update has been requested, but not
   * reached, flush can force the processing before the time update.
   */
  public void flush()
  {
    CompletableFuture.runAsync(() -> {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Forced flush "));
      processExpiredFrames(getClock().getTime());
    }, _executor);
  }

  /**
   * queue up these elements and flush. The elements' time features will be
   * rewritten to force them to be immediate. Since each frame represents a time
   * chunk, that time's information will be ignored and made immedate
   * 
   * @param elementsToFlush
   */
  public void flush(Collection<Element> elementsToFlush)
  {
    /*
     * lets the do rewrite here in this thread, with the queueing and processing
     * on the executor.
     */
    for (Element timeFrame : elementsToFlush)
    {
      timeFrame.removeAttribute("value");
      timeFrame.removeAttribute("relative");
      timeFrame.setAttribute("immediate", "true");
      addFrame(timeFrame);
    }

    flush();
  }

  protected boolean isTimeRequestPending()
  {
    return _currentTimeRequest != null && !_currentTimeRequest.isDone();
  }

  /**
   * called when we want to make a time update, but without blocking
   * 
   * @param targetTime
   */
  protected void requestTimeUpdate(double targetTime)
  {
    if (isTimeRequestPending())
    {
      if (LOGGER.isWarnEnabled())
        LOGGER
            .warn("Current request hasn't completed. This call is invalid. Ignoring");
      return;
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Requesting time update %.4f", targetTime));
    /*
     * we make the time update request, but don't block.
     */
    if (Double.isNaN(targetTime))
      _currentTimeRequest = getClock().getAuthority().get()
          .requestAndWaitForChange(null);
    else
      _currentTimeRequest = getClock().getAuthority().get()
          .requestAndWaitForTime(targetTime, null);

    /*
     * when the time has been reached, this will be added to the executors to-do
     * list, keeping us on a single thread. regardless of where the request
     * originated
     */
    _currentTimeRequest.thenAcceptAsync(
        (time) -> {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Time reached : %.4f", time));
          timeReached(time);
        }, _executor);
  }

  /**
   * called on the central executor, this processes the expired frames and
   * queues up a new time update.
   * 
   * @param currentTime
   */
  protected void timeReached(double currentTime)
  {
    processExpiredFrames(currentTime);

    if (!stateMatches(State.STARTED))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("State (%s) not started, returning",
            getState()));
      return;
    }

    if (isTimeRequestPending())
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format(
            "Skipped time update request? isPending:%s state:%s",
            isTimeRequestPending(), getState()));
      return;
    }

    // we're still running, right? and no other request is pending?

    double requested = getEstimatedTargetTime(currentTime);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Will request : %.4f", requested));

    requestTimeUpdate(requested);
  }

  /**
   * called on central exec by {@link #timeReached(double)}
   * 
   * @param currentTime
   */
  protected void processExpiredFrames(double currentTime)
  {
    List<Element> expired = new ArrayList<>();
    List<IIdentifier> agentIds = new ArrayList<>();

    double nextFrameTime = getExpiredFrames(currentTime, expired);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "Processing %d frames @ %.4f, nextFrameTime = %.4f", expired.size(),
          currentTime, nextFrameTime));

    if (expired.size() != 0)
    {
      getInterfacedAgents(agentIds);

      Map<IIdentifier, Collection<IMessage>> data = _dataMap.get();
      if (data == null)
      {
        data = new HashMap<IIdentifier, Collection<IMessage>>();
        _dataMap.set(data);
      }

      for (Element frame : expired)
      {
        /*
         * the two following loops could be collapsed, but since processFrame
         * send objectData and we are sending the comits, this gives clients a
         * changce to process
         */
        for (IIdentifier agentId : agentIds)
          data.put(agentId, _processor.processFrame(agentId, frame, true));

        for (Map.Entry<IIdentifier, Collection<IMessage>> entry : data
            .entrySet())
          _processor.sendData(entry.getKey(), entry.getValue()); // will recycle

        data.clear();
      }
    }

    // if (nextFrameTime > _nextTimeGuess)
    // {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("newFrameTime %.4f  priorGuess %.4f",
          nextFrameTime, _nextTimeGuess));

    _nextTimeGuess = nextFrameTime;
    // }

  }

  /**
   * remove the expired frames for processing, returning the next lowest frame
   * time remaining.
   * 
   * @param currentTime
   * @param container
   * @return
   */
  protected double getExpiredFrames(double currentTime,
      Collection<Element> container)
  {
    double lowestLeft = Double.POSITIVE_INFINITY;
    int visited = 0;

    synchronized (_processingFrames)
    {
      Iterator<Element> itr = _processingFrames.iterator();
      while (itr.hasNext())
      {
        visited++;
        Element frame = itr.next();
        double frameTime = _processor.getTime(frame);

        if (Double.isNaN(frameTime) || frameTime <= currentTime)
        {
          container.add(frame);
          itr.remove();
        }
        else if (frameTime < lowestLeft) lowestLeft = frameTime;
      }
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%d expired, %d remain, guess: %.4f",
          container.size(), visited - container.size(), lowestLeft));

    return Double.isFinite(lowestLeft) ? lowestLeft : Double.NaN;
  }

}
