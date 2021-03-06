/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.internal.expressions.CountExpression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.HandlerProxy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 *
 */
public class CommandEnablementTest extends UITestCase {

	private static final String CONTEXT_TEST2 = "org.eclipse.ui.command.contexts.enablement_test2";
	private static final String CONTEXT_TEST1 = "org.eclipse.ui.command.contexts.enablement_test1";
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";
	private static final String CMD3_ID = PREFIX + "cmd3";

	private ICommandService commandService;
	private IHandlerService handlerService;
	private IContextService contextService;

	private Command cmd1;
	private Command cmd3;
	private DefaultHandler normalHandler1;
	private IHandlerActivation activation1;
	private DefaultHandler normalHandler2;
	private IHandlerActivation activation2;
	private DisabledHandler disabledHandler1;
	private DisabledHandler disabledHandler2;
	private EnableEventHandler eventHandler1;
	private EnableEventHandler eventHandler2;
	private IEvaluationService evalService;
	private CheckContextHandler contextHandler;
	private IContextActivation contextActivation1;
	private IContextActivation contextActivation2;

	/**
	 * @param testName
	 */
	public CommandEnablementTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		commandService = (ICommandService) fWorkbench
				.getService(ICommandService.class);
		handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);
		contextService = (IContextService) fWorkbench
				.getService(IContextService.class);
		evalService = (IEvaluationService) fWorkbench
				.getService(IEvaluationService.class);
		cmd1 = commandService.getCommand(CMD1_ID);
		cmd3 = commandService.getCommand(CMD3_ID);
		normalHandler1 = new DefaultHandler();
		normalHandler2 = new DefaultHandler();
		disabledHandler1 = new DisabledHandler();
		disabledHandler2 = new DisabledHandler();
		eventHandler1 = new EnableEventHandler();
		eventHandler2 = new EnableEventHandler();
		contextHandler = new CheckContextHandler();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		if (activation1 != null) {
			handlerService.deactivateHandler(activation1);
			activation1 = null;
		}
		if (activation2 != null) {
			handlerService.deactivateHandler(activation2);
			activation2 = null;
		}
		if (contextActivation1 != null) {
			contextService.deactivateContext(contextActivation1);
			contextActivation1 = null;
		}
		if (contextActivation2 != null) {
			contextService.deactivateContext(contextActivation2);
			contextActivation2 = null;
		}
		super.doTearDown();
	}

	private static class DefaultHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}
	}

	private static class DisabledHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
		 */
		public boolean isEnabled() {
			return false;
		}
	}

	private static class EnableEventHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		private boolean fEnabled = true;

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
		 */
		public boolean isEnabled() {
			return fEnabled;
		}

		public void setEnabled(boolean enabled) {
			if (fEnabled != enabled) {
				fEnabled = enabled;
				fireHandlerChanged(new HandlerEvent(this, true, false));
			}
		}
	}

	private static class CheckContextHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActivePartChecked(event);
			return null;
		}

		public void setEnabled(Object applicationContext) {
			Object o = HandlerUtil.getVariable(applicationContext,
					ISources.ACTIVE_PART_NAME);
			setBaseEnabled(o instanceof IWorkbenchPart);
		}
	}

	private static class EnablementListener implements ICommandListener {
		public int enabledChanged = 0;

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.ICommandListener#commandChanged(org.eclipse.core.commands.CommandEvent)
		 */
		public void commandChanged(CommandEvent commandEvent) {
			if (commandEvent.isEnabledChanged()) {
				enabledChanged++;
			}
		}
	}

	class UpdatingHandler extends AbstractHandler implements IElementUpdater {
		
		private final String text;

		public UpdatingHandler(String text) {
			this.text = text;
		}
		
		public void updateElement(UIElement element, Map parameters) {
			element.setText(text);
		}

		public Object execute(ExecutionEvent event) {
			return null;
		}

	}
	
	public void testRestoreContributedUI() throws Exception {
		
		Field iconField = CommandContributionItem.class.getDeclaredField("icon");
		iconField.setAccessible(true);

		Field labelField = CommandContributionItem.class.getDeclaredField("label");
		labelField.setAccessible(true);
		
		String menuId = "org.eclipse.ui.tests.Bug275126";
		MenuManager manager = new MenuManager(null, menuId);
		IMenuService menuService = (IMenuService) fWorkbench.getService(IMenuService.class);
		menuService.populateContributionManager(manager, MenuUtil.menuUri(menuId));
		IContributionItem[] items = manager.getItems();
		assertTrue(items.length ==1);
		assertTrue(items[0] instanceof CommandContributionItem);
		CommandContributionItem item = (CommandContributionItem) items[0];

		String text1 = "text1";
		String text2 = "text2";
		
		// contributed from plugin.xml
		String contributedLabel = "Contributed Label";
		
		// default handler 
		assertTrue(cmd3.getHandler() instanceof HandlerProxy);
		assertEquals(contributedLabel, labelField.get(item)); 
		assertNotNull(iconField.get(item));

		UpdatingHandler handler1 = new UpdatingHandler(text1);
		activation1 = handlerService.activateHandler(CMD3_ID, handler1, new ActiveContextExpression(CONTEXT_TEST1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		UpdatingHandler handler2 = new UpdatingHandler(text2);
		activation2 = handlerService.activateHandler(CMD3_ID, handler2, new ActiveContextExpression(CONTEXT_TEST2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertEquals(handler1, cmd3.getHandler());
		assertEquals(text1, labelField.get(item));
		assertNotNull(iconField.get(item));
		
		contextService.deactivateContext(contextActivation1);
		// back to default handler state
		assertTrue(cmd3.getHandler() instanceof HandlerProxy);
		assertEquals(contributedLabel, labelField.get(item)); 
		assertNotNull(iconField.get(item));

		contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
		assertEquals(handler2, cmd3.getHandler());
		assertEquals(text2, labelField.get(item));
		assertNotNull(iconField.get(item));

		// activate both context
		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);

		// both handler activations eval to true, no handler set
		assertNull(cmd3.getHandler());
		assertEquals(contributedLabel, labelField.get(item));
		assertNotNull(iconField.get(item));
		
		contextService.deactivateContext(contextActivation1);
		contextService.deactivateContext(contextActivation2);
				
	}
	
	
	public void testEnablementForNormalHandlers() throws Exception {
		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler1, cmd1.getHandler());
		contextService.deactivateContext(contextActivation1);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler2, cmd1.getHandler());
		contextService.deactivateContext(contextActivation2);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
	}

	public void testEventsForNormalHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler1, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation1);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler2, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation2);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	public void testEventsForDisabledHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, disabledHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, disabledHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler1, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler2, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation2);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	public void testEventsForEnabledHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, eventHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, eventHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;

			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(eventHandler1, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			eventHandler1.setEnabled(true);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertTrue(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler1.setEnabled(true);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertTrue(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler2.setEnabled(false);
			eventHandler2.setEnabled(true);
			eventHandler2.setEnabled(false);
			eventHandler2.setEnabled(true);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			contextService.deactivateContext(contextActivation1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	public void testCommandWithHandlerProxy() throws Exception {
		IConfigurationElement handlerProxyConfig = null;
		String commandId = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.handlers");
		IExtension[] extensions = point.getExtensions();
		boolean found = false;
		for (int i = 0; i < extensions.length && !found; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length && !found; j++) {
				if (configElements[j].getAttribute(
						IWorkbenchRegistryConstants.ATT_CLASS).equals(
						"org.eclipse.ui.tests.menus.HelloEHandler")) {
					handlerProxyConfig = configElements[j];
					commandId = handlerProxyConfig
							.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					found = true;
				}
			}
		}
		assertNotNull(handlerProxyConfig);
		Expression enabledWhen = new ActiveContextExpression(CONTEXT_TEST1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		HandlerProxy proxy = new HandlerProxy(commandId, handlerProxyConfig,
				"class", enabledWhen, evalService);
		assertFalse(proxy.isEnabled());
		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertTrue(proxy.isEnabled());
		contextService.deactivateContext(contextActivation1);
		assertFalse(proxy.isEnabled());
	}

	private static class Checker implements IHandlerListener {
		boolean lastChange = false;
		public void handlerChanged(HandlerEvent handlerEvent) {
			lastChange = handlerEvent.isEnabledChanged();
		}
	}

	public void testEnablementWithHandlerProxy() throws Exception {
		IConfigurationElement handlerProxyConfig = null;
		String commandId = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.handlers");
		IExtension[] extensions = point.getExtensions();
		boolean found = false;
		for (int i = 0; i < extensions.length && !found; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length && !found; j++) {
				if (configElements[j].getAttribute(
						IWorkbenchRegistryConstants.ATT_COMMAND_ID).equals(
						"org.eclipse.ui.tests.enabledCount")) {
					handlerProxyConfig = configElements[j];
					commandId = handlerProxyConfig
							.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					found = true;
				}
			}
		}
		assertNotNull(handlerProxyConfig);
		Expression enabledWhen = ExpressionConverter.getDefault()
				.perform(
						handlerProxyConfig.getChildren("enabledWhen")[0]
								.getChildren()[0]);
		assertTrue(enabledWhen instanceof CountExpression);
		HandlerProxy proxy = new HandlerProxy(commandId, handlerProxyConfig, "class",
				enabledWhen, evalService);
		Checker listener = new Checker();
		proxy.addHandlerListener(listener);
		assertFalse(proxy.isEnabled());
		ISourceProviderService providers = (ISourceProviderService) fWorkbench
				.getService(ISourceProviderService.class);
		WorkbenchSourceProvider selectionProvider = (WorkbenchSourceProvider) providers
				.getSourceProvider(ISources.ACTIVE_CURRENT_SELECTION_NAME);

		selectionProvider.selectionChanged(null, StructuredSelection.EMPTY);
		assertFalse(proxy.isEnabled());
		assertFalse(listener.lastChange);

		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object()));
		assertFalse(proxy.isEnabled());
		assertFalse(listener.lastChange);

		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object[] { new Object(), new Object() }));
		assertTrue(proxy.isEnabled());
		assertTrue(listener.lastChange);

		listener.lastChange = false;
		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object[] { new Object(), new Object(), new Object() }));
		assertFalse(proxy.isEnabled());
		assertTrue(listener.lastChange);
	}

// RAP [if] Commented as it fails with RAP X
// RAP [hs] the reason why this test fails is because
//	org.eclipse.ui.resourcePerspective is contributed via the ide
//	public void testEnablementForLocalContext() throws Exception {
//		openTestWindow("org.eclipse.ui.resourcePerspective");
//		activation1 = handlerService.activateHandler(CMD1_ID, contextHandler,
//				new ActiveContextExpression(CONTEXT_TEST1,
//						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
//		assertFalse(cmd1.isHandled());
//		assertFalse(cmd1.isEnabled());
//		IEvaluationContext snapshot = handlerService
//				.createContextSnapshot(false);
//		cmd1.setEnabled(snapshot);
//		assertFalse(cmd1.isEnabled());
//
//		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
//		assertTrue(cmd1.isHandled());
//		cmd1.setEnabled(snapshot);
//		assertTrue(cmd1.isEnabled());
//		assertEquals(contextHandler, cmd1.getHandler());
//
//		snapshot.removeVariable(ISources.ACTIVE_PART_NAME);
//		assertTrue(cmd1.isHandled());
//		cmd1.setEnabled(snapshot);
//		assertFalse(cmd1.isEnabled());
//		cmd1.setEnabled(handlerService.getCurrentState());
//		assertTrue(cmd1.isEnabled());
//		assertEquals(contextHandler, cmd1.getHandler());
//
//		snapshot.addVariable(ISources.ACTIVE_PART_NAME, handlerService
//				.getCurrentState().getVariable(ISources.ACTIVE_PART_NAME));
//		cmd1.setEnabled(snapshot);
//		assertTrue(cmd1.isEnabled());
//		cmd1.setEnabled(handlerService.getCurrentState());
//		assertTrue(cmd1.isEnabled());
//		assertEquals(contextHandler, cmd1.getHandler());
//	}

}
