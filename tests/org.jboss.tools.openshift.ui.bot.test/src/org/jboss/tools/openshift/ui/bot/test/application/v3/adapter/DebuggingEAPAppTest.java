package org.jboss.tools.openshift.ui.bot.test.application.v3.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.reddeer.common.condition.AbstractWaitCondition;
import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.exception.CoreLayerException;
import org.jboss.reddeer.eclipse.condition.ServerHasPublishState;
import org.jboss.reddeer.eclipse.condition.ServerHasState;
import org.jboss.reddeer.eclipse.core.resources.Project;
import org.jboss.reddeer.eclipse.core.resources.ProjectItem;
import org.jboss.reddeer.eclipse.debug.core.BreakpointsView;
import org.jboss.reddeer.eclipse.debug.core.DebugView;
import org.jboss.reddeer.eclipse.debug.core.VariablesView;
import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.eclipse.ui.browser.BrowserEditor;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.perspectives.DebugPerspective;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerPublishState;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerState;
import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.junit.screenshot.CaptureScreenshotException;
import org.jboss.reddeer.junit.screenshot.ScreenshotCapturer;
import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.jboss.reddeer.swt.api.Tree;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.impl.browser.InternalBrowser;
import org.jboss.reddeer.swt.impl.button.OkButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.openshift.reddeer.exception.OpenShiftToolsException;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftCommandLineToolsRequirement.OCBinary;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement.RequiredBasicConnection;
import org.jboss.tools.openshift.reddeer.view.OpenShiftExplorerView;
import org.jboss.tools.openshift.reddeer.view.resources.ServerAdapter;
import org.jboss.tools.openshift.reddeer.view.resources.ServerAdapter.Version;
import org.jboss.tools.openshift.ui.bot.test.application.v3.create.AbstractCreateApplicationTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@OpenPerspective(DebugPerspective.class)
@RunWith(RedDeerSuite.class)
@OCBinary
@RequiredBasicConnection
public class DebuggingEAPAppTest extends AbstractCreateApplicationTest {

	@InjectRequirement
	private OpenShiftConnectionRequirement requiredConnection;
	private static ServerAdapter serverAdapter;

	@BeforeClass
	public static void setupClass() {

		toggleAutoBuild(false);

		createServerAdapter();

		disableShowConsoleWhenOutputChanges();

		serverAdapter = new ServerAdapter(Version.OPENSHIFT3, "eap-app", "Service");
		serverAdapter.select();
		new ContextMenu("Restart in Debug").select();
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);

		waitForserverAdapterToBeInRightState();

		cleanAndBuildWorkspace();
	}

	private static void cleanAndBuildWorkspace() {
		new ShellMenu("Project", "Clean...").select();
		new DefaultShell("Clean");
		new OkButton().click();
		new WaitWhile(new JobIsRunning());
	}

	@AfterClass
	public static void tearDownClass() {
		toggleAutoBuild(true);
	}

	private static void toggleAutoBuild(boolean autoBuild) {
		ShellMenu autoBuildMenuItem = new ShellMenu("Project", "Build Automatically");
		boolean isSelected = autoBuildMenuItem.isSelected();
		if (autoBuild && !isSelected) {
			autoBuildMenuItem.select();
		}
		if (!autoBuild && isSelected) {
			autoBuildMenuItem.select();
		}
	}

	private static void waitForserverAdapterToBeInRightState() {
		new WaitUntil(new ServerHasState(new ServersView().getServer(serverAdapter.getLabel()), ServerState.DEBUGGING));
		new WaitUntil(new ServerHasPublishState(new ServersView().getServer(serverAdapter.getLabel()),
				ServerPublishState.SYNCHRONIZED));
	}

	@Before
	public void setup() {
		setupBreakpoint();
	}

	@Test
	public void debuggerStopsAtBreakpointTest() {

		cleanAndBuildWorkspace();

		triggerDebugSession();

		// now it shoud be stopped in debug mode.

		checkDebugView();

		checkVariablesView();
	}

	@Test
	public void changeVariableValueTest() throws CaptureScreenshotException {

		cleanAndBuildWorkspace();

		triggerDebugSession();
		try {
			setNewVariableValue("NewWorld", "name");
		} catch (WaitTimeoutExpiredException ex) {
			ex.printStackTrace();

		}
		checkNewVariableValueIsPropagatedToBrowser();
	}

	@After
	public void teardown() {
		try {
			new ShellMenu("Run", "Terminate").select();
		} catch (CoreLayerException ex) {
			if (ex.getMessage().contains("Menu item is not enabled")) {
				// no big deal, there is no execution running
			} else {
				throw ex;
			}
		}

		// remove all breakpoints
		BreakpointsView breakpointsView = new BreakpointsView();
		breakpointsView.open();
		breakpointsView.removeAllBreakpoints();

	}

	private void setupBreakpoint() {
		// set breakpoint where we need it.
		ProjectItem helloServiceFile = getHelloServiceFile();
		setBreakpointToLineWithText(helloServiceFile, "return \"Hello");
	}

	private void checkNewVariableValueIsPropagatedToBrowser() throws CaptureScreenshotException {

		new WaitWhile(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				ShellMenu resumeMenu = new ShellMenu("Run", "Resume");
				if (resumeMenu.isEnabled()) {
					resumeMenu.select();
					return true;
				} else {
					return false;
				}
			}
		});
		try {
			BrowserEditor browserEditor = new BrowserEditor("helloworld");
			browserEditor.activate();
			String text = browserEditor.getText();
			assertTrue(text.contains("NewWorld"));
		} catch (CoreLayerException e) {
			e.printStackTrace();
		}

	}

	private void checkDebugView() {
		DebugView debugView = new DebugView();
		debugView.open();
		TreeItem createHelloMessageDebugItem = null;
		magic(debugView.getSelectedItem());
		try {
			ScreenshotCapturer.getInstance().captureScreenshot("pokus");
		} catch (CaptureScreenshotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (debugView.getSelectedText().contains("createHelloMessage")) {
			createHelloMessageDebugItem = debugView.getSelectedItem();
		} else if (debugView.getSelectedText().contains("Suspended")) {
			// the thread is not expanded -> expand it.
			debugView.getSelectedItem().expand();
			List<TreeItem> items = debugView.getSelectedItem().getItems();
			try {
				createHelloMessageDebugItem = items.get(0);
			} catch (IndexOutOfBoundsException ex) {
				throw ex;
			}
		} else {
			throw new OpenShiftToolsException("Unable to locate correct thread in DebugView.");
		}
		createHelloMessageDebugItem.select();
		assertTrue(createHelloMessageDebugItem.getText().contains("createHelloMessage"));
	}

	private void magic(TreeItem selectedItem) {
		// get top item
		Tree parent = selectedItem.getParent();
		TreeItem remoteDebuggerTreeItem = parent.getItems().stream().filter(containsStringPredicate("Remote debugger"))
				.findFirst().get();

		List<TreeItem> items = remoteDebuggerTreeItem.getItems();
		TreeItem openJDKTreeItem = items.get(0);
		assertTrue(openJDKTreeItem.getText().contains("OpenJDK"));
		// wait until we can see the suspended thread
		try {
			new WaitUntil(new AbstractWaitCondition() {

				@Override
				public boolean test() {
					Optional<TreeItem> suspendedTreeItemOptional = openJDKTreeItem.getItems().stream()
							.filter(containsStringPredicate("Suspended")).findFirst();
					return suspendedTreeItemOptional.isPresent();
				}
			});
		} catch (WaitTimeoutExpiredException e) {
			throw e;
		}
		TreeItem suspendedThreadTreeItem = openJDKTreeItem.getItems().stream()
				.filter(containsStringPredicate("Suspended")).findFirst().get();
		suspendedThreadTreeItem.select();

	}

	private Predicate<TreeItem> containsStringPredicate(String string) {
		return treeItem -> treeItem.getText().contains(string);
	}

	private void checkVariablesView() {
		VariablesView variablesView = new VariablesView();
		variablesView.open();
		String nameValue = variablesView.getValue("name");
		String thisValue = variablesView.getValue("this");
		try {
			ScreenshotCapturer.getInstance().captureScreenshot("this_should_contain_HelloService");
		} catch (CaptureScreenshotException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertEquals("World", nameValue);
		try {
			assertTrue(variablesView.getValue("this").contains("HelloService"));
		} catch (AssertionError e) {
			throw e;
		}
	}

	private void triggerDebugSession() {
		serverAdapter.select();
		new ContextMenu("Show In", "Web Browser").select();
		try {
			new WaitUntil(new AbstractWaitCondition() {

				@Override
				public boolean test() {
					BrowserEditor browserEditor = null;
					try {
						browserEditor = new BrowserEditor(new BaseMatcher<String>() {

							@Override
							public boolean matches(Object arg0) {
								return true;
							}

							@Override
							public void describeTo(Description arg0) {
								// TODO Auto-generated method stub

							}
						});
					} catch (CoreLayerException ex) {
						System.out.println("Core layer exception");
						return false;
					}
					String text = browserEditor.getText();
					if (text.contains("Unable to load page") || text.contains("404")) {
						System.out.println("Refreshing page");
						try {
							ScreenshotCapturer.getInstance().captureScreenshot("Browser_should_be_ready");
						} catch (CaptureScreenshotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						new ServersView().open();
						serverAdapter.select();
						new ContextMenu("Show In", "Web Browser").select();
						return false;
					} else {
						System.out.println("Browser je ready.");
						try {
							ScreenshotCapturer.getInstance().captureScreenshot("Browser_should_be_ready");
						} catch (CaptureScreenshotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}
				}
			});
		} catch (WaitTimeoutExpiredException e) {
			throw e;
		}

	}

	private ProjectItem getHelloServiceFile() {
		ProjectExplorer projectExplorer = new ProjectExplorer();
		projectExplorer.open();
		Project project = projectExplorer.getProject(PROJECT_NAME);
		ProjectItem helloServiceFile = project.getProjectItem("Java Resources", "src/main/java",
				"org.jboss.as.quickstarts.helloworld", "HelloService.java");
		return helloServiceFile;
	}

	// Sets breakpoint to first appearance of given text.
	private void setBreakpointToLineWithText(ProjectItem file, String text) {
		file.open();
		TextEditor textEditor = new TextEditor("HelloService.java");
		textEditor.setCursorPosition(textEditor.getPositionOfText(text));
		new ShellMenu("Run", "Toggle Breakpoint").select();
	}

	private static void createServerAdapter() {
		OpenShiftExplorerView explorer = new OpenShiftExplorerView();
		explorer.getOpenShift3Connection().getProject().getService("eap-app").createServerAdapter();
	}

	private static void disableShowConsoleWhenOutputChanges() {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();

		new WaitUntil(new ShowConsoleOutputToolItemIsAvailable());

		DefaultToolItem showConsoleOnChange = new DefaultToolItem(new WorkbenchShell(),
				"Show Console Output When Standard Out Changes");
		showConsoleOnChange.click();
	}

	// TODO this should be replaced once
	// https://github.com/jboss-reddeer/reddeer/issues/1668 is fixed.
	private void setNewVariableValue(String newValue, final String... variablePath) {
		new WaitWhile(new JobIsRunning());

		VariablesView variablesView = new VariablesView();
		variablesView.open();

		new WaitUntil(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				try {
					TreeItem variable = new DefaultTreeItem(variablePath);
					variable.select();
					return variable.isSelected();
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public String description() {
				return "Variable is not selected";
			}
		});

		new ContextMenu("Change Value...").select();
		new DefaultShell("Change Object Value");
		new DefaultStyledText().setText(newValue);
		new OkButton().click();

		new WaitWhile(new JobIsRunning());
	}

	private static class ShowConsoleOutputToolItemIsAvailable extends AbstractWaitCondition {
		@Override
		public boolean test() {
			try {
				new DefaultToolItem(new WorkbenchShell(), "Show Console Output When Standard Out Changes");
				return true;
			} catch (CoreLayerException ex) {
				return false;
			}
		}
	}

	// private class ResumeButtonIsEnabled extends AbstractWaitCondition{
	// @Override
	// public boolean test() {
	// serverAdapter.select();
	// new ContextMenu("Show In", "Web Browser").select();
	// //is debugSession active?
	// if (new ShellMenu("Run", "Resume").isEnabled()){
	// return true;
	// }else{
	// //close editor and try again
	// new DefaultEditor().close();
	// return false;
	// }
	// }
	// }
}
