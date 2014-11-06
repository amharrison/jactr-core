package org.jactr.eclipse.runtime.probe3;

/*
 * default logging
 */

public interface IProbeData
{

  public String getName();

  public void addSample(double time, double value);

  public void setBufferSize(int size);

  public void setClipWindow(int window);
}
