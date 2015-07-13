package org.jactr.eclipse.runtime.ui.log2;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.jactr.core.logging.Logger;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.log2.ILogSessionDataStream;
import org.jactr.eclipse.runtime.log2.LogData;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.runtime.ui.log2.live.ColumnListener;
import org.jactr.eclipse.runtime.ui.log2.live.LiveLogDataContentProvider;
import org.jactr.eclipse.runtime.ui.misc.AbstractRuntimeModelViewPart;
import org.jactr.eclipse.runtime.ui.selection.SessionTimeSelection;
import org.jactr.eclipse.runtime.ui.selection.SessionTimeSelectionProvider;

public class ModelLogView2 extends AbstractRuntimeModelViewPart
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(ModelLogView2.class);

  static public final String         ID               = ModelLogView2.class
                                                          .getName();

  static private String[]            _expectedStreams = {
      Logger.Stream.TIME.name(), "MARKERS", Logger.Stream.OUTPUT.name(),
      Logger.Stream.GOAL.name(), Logger.Stream.IMAGINAL.name(),
      Logger.Stream.RETRIEVAL.name(), Logger.Stream.PROCEDURAL.name(),
      Logger.Stream.DECLARATIVE.name(), Logger.Stream.VISUAL.name(),
      Logger.Stream.AURAL.name(), Logger.Stream.MOTOR.name() };

  private Color                      EXCEPTION_COLOR  = new Color(
                                                          Display.getCurrent(),
                                                          new RGB(128, 0, 0));

  private StyledText                 _reader;

  @Override
  public void dispose()
  {
    super.dispose();
    EXCEPTION_COLOR.dispose();
  }

  @Override
  public void init(IViewSite viewSite) throws PartInitException
  {
    super.init(viewSite);
    viewSite.setSelectionProvider(new SessionTimeSelectionProvider());
  }

  @Override
  public void createPartControl(Composite parent)
  {
    /*
     * instead of create the models view folder as the primary, we create a sash
     * and stick it in there..
     */
    SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.VERTICAL);
    sashForm.setLayout(new FillLayout());

    createModelViewsFolder(sashForm);

    /*
     * and the other component is the reader
     */
    createReader(sashForm);

    partControlCreated();
  }

  protected boolean hasLogData(ISessionData sessionData)
  {
    ILogSessionDataStream lsds = (ILogSessionDataStream) sessionData
        .getDataStream("log");
    return lsds != null;
  }

  @Override
  protected Composite createModelComposite(String modelName, Object modelData,
      Composite parent)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Creating log ui for %s", modelName));

    ISessionData sessionData = (ISessionData) modelData;

    ILogSessionDataStream lsds = (ILogSessionDataStream) sessionData
        .getDataStream("log");

    /*
     * just because a session is opened, and is even expecting log data, there
     * might not be any log data just yet.
     */
    if (lsds == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("No log data available for %s jsut yet",
            modelName));
      if (sessionData.isOpen())
      {
        if (!wasDeferred(modelData))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Deferring add of %s", modelName));
          deferAdd(modelName, modelData, 500);
        }
        else
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "%s Was previous deferred, assuming no data is coming.",
                modelName));
          removeDeferred(modelData);
        }

      }
      else
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Session data is closed, ignoring %s",
              modelName));

        removeDeferred(modelData);
      }

      return null;
    }

    /*
     * create the sash
     */

    TableViewer viewer = createTableViewer(parent, lsds);
    viewer.setData("sessionData", sessionData);

    configureTable(viewer.getTable());

    /*
     * this needs to be done last
     */
    viewer.setInput(lsds);

    return viewer.getTable();
  }

  protected void createReader(Composite parent)
  {
    _reader = new StyledText(parent, SWT.HORIZONTAL | SWT.VERTICAL);

    Font font = new Font(parent.getDisplay(),
        PreferenceConverter.getFontDataArray(RuntimePlugin.getDefault()
            .getPreferenceStore(), "log.font"));
    _reader.setFont(font);
    _reader.setEditable(false);

    /*
     * clean up the font
     */
    _reader.addDisposeListener(new DisposeListener() {

      public void widgetDisposed(DisposeEvent e)
      {
        _reader.getFont().dispose();
      }

    });

  }

  protected TableViewer createTableViewer(Composite parent,
      ISessionDataStream<LogData> logStream)
  {
    /*
     * if the data is coming in live, we need a rolling content provider
     */
    int properties = SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
        | SWT.BORDER;

    final TableViewer viewer = new TableViewer(parent, properties);
    viewer.setLabelProvider(new TableLabelProvider(viewer.getTable()));

    // if (logStream instanceof ILiveSessionDataStream)
    LiveLogDataContentProvider contentProvider = new LiveLogDataContentProvider(
        viewer);
    viewer.setContentProvider(contentProvider);
    contentProvider.addColumnListener(new ColumnListener() {
      @Override
      public void added(final TableColumn column)
      {
        boolean updateActionBars = createActionForColumnOrAddColumnToExistingAction(
            viewer.getTable(), column, column.getText());
        if (updateActionBars) getViewSite().getActionBars().updateActionBars();
      }
    });

    viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @SuppressWarnings("unchecked")
      public void selectionChanged(SelectionChangedEvent event)
      {
        ISelection selection = event.getSelection();
        boolean clearContent = true;
        if (selection != null)
        {
          LogData row = (LogData) ((StructuredSelection) selection)
              .getFirstElement();

          if (row != null)
          {
            clearContent = false;
            Collection<StyleRange> ranges = new ArrayList<StyleRange>();
            StringBuilder sb = new StringBuilder();
            for (String stream : row.getStreamNames())
            {
              String content = row.get(stream);

              StyleRange streamStyle = new StyleRange();
              streamStyle.start = sb.length();
              sb.append(stream);
              streamStyle.length = stream.length();
              streamStyle.fontStyle = SWT.BOLD | SWT.ITALIC;

              sb.append("\t:\t");
              content = content.replace("\n", "\n\t\t\t\t");
              if (stream.equalsIgnoreCase("exception"))
              {
                // need another style
                StyleRange oops = new StyleRange();
                oops.start = sb.length();
                oops.length = content.length();
                oops.fontStyle = SWT.BOLD;
                oops.foreground = EXCEPTION_COLOR;
                ranges.add(oops);
              }

              sb.append(content);

              sb.append("\n");

              ranges.add(streamStyle);
            }

            _reader.setText(sb.toString());
            for (StyleRange style : ranges)
              _reader.setStyleRange(style);

            /*
             * propogate time selection event.
             */
            TableViewer viewer = (TableViewer) event.getSelectionProvider();
            ISessionData sessionData = (ISessionData) viewer
                .getData("sessionData");
            String model = sessionData.getModelName();

            getViewSite().getSelectionProvider().setSelection(
                new SessionTimeSelection(sessionData.getSession(), model, row
                    .getTime()));
          }

        }

        if (clearContent)
        {
          _reader.setText("");
          StyleRange streamStyle = new StyleRange();
          streamStyle.start = 0;
          streamStyle.length = 0;
          streamStyle.fontStyle = SWT.NORMAL;
          _reader.setStyleRange(streamStyle);

          if (selection.isEmpty())
            getViewSite().getSelectionProvider().setSelection(
                SessionTimeSelection.EMPTY);
        }
      }

    });

    return viewer;
  }

  protected void configureTable(Table table)
  {
    table.setLinesVisible(true);
    table.setHeaderVisible(true);


    /*
     * populate the column headers & pack
     */
    boolean updateActionBars = false;
    for (String stream : _expectedStreams)
    {
      TableColumn column = new TableColumn(table, SWT.LEFT);
      column.setText(stream);
      column.setMoveable(true);
      // column.setResizable(!stream.equals("TIME"));
      // if (stream.equals("TIME")) column.setWidth(TIME_COLUMN_WIDTH);
      updateActionBars |= createActionForColumnOrAddColumnToExistingAction(
          table, column, stream);
    }
    if (updateActionBars) getViewSite().getActionBars().updateActionBars();

    table.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent ce)
      {
        adjustColumnSizes((Table) ce.widget);
      }
    });

    table.pack();
  }

  /**
   * @return True, if the action bars need to be updated. False, if not.
   */
  protected boolean createActionForColumnOrAddColumnToExistingAction(
      Table table, TableColumn column, String columnText)
  {
    boolean updateActionBars = false;
    IToggleColumnAction action = getActionFor(columnText);
    if (action == null)
    {
      getViewSite().getActionBars().getMenuManager()
          .add(new ToggleColumnAction(this, table, column));
      updateActionBars = true;
    }
    else
      action.add(column, table);
    return updateActionBars;
  }

  protected void adjustColumnSizes(Table table)
  {
    int resizableColumns = 0;
    int timeWidth = 0;
    for (TableColumn column : table.getColumns())
      if (column.getText().equals("TIME"))
      {
        timeWidth = column.getWidth();
        if (timeWidth <= 15)
        {
          timeWidth = 0; // effectively.
          resizableColumns++;
        }
      }
      else
      if (column.getResizable()) resizableColumns++;
    /*
     * resize
     */
    // int size = (table.getClientArea().width - TIME_COLUMN_WIDTH)
    // / resizableColumns;
    int size = (table.getClientArea().width - timeWidth)
        / resizableColumns;

    table.setRedraw(false);
    for (TableColumn column : table.getColumns())
      if (column.getResizable()
          && (!column.getText().equals("TIME") || timeWidth == 0))
        column.setWidth(size);

    table.setRedraw(true);
  }

  @Override
  protected void disposeModelComposite(String modelName, Object modelData,
      Composite content)
  {
    content.dispose();
  }

  @Override
  protected void newSessionData(ISessionData sessionData)
  {
    deferAdd(sessionData.getModelName(), sessionData, 250);
  }

  private IToggleColumnAction getActionFor(String columnName)
  {
    Optional<IContributionItem> firstMatchingItem = Arrays
        .stream(getViewSite().getActionBars().getMenuManager().getItems())
        .filter(
            item -> {
              IAction action = null;
              boolean hasAction = item instanceof ActionContributionItem
                  && (action = ((ActionContributionItem) item).getAction()) instanceof IToggleColumnAction
                  && ((IToggleColumnAction) action).getColumnText().equals(
                      columnName);
              return hasAction;
            }).findFirst();
    if (firstMatchingItem.isPresent())
      return (IToggleColumnAction) ((ActionContributionItem) firstMatchingItem
          .get()).getAction();
    else
      return null;
  }
}
