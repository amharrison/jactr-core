/*
 * Created on Jun 12, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.reconciler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CharStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.jactr.io.parser.CanceledException;

/**
 * Based on ANTLRStringStream
 * 
 * @author developer
 */
public class ANTLRDocumentStream implements CharStream
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER          = LogFactory
                                                         .getLog(ANTLRDocumentStream.class);

  protected IDocument                _document;

  /** 0..n-1 index into string of next char */
  protected int                      _currentIndex   = 0;

  protected int                      _currentLine    = 1;

  protected int                      _positionInLine = 0;

  protected int                      _markerDepth    = 0;

  protected List<StreamState>        _markers;

  protected int                      _lastMarker;
  
  protected IProgressMonitor         _monitor;

  private String                     _name;
  
  
  public ANTLRDocumentStream(IDocument document, String name)
  {
    this(document, name, new NullProgressMonitor());
  }

  public ANTLRDocumentStream(IDocument document, String name,
      IProgressMonitor monitor)
  {
    _name = name;
    _document = document;
    setProgressMonitor(monitor);
    reset();
  }
  
  public void setProgressMonitor(IProgressMonitor monitor)
  {
    _monitor = monitor;
  }

  /**
   * Reset the stream so that it's in the same state it was when the object was
   * created *except* the data array is not touched.
   */
  public void reset()
  {
    _currentIndex = 0;
    _currentLine = 1;
    _positionInLine = 0;
    _markerDepth = 0;
    _markers = null;
  }

  public void consume()
  {
    if (_monitor.isCanceled()) throw new CanceledException();
    
    if (_currentIndex < size())
    {
      _positionInLine++;
      char c = 0;

      try
      {
        c = _document.getChar(_currentIndex);
      }
      catch (BadLocationException e)
      {
        throw new IllegalStateException(
            "Could not get next character in document next:" + _currentIndex
                +
                " size:" + size(), e);
      }

      if (c == '\n')
      {
        _currentLine++;
        _positionInLine = 0;
      }
      _currentIndex++;
    }
  }

  public int LA(int i)
  {
    if (_monitor.isCanceled()) throw new CanceledException();
    
    if (i == 0) return 0; // undefined
    if (i < 0)
    {
      i++; // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
      if (_currentIndex + i - 1 < 0) return CharStream.EOF; // invalid; no char before first char
    }

    if (_currentIndex + i - 1 >= size()) return CharStream.EOF;
    // System.out.println("char LA("+i+")="+(char)data[p+i-1]+"; p="+p);
    // System.out.println("LA("+i+"); p="+p+" n="+n+"
    // data.length="+data.length);
    try
    {
      return _document.getChar(_currentIndex + i - 1);
    }
    catch (BadLocationException e)
    {
      throw new IllegalStateException("Could not get character index:"
          +
          (_currentIndex + i - 1) + " size:" + size(), e);
    }
  }

  public int LT(int i)
  {
    return LA(i);
  }

  /**
   * Return the current input symbol index 0..n where n indicates the last
   * symbol has been read. The index is the index of char to be returned from
   * LA(1).
   */
  public int index()
  {
    return _currentIndex;
  }

  public int size()
  {
    return _document.getLength();
  }

  public int mark()
  {
    if (_markers == null)
    {
      _markers = new ArrayList<StreamState>();
      _markers.add(null); // depth 0 means no backtracking, leave blank
    }
    _markerDepth++;
    StreamState state = null;
    if (_markerDepth >= _markers.size())
    {
      state = new StreamState();
      _markers.add(state);
    }
    else
      state = _markers.get(_markerDepth);
    state._p = _currentIndex;
    state._line = _currentLine;
    state._charPositionInLine = _positionInLine;
    _lastMarker = _markerDepth;
    return _markerDepth;
  }

  public void rewind(int m)
  {
    StreamState state = _markers.get(m);
    // restore stream state
    seek(state._p);
    _currentLine = state._line;
    _positionInLine = state._charPositionInLine;
    release(m);
  }

  public void rewind()
  {
    rewind(_lastMarker);
  }

  public void release(int marker)
  {
    // unwind any other markers made after m and release m
    _markerDepth = marker;
    // release this marker
    _markerDepth--;
  }

  /**
   * consume() ahead until p==index; can't just set p=index as we must update
   * line and charPositionInLine.
   */
  public void seek(int index)
  {
    if (index <= _currentIndex)
    {
      _currentIndex = index; // just jump; don't update stream state (line,
      // ...)
      return;
    }
    // seek forward, consume until p hits index
    while (_currentIndex < index)
      consume();
  }

  public String substring(int start, int stop)
  {
    try
    {
      String rtn = _document.get(start, stop - start + 1);
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Returning " + rtn);
      return rtn;
    }
    catch (BadLocationException e)
    {
      throw new IllegalStateException("Could not get substring(" + start + ","
          +
          stop + "):" + size(), e);
    }
  }

  public int getLine()
  {
    return _currentLine;
  }

  public int getCharPositionInLine()
  {
    return _positionInLine;
  }

  public void setLine(int line)
  {
    this._currentLine = line;
  }

  public void setCharPositionInLine(int pos)
  {
    this._positionInLine = pos;
  }

  private class StreamState
  {

    /** Index into the char stream of next lookahead char */
    int _p;

    /** What line number is the scanner at before processing buffer[p]? */
    int _line;

    /** What char position 0..n-1 in line is scanner before processing buffer[p]? */
    int _charPositionInLine;
  }

  public String getSourceName()
  {
    return _name;
  }
  
  
}
