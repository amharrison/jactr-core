package org.jactr.eclipse.association.ui.model;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Association
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(Association.class);

  final private CommonTree           _iChunk;

  final private CommonTree           _jChunk;

  final private int                  _count;

  final private double               _strength;

  public Association(CommonTree jChunk, CommonTree iChunk, int count,
      double strength)
  {
    _iChunk = iChunk;
    _jChunk = jChunk;
    _count = count;
    _strength = strength;
  }

  public CommonTree getJChunk()
  {
    return _jChunk;
  }

  public CommonTree getIChunk()
  {
    return _iChunk;
  }

  public int getCount()
  {
    return _count;
  }

  public double getStrength()
  {
    return _strength;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_iChunk == null ? 0 : _iChunk.hashCode());
    result = prime * result + (_jChunk == null ? 0 : _jChunk.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Association other = (Association) obj;
    if (_iChunk == null)
    {
      if (other._iChunk != null) return false;
    }
    else if (!_iChunk.equals(other._iChunk)) return false;
    if (_jChunk == null)
    {
      if (other._jChunk != null) return false;
    }
    else if (!_jChunk.equals(other._jChunk)) return false;
    return true;
  }

}
