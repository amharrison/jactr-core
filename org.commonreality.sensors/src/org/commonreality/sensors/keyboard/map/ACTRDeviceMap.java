package org.commonreality.sensors.keyboard.map;

/*
 * default logging
 */
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Set;

import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ACTRDeviceMap extends AbstractDeviceMap
{
  /**
   * 
   */
  private static final long          serialVersionUID = 9156702245099838424L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(ACTRDeviceMap.class);

  public ACTRDeviceMap()
  {
    super();
    initialize();
  }

  protected void initialize()
  {
    addKey(KeyEvent.VK_ESCAPE, 0, 0);
    addKey(KeyEvent.VK_F1, 2, 0);
    addKey(KeyEvent.VK_F2, 3, 0);
    addKey(KeyEvent.VK_F3, 4, 0);
    addKey(KeyEvent.VK_F4, 5, 0);
    addKey(KeyEvent.VK_F5, 7, 0);
    addKey(KeyEvent.VK_F6, 8, 0);
    addKey(KeyEvent.VK_F7, 9, 0);
    addKey(KeyEvent.VK_F8, 10, 0);
    addKey(KeyEvent.VK_F9, 12, 0);
    addKey(KeyEvent.VK_F10, 13, 0);
    addKey(KeyEvent.VK_F11, 14, 0);
    addKey(KeyEvent.VK_F12, 15, 0);
    addKey(KeyEvent.VK_F13, 17, 0);
    addKey(KeyEvent.VK_F14, 18, 0);
    addKey(KeyEvent.VK_F15, 19, 0);

    addKey(KeyEvent.VK_BACK_QUOTE, 0, 2);
    addKey(KeyEvent.VK_1, 1, 2);
    addKey(KeyEvent.VK_2, 2, 2);
    addKey(KeyEvent.VK_3, 3, 2);
    addKey(KeyEvent.VK_4, 4, 2);
    addKey(KeyEvent.VK_5, 5, 2);
    addKey(KeyEvent.VK_6, 6, 2);
    addKey(KeyEvent.VK_7, 7, 2);
    addKey(KeyEvent.VK_8, 8, 2);
    addKey(KeyEvent.VK_9, 9, 2);
    addKey(KeyEvent.VK_0, 10, 2);
    addKey(KeyEvent.VK_MINUS, 11, 2);
    addKey(KeyEvent.VK_EQUALS, 12, 2);
    addKey(KeyEvent.VK_BACK_SPACE, 13, 2);
    addKey(KeyEvent.VK_HELP, 15, 2);
    addKey(KeyEvent.VK_HOME, 16, 2);
    addKey(KeyEvent.VK_PAGE_UP, 17, 2);
    addKey(KeyEvent.VK_CLEAR, 19, 2);
    addKey(KeyEvent.VK_EQUALS, 20, 2);
    addKey(KeyEvent.VK_DIVIDE, 21, 2);
    addKey(KeyEvent.VK_MULTIPLY, 22, 2);

    addKey(KeyEvent.VK_TAB, 0, 3);
    addKey(KeyEvent.VK_Q, 1, 3);
    addKey(KeyEvent.VK_W, 2, 3);
    addKey(KeyEvent.VK_E, 3, 3);
    addKey(KeyEvent.VK_R, 4, 3);
    addKey(KeyEvent.VK_T, 5, 3);
    addKey(KeyEvent.VK_Y, 6, 3);
    addKey(KeyEvent.VK_U, 7, 3);
    addKey(KeyEvent.VK_I, 8, 3);
    addKey(KeyEvent.VK_O, 9, 3);
    addKey(KeyEvent.VK_P, 10, 3);
    addKey(KeyEvent.VK_OPEN_BRACKET, 11, 3);
    addKey(KeyEvent.VK_CLOSE_BRACKET, 12, 3);
    addKey(KeyEvent.VK_BACK_SLASH, 13, 3);
    addKey(KeyEvent.VK_DELETE, 15, 3);
    addKey(KeyEvent.VK_END, 16, 3);
    addKey(KeyEvent.VK_PAGE_DOWN, 17, 3);
    addKey(KeyEvent.VK_NUMPAD7, 19, 3);
    addKey(KeyEvent.VK_NUMPAD8, 20, 3);
    addKey(KeyEvent.VK_NUMPAD9, 21, 3);
    addKey(KeyEvent.VK_MINUS, 22, 3);

    addKey(KeyEvent.VK_CAPS_LOCK, 0, 4);
    addKey(KeyEvent.VK_A, 1, 4);
    addKey(KeyEvent.VK_S, 2, 4);
    addKey(KeyEvent.VK_D, 3, 4);
    addKey(KeyEvent.VK_F, 4, 4);
    addKey(KeyEvent.VK_G, 5, 4);
    addKey(KeyEvent.VK_H, 6, 4);
    addKey(KeyEvent.VK_J, 7, 4);
    addKey(KeyEvent.VK_K, 8, 4);
    addKey(KeyEvent.VK_L, 9, 4);
    addKey(KeyEvent.VK_SEMICOLON, 10, 4);
    addKey(KeyEvent.VK_QUOTE, 11, 4);
    addKey(KeyEvent.VK_ENTER, 12, 4);
    addKey(KeyEvent.VK_ENTER, 13, 4);
    addKey(KeyEvent.VK_NUMPAD4, 19, 4);
    addKey(KeyEvent.VK_NUMPAD5, 20, 4);
    addKey(KeyEvent.VK_NUMPAD7, 21, 4);
    addKey(KeyEvent.VK_PLUS, 22, 4);

    addKey(KeyEvent.VK_SHIFT, 0, 5);
    addKey(KeyEvent.VK_Z, 1, 5);
    addKey(KeyEvent.VK_X, 2, 5);
    addKey(KeyEvent.VK_C, 3, 5);
    addKey(KeyEvent.VK_V, 4, 5);
    addKey(KeyEvent.VK_B, 5, 5);
    addKey(KeyEvent.VK_N, 6, 5);
    addKey(KeyEvent.VK_M, 7, 5);
    addKey(KeyEvent.VK_COMMA, 8, 5);
    addKey(KeyEvent.VK_PERIOD, 9, 5);
    addKey(KeyEvent.VK_SLASH, 10, 5);
    addKey(KeyEvent.VK_SHIFT, 11, 5);
    addKey(KeyEvent.VK_SHIFT, 12, 5);
    addKey(KeyEvent.VK_SHIFT, 13, 5);
    addKey(KeyEvent.VK_UP, 16, 5);
    addKey(KeyEvent.VK_NUMPAD1, 19, 5);
    addKey(KeyEvent.VK_NUMPAD2, 20, 5);
    addKey(KeyEvent.VK_NUMPAD3, 21, 5);
    addKey(KeyEvent.VK_ENTER, 22, 5);

    addKey(KeyEvent.VK_CONTROL, 0, 6);
    addKey(KeyEvent.VK_ALT, 1, 6); // OR OPTION
    addKey(KeyEvent.VK_META, 2, 6);
    addKey(KeyEvent.VK_SPACE, 3, 6);
    addKey(KeyEvent.VK_SPACE, 4, 6);
    addKey(KeyEvent.VK_SPACE, 5, 6);
    addKey(KeyEvent.VK_SPACE, 6, 6);
    addKey(KeyEvent.VK_SPACE, 7, 6);
    addKey(KeyEvent.VK_SPACE, 8, 6);
    addKey(KeyEvent.VK_SPACE, 9, 6);
    addKey(KeyEvent.VK_SPACE, 10, 6);
    addKey(KeyEvent.VK_META, 11, 6);
    addKey(KeyEvent.VK_ALT, 12, 6);
    addKey(KeyEvent.VK_CONTROL, 13, 6);
    addKey(KeyEvent.VK_LEFT, 15, 6);
    addKey(KeyEvent.VK_DOWN, 16, 6);
    addKey(KeyEvent.VK_RIGHT, 17, 6);
    addKey(KeyEvent.VK_NUMPAD0, 19, 6);
    addKey(KeyEvent.VK_NUMPAD0, 20, 6);
    addKey(KeyEvent.VK_PERIOD, 21, 6);
    addKey(KeyEvent.VK_ENTER, 22, 6);

    addKey(MouseEvent.BUTTON1, 28, 2);
    addKey(MouseEvent.BUTTON2, 29, 2);
    addKey(MouseEvent.BUTTON3, 30, 2);

  }

  public int getKeyCode(String string)
  {
    KeyStroke stroke =KeyStroke.getKeyStroke(string); 
    if(stroke!=null)
      return stroke.getKeyCode();
    
    return -1;
  }
}
