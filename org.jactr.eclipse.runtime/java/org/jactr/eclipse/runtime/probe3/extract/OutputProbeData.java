package org.jactr.eclipse.runtime.probe3.extract;

/*
 * default logging
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.jactr.eclipse.runtime.probe3.IProbeData;

/**
 * probe data that doesn't actually save the data in memory, but to file.
 * 
 * @author harrison
 */
public class OutputProbeData implements IProbeData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER         = LogFactory
                                                        .getLog(OutputProbeData.class);

  private final String               _probeName;

  private final IResource            _outputFile;

  private PrintWriter                _outputWriter;

  private double                     _lastTimeStamp = Double.MIN_VALUE;

  public OutputProbeData(String probeName, IFolder outputDir)
  {
    _probeName = probeName;
    _outputFile = outputDir.getFile(probeName + ".txt");
    openFile();
  }

  protected void openFile()
  {
    File fp = _outputFile.getRawLocation().toFile();
    try
    {
      fp.getParentFile().mkdirs();
      _outputWriter = new PrintWriter(new FileWriter(fp));
      _outputWriter.println(_probeName);
      _outputWriter.println("Time\tValue");
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("OutputProbeData.openFile threw IOException : ", e);
      _outputWriter = null;
    }
  }

  public void close()
  {
    if (_outputWriter != null)
    {
      _outputWriter.flush();
      _outputWriter.close();
      _outputWriter = null;
    }
  }

  @Override
  public String getName()
  {
    return _probeName;
  }

  @Override
  public void addSample(double time, double value)
  {
    if (_lastTimeStamp >= time)
      LOGGER.error(String.format(
          "Time regression in probe data (%s)? last: %.3f  now: %.3f ",
          getName(), _lastTimeStamp, time), new RuntimeException());

    if (_outputWriter != null /* && _lastTimeStamp < time */)
      _outputWriter.println(String.format("%.3f\t%.3f", time, value));

    _lastTimeStamp = time;
  }

  @Override
  public void setBufferSize(int size)
  {
    // noop

  }

  @Override
  public void setClipWindow(int window)
  {
    // noop

  }

}
