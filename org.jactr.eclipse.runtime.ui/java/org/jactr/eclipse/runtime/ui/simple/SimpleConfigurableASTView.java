package org.jactr.eclipse.runtime.ui.simple;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.ui.UIPlugin;
import org.jactr.eclipse.runtime.ui.misc.AbstractSessionTimeViewPart;

public abstract class SimpleConfigurableASTView extends
    AbstractSessionTimeViewPart
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SimpleConfigurableASTView.class);

  static private enum Orientation {
    VERTICAL, HORIZONTAL
  };

  private Orientation        _currentOrientation;

  private IOrientedComponent _orientedComponent;

  private Composite          _root;

  private ISession           _currentSession;

  private String             _currentModel;

  private double             _currentTime;

  private boolean            _currentConflict;

  private Action             _showPostConflictResolution;

  public SimpleConfigurableASTView(boolean listensToLiveSessions)
  {
    super(listensToLiveSessions);
  }

  @Override
  protected void setData(ISession session, String modelName, double time,
      boolean isPostConflictResolution)
  {
    _currentConflict = isPostConflictResolution;
    _currentModel = modelName;
    _currentSession = session;
    _currentTime = time;
    _orientedComponent.setData(session, modelName, time,
        isPostConflictResolution);
  }

  @Override
  protected void noData()
  {
    _orientedComponent.noAST();
  }

  @Override
  public void createPartControl(Composite parent)
  {
    _root = new Composite(parent, SWT.NONE);
    _root.setLayout(new FillLayout());

    _root.addControlListener(new ControlListener() {

      public void controlResized(ControlEvent e)
      {
        if (determineOrientation() != _currentOrientation)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Switching orientation!"));
          _currentOrientation = determineOrientation();
          IOrientedComponent old = _orientedComponent;
          if (old != null) old.dispose();

          _orientedComponent = instantiateOrientation(_currentOrientation);

          if (_currentSession == null)
            noData();
          else
            setData(_currentSession, _currentModel, _currentTime,
                _currentConflict);

        }

      }

      public void controlMoved(ControlEvent e)
      {
        // noop

      }
    });

    _currentOrientation = determineOrientation();
    _orientedComponent = instantiateOrientation(_currentOrientation);

    createActions();
    createMenus();
    createToolbar();
  }

  protected void createActions()
  {
    /*
     * create the action that shows pre/post conflict res
     */
    _showPostConflictResolution = new Action("Pre Conflict",
        IAction.AS_CHECK_BOX) {
      @Override
      public void run()
      {
        setSelection(getCurrentSelection());

        if (isChecked())
        {
          setText("Post Conflict");
          setToolTipText("Press to see pre conflict resolution");
          setImageDescriptor(UIPlugin.getDefault().getImageDescriptor(
              "postconflict"));
        }
        else
        {
          setText("Pre Conflict");
          setToolTipText("Press to see post conflict resolution");
          setImageDescriptor(UIPlugin.getDefault().getImageDescriptor(
              "preconflict"));
        }

        getViewSite().getActionBars().getToolBarManager().update(true);
      }
    };

    _showPostConflictResolution.setEnabled(true);

    _showPostConflictResolution.setImageDescriptor(UIPlugin.getDefault()
        .getImageDescriptor("preconflict"));
  }

  protected void createMenus()
  {

  }

  protected void createToolbar()
  {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(new Separator());
    if (isSensitiveToConflictResolution())
      mgr.add(_showPostConflictResolution);
  }

  private Orientation determineOrientation()
  {
    Rectangle bounds = _root.getClientArea();
    if (LOGGER.isDebugEnabled())
      LOGGER
          .debug(String.format("Bounds %d x %d", bounds.width, bounds.height));

    if (bounds.width < bounds.height) return Orientation.VERTICAL;
    return Orientation.HORIZONTAL;
  }

  private IOrientedComponent instantiateOrientation(Orientation orientation)
  {
    IOrientedComponent component = null;
    if (orientation == Orientation.VERTICAL)
      component = instantiateVertical();
    else
      component = instantiateHorizontal();

    component.createPartControl(_root);
    return component;
  }

  abstract protected IOrientedComponent instantiateVertical();

  abstract protected IOrientedComponent instantiateHorizontal();

  @Override
  public void setFocus()
  {
    _orientedComponent.setFocus();
  }

  @Override
  protected boolean showPostConflictResolution()
  {
    return _showPostConflictResolution.isChecked();
  }

  abstract protected boolean isSensitiveToConflictResolution();

}
