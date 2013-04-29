package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.tools.grapher.core.message.StringTableMessage;

public class StringTable
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(StringTable.class);

  private Map<Long, String>          _table;

  private String[]                   _index;

  public StringTable()
  {
    _table = new HashMap<Long, String>();
    _index = new String[40];
  }

  synchronized public String lookUp(Long id)
  {
    // String str = _table.get(id);
    int index = id.intValue();

    if (index >= _index.length)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format("Index exceeds table size, ignoring"));
      return null;
    }

    String str = _index[index];

    if (str == null && LOGGER.isWarnEnabled())
      LOGGER.warn(String.format("Could not find %d in string table %s", id,
          Arrays.toString(_index)));

    return str;
  }

  synchronized public void update(StringTableMessage message)
  {

    Map<Long, String> table = message.getStringTable();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("ST update : %s", table));

    // _table.putAll(message.getStringTable());

    for (Map.Entry<Long, String> entry : table.entrySet())
    {
      int index = entry.getKey().intValue();

      if (index >= _index.length)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Growing string table"));
        // grow
        String[] newIndex = new String[_index.length * 3 / 2];
        System.arraycopy(_index, 0, newIndex, 0, _index.length);
        _index = newIndex;
      }

      _index[index] = entry.getValue();
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Table : %s", Arrays.toString(_index)));
  }
}
