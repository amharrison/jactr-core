package org.jactr.eclipse.association.ui.model;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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

  private Map<String, String>        _parameters;

  public Association(CommonTree jChunk, CommonTree iChunk, int count,
      double strength)
  {
    _iChunk = iChunk;
    _jChunk = jChunk;
    _count = count;
    _strength = strength;
  }

  public void setParameter(String name, String value)
  {
    if (_parameters == null) _parameters = new TreeMap<String, String>();
    _parameters.put(name, value);
  }

  public String getParameter(String name)
  {
    if (_parameters == null) return null;
    return _parameters.get(name);
  }

  public Collection<String> getParameterNames()
  {
    if (_parameters == null) return Collections.EMPTY_LIST;
    return new ArrayList<String>(_parameters.keySet());
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
    result = prime * result
        + (_parameters == null ? 0 : _parameters.hashCode());
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
    if (_parameters == null)
    {
      if (other._parameters != null) return false;
    }
    else if (!_parameters.equals(other._parameters)) return false;
    return true;
  }


}
