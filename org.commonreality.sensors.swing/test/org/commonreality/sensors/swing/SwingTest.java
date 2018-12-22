package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SwingTest
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SwingTest.class);

  JFrame _frame;
  
  public SwingTest()
  {
    
  }
  
  public JFrame getFrame()
  {
    return _frame;
  }
  
  public void buildGUI()
  {
    try
    {
      SwingUtilities.invokeAndWait(new Runnable(){
        public void run()
        {
          assembleGUI();
        }
      });
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("SwingTest.buildGUI threw InterruptedException : ",e);
    }
    catch (InvocationTargetException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("SwingTest.buildGUI threw InvocationTargetException : ",e);
    }
  }
  
  private void assembleGUI()
  {
    _frame = new JFrame("test");
    
    JTextPane text = new JTextPane();
    text.setText("Test");
    
    JScrollPane right = new JScrollPane(text);
    JPanel left = new JPanel();
    left.setLayout(new BorderLayout());
    left.add(new JButton("button"), "Center");
    
    JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, left, right);
    _frame.add(jsp);
    
    _frame.pack();
    _frame.setSize(400, 400);
  }
}
