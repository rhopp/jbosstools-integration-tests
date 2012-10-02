/*******************************************************************************
 * Copyright (c) 2010-2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.ws.ui.bot.test.rest.validation;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.jboss.tools.ws.ui.bot.test.rest.RESTfulTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jjankovi
 *
 */
public class ApplicationPathAnnotationTest extends RESTfulTestBase {

	
	@Before
	public void setup() {		
		
	}
	
	@Test
	public void testMultipleApplicationClasses() {
		
		/* prepare project */
		importRestWSProject("app1");
		
		/* workaround for JBIDE-12690 
		jbide12680Workaround("app1", "src", "test", "App.java"); */
		
		/* test validation error */
		assertCountOfApplicationAnnotationValidationWarnings("app1", 2);
	}
	
	@Test
	public void testWebXmlAndApplicationClassWithWarning() {
		
		/* prepare project */
		importRestWSProject("app2");
		
		/* workaround for JBIDE-12690 
		jbide12680Workaround("app2", "src", "test", "App.java"); */
		
		/* test validation error */
		assertCountOfApplicationAnnotationValidationWarnings("app2", 2);
	}
	
	@Test
	public void testWebXmlAndApplicationClassWithoutWarning() {
		
		/* prepare project */
		importRestWSProject("app3");
		
		/* workaround for JBIDE-12690 
		jbide12680Workaround("app3", "src", "test", "App.java"); */
		
		/* test validation error */
		assertCountOfApplicationAnnotationValidationWarnings("app3", 0);
	}
	
	private void jbide12680Workaround(String projectName, String... path) {
		SWTBotEditor editor = packageExplorer.openFile(projectName, path);
		SWTBotEclipseEditor eclipseEditor = editor.toTextEditor();
		eclipseEditor.insertText(" ");
		eclipseEditor.save();
	}
	
}
