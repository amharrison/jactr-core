package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.module.procedural.event.ProceduralModuleEvent;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.tools.tracer.transformer.procedural.TransformedProceduralEvent;

public class RecentProductionModelData
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER           = LogFactory
                                                               .getLog(RecentProductionModelData.class);

  private final ISession                  _session;

  private final String                    _modelName;

  private final Map<String, Character>    _productionNameTranslation;

  private final Map<Character, String>    _characterTranslation;

  private final Set<Character>            _available;

  private final Collection<StringBuilder> _patterns;

  private final StringBuilder             _patternWindow;

  private int                             _maxWindowSize   = 10;

  private int                             _maxPatternDepth = 5;

  private int                             _maxTableSize    = 10;

  private ILoopListener                   _loopListener;

  @SuppressWarnings("serial")
  public RecentProductionModelData(ISession session, String modelName,
      ILoopListener loopListener)
  {
    _modelName = modelName;
    _session = session;
    _loopListener = loopListener;
    _patternWindow = new StringBuilder();

    _available = new HashSet<Character>();
    for (char c = 'a'; c <= 'z'; c++)
      _available.add(c);

    _patterns = new ArrayList<StringBuilder>(_maxPatternDepth);
    for (int i = 1; i <= _maxPatternDepth; i++)
      _patterns.add(new StringBuilder(i));

    _characterTranslation = new TreeMap<Character, String>();
    _productionNameTranslation = new LinkedHashMap<String, Character>(
        _maxTableSize, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Character> eldest)
      {
        if (size() > _maxTableSize)
        {
          Character c = eldest.getValue();
          clearOccurrences(c);
          _available.add(eldest.getValue());
          _characterTranslation.remove(c);
          return true;
        }
        else
          return false;
      }
    };
  }

  public void dispose()
  {
    _productionNameTranslation.clear();
    _patterns.clear();
    _available.clear();
  }

  public void process(ISession session, TransformedProceduralEvent event)
  {
    if (event.getType() != ProceduralModuleEvent.Type.PRODUCTION_FIRED) return;

    String productionName = ASTSupport.getName(event.getAST()).toLowerCase();
    Character translated = translate(productionName);

    append(translated);
    search(session);
  }

  private void search(ISession session)
  {
    new LinkedHashMap<List<String>, Integer>();
    List<String> productionSequence = new ArrayList<String>();
    int iterations = 0;
    for (StringBuilder patternTemplate : _patterns)
    {
      String pattern = patternTemplate.toString();
      int patternLength = pattern.length();
      int count = 0;
      int start = _patternWindow.indexOf(pattern);
      while (start >= 0 && start + patternLength < _patternWindow.length())
      {
        int next = _patternWindow.indexOf(pattern, start + patternLength);
        // consecutive
        if (next - start == patternLength) count++;

        start = next;
      }

      // no match found, there cant be any further down
      if (count == 0) break;

      /*
       * build production sequence
       */
      productionSequence.add(translate(pattern.charAt(pattern.length() - 1)));
      iterations = count;
    }

    if (iterations > 0)
      _loopListener.loopDetected(session, _modelName, productionSequence,
          iterations);
  }

  private void append(Character production)
  {
    _patternWindow.append(production);
    while (_patternWindow.length() > _maxWindowSize)
      _patternWindow.delete(0, _patternWindow.length() - _maxWindowSize);

    // and to the patterns
    int length = 1;
    for (StringBuilder pattern : _patterns)
    {
      if (pattern.length() == length) pattern.delete(0, length);
      pattern.append(production);

      length++;
    }
  }

  private void clearOccurrences(Character c)
  {
    for (StringBuilder string : _patterns)
      for (int index = 0; index < string.length(); index++)
        if (string.charAt(index) == c) string.deleteCharAt(index);
  }

  private String translate(Character c)
  {
    return _characterTranslation.get(c);
  }

  private Character translate(String productionName)
  {
    Character c = _productionNameTranslation.get(productionName);
    if (c == null)
    {
      Iterator<Character> itr = _available.iterator();

      c = itr.next();
      itr.remove();

      _productionNameTranslation.put(productionName, c);
      _characterTranslation.put(c, productionName);
    }

    return c;
  }
}
