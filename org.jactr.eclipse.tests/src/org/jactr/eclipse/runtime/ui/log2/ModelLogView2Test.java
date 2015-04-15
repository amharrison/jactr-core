package org.jactr.eclipse.runtime.ui.log2;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javolution.util.FastList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.log2.ILogSessionDataStream;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.ISessionController;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.test.DisplayHelper;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.ActionSequence;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Note: Run as JUnit Plug-in Test
 */
public class ModelLogView2Test {
	
	@Rule
	public DisplayHelper displayHelper = new DisplayHelper();

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();
	
	@Test
	public void testCreateTableViewerAddsASingleToggleColumnActionForIdenticallyNamedColumns()
		throws Exception {
		
		final List<IContributionItem> items = new ArrayList<>();
		items.add(new ActionContributionItem(new MockToggleColumnAction("TIME")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("MARKERS")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("OUTPUT")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("GOAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("IMAGINAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("RETRIEVAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("PROCEDURAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("DECLARATIVE")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("VISUAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("AURAL")));
		items.add(new ActionContributionItem(new MockToggleColumnAction("MOTOR")));
		
		final IContributionItem[] items0  = new IContributionItem[0],
								  items1  = items.subList(0, 1).toArray(items0),
								  items2  = items.subList(0, 2).toArray(items0),
								  items3  = items.subList(0, 3).toArray(items0),
								  items4  = items.subList(0, 4).toArray(items0),
								  items5  = items.subList(0, 5).toArray(items0),
								  items6  = items.subList(0, 6).toArray(items0),
								  items7  = items.subList(0, 7).toArray(items0),
								  items8  = items.subList(0, 8).toArray(items0),
							      items9  = items.subList(0, 9).toArray(items0),
							      items10 = items.subList(0, 10).toArray(items0),
							      items11 = items.subList(0, 11).toArray(items0);
		
		final BundleContext bundleContext = context.mock(BundleContext.class);
		final Bundle bundle = context.mock(Bundle.class);
		final IViewSite viewSite = context.mock(IViewSite.class);
		final IActionBars actionBars = context.mock(IActionBars.class);
		final IMenuManager menuManager = context.mock(IMenuManager.class);
		final IToolBarManager toolBarManager = context.mock(IToolBarManager.class);
		final ISession model1Session = context.mock(ISession.class, "model1Session");
		final ISessionData model1Data = context.mock(ISessionData.class, "model1Data");
		final ISessionController model1Controller = context.mock(ISessionController.class, "model1Controller");
		final ILogSessionDataStream model1Stream = context.mock(ILogSessionDataStream.class, "model1Stream");
		final ISession model2Session = context.mock(ISession.class, "model2Session");
		final ISessionData model2Data = context.mock(ISessionData.class, "model2Data");
		final ISessionController model2Controller = context.mock(ISessionController.class, "model2Controller");
		final ILogSessionDataStream model2Stream = context.mock(ILogSessionDataStream.class, "model2Stream");
		
		@SuppressWarnings("unchecked")
		Expectations expectations = new Expectations() {{
			allowing(bundleContext).getBundle();
				will(returnValue(bundle));
			allowing(bundleContext).registerService(
					with(aNonNull(String.class)),
					with(aNonNull(Object.class)),
					with(aNonNull(Dictionary.class)));
				
			allowing(bundle).getSymbolicName();
				will(returnValue("bundleName"));
			
			allowing(viewSite).setSelectionProvider(with(aNonNull(ISelectionProvider.class)));
			allowing(viewSite).getActionBars();
				will(returnValue(actionBars));
				
			allowing(actionBars).getToolBarManager();
				will(returnValue(toolBarManager));
			
			allowing(toolBarManager).add(with(aNonNull(IContributionItem.class)));
			allowing(toolBarManager).add(with(aNonNull(IAction.class)));
			allowing(toolBarManager).update(with(any(Boolean.class)));
				
			allowing(model1Data).getDataStream("log");
				will(returnValue(model1Stream));
			allowing(model1Data).getSession();
				will(returnValue(model1Session));
				
			allowing(model2Data).getDataStream("log");
				will(returnValue(model2Stream));
			allowing(model2Data).getSession();
				will(returnValue(model2Session));
				
			allowing(model1Session).isOpen();
			allowing(model1Session).hasBeenDestroyed();
				will(returnValue(true));
			allowing(model1Session).getController();
				will(returnValue(model1Controller));
				
			allowing(model2Session).isOpen();
			allowing(model2Session).hasBeenDestroyed();
				will(returnValue(true));
			allowing(model2Session).getController();
				will(returnValue(model2Controller));
				
			allowing(model1Controller).isTerminated();
				will(returnValue(true));
			allowing(model1Controller).canTerminate();
				will(returnValue(false));
			allowing(model1Controller).canSuspend();
				will(returnValue(false));
			allowing(model1Controller).canResume();
				will(returnValue(false));
				
			allowing(model2Controller).isTerminated();
				will(returnValue(true));
			allowing(model2Controller).canTerminate();
				will(returnValue(false));
			allowing(model2Controller).canSuspend();
				will(returnValue(false));
			allowing(model2Controller).canResume();
				will(returnValue(false));
				
			allowing(model1Stream).getStartTime();
				will(returnValue(0.0d));
			allowing(model1Stream).getEndTime();
				will(returnValue(0.0d));
			allowing(model1Stream).getData(0.0d, 0.0d, FastList.newInstance());
				
			allowing(model2Stream).getStartTime();
				will(returnValue(0.0d));
			allowing(model2Stream).getEndTime();
				will(returnValue(0.0d));
			allowing(model2Stream).getData(0.0d, 0.0d, FastList.newInstance());
				
			allowing(actionBars).getMenuManager();
				will(returnValue(menuManager));
			exactly(1).of(actionBars).updateActionBars();
			
			exactly(22).of(menuManager).getItems();
				will(new ActionSequence(
						returnValue(items0),
						returnValue(items1),
						returnValue(items2),
						returnValue(items3),
						returnValue(items4),
						returnValue(items5),
						returnValue(items6),
						returnValue(items7),
						returnValue(items8),
						returnValue(items9),
						returnValue(items10),
						
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11),
						returnValue(items11)
						));
			
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("TIME")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("MARKERS")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("OUTPUT")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("GOAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("IMAGINAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("RETRIEVAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("PROCEDURAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("DECLARATIVE")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("VISUAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("AURAL")));
			exactly(1).of(menuManager).add(with(new ToggleColumnActionMatcher("MOTOR")));
				
		}};
		context.checking(expectations);

		new RuntimePlugin().start(bundleContext);

		Shell shell = displayHelper.createShell();
		ModelLogView2 view = new ModelLogView2();
		view.init(viewSite);
		view.createPartControl(shell);
		view.addModelData("model1", model1Data);
		view.addModelData("model2", model2Data);
		
		items.forEach(item -> {
			int numberOfColumns = ((MockToggleColumnAction)
					((ActionContributionItem)item).getAction()).getNumberOfColumns();
			assertThat(numberOfColumns, is(2));
		});
		

		
		//RuntimePlugin.getDefault().stop(bundleContext);	}
	
	private static class ToggleColumnActionMatcher extends FeatureMatcher<IToggleColumnAction,String> {
		
		private ToggleColumnActionMatcher(String columnName) {
			super(new IsEqual<String>(columnName), "the column text", "text");
		}

		@Override
		protected String featureValueOf(IToggleColumnAction action) {
			return action.getColumnText();
		}
		
	}
	
	private static class MockToggleColumnAction extends Action implements IToggleColumnAction {

		private int numberOfColumns = 1;
		
		protected MockToggleColumnAction(String columnName) {
			super(columnName, IAction.AS_CHECK_BOX);
		}
		
		@Override
		public String getColumnText() {
			return getText();
		}

		@Override
		public int getNumberOfColumns() {
			return numberOfColumns;
		}

		@Override
		public void add(TableColumn column, Table table) {
			numberOfColumns++;
		}

		@Override
		public void remove(TableColumn column) {
			numberOfColumns--;
		}
		
	}
}
