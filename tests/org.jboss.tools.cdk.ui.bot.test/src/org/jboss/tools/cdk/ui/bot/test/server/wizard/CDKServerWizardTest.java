/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.cdk.ui.bot.test.server.wizard;

import org.jboss.reddeer.common.logging.Logger;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardDialog;
import org.jboss.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardPage;
import org.jboss.reddeer.swt.condition.WidgetIsEnabled;
import org.jboss.reddeer.swt.impl.button.NextButton;
import org.jboss.tools.cdk.reddeer.requirements.DisableSecureStorageRequirement.DisableSecureStorage;
import org.jboss.tools.cdk.reddeer.server.ui.wizard.NewCDKServerContainerWizardPage;
import org.jboss.tools.cdk.ui.bot.test.utils.CDKTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests of CDK 2.x Server Wizard
 * @author odockal
 *
 */
@DisableSecureStorage
public class CDKServerWizardTest extends CDKServerWizardAbstractTest {
	
	private static Logger log = Logger.getLogger(CDKServerWizardTest.class);

	// page description messages
	
	private static final String NO_VAGRANTFILE = "does not have a Vagrantfile";
	
	@BeforeClass
	public static void setup() {
		checkVagrantfileParameters();
	}
	
	@Test
	public void testCDK2xServerType() {
		assertServerType(CDK_SERVER_NAME);
	}
	
	@Test
	public void testNewCDKServerWizard() {
		NewServerWizardDialog dialog = CDKTestUtils.openNewServerWizardDialog();
		NewServerWizardPage page = new NewServerWizardPage();
		
		page.selectType(SERVER_TYPE_GROUP, CDK_SERVER_NAME);
		dialog.next();
		NewCDKServerContainerWizardPage containerPage = new NewCDKServerContainerWizardPage();
		
		checkWizardPagewidget("Folder: ", CDK_SERVER_NAME);

		// just check that default domain is choosen correctly
		assertTrue(containerPage.getDomain().equalsIgnoreCase(CREDENTIALS_DOMAIN));
		
		// to change dialog page description folder must be set with path where is no vagrantfile
		containerPage.setFolder(NON_EXECUTABLE_FILE);
		
		// first error description will demand vagrantfile in the path or not?
		// seems that adding the user has priority over checking vagrantfile
		assertSameMessage(dialog, NO_USER);
		containerPage.setCredentials(USERNAME, PASSWORD);
		// now the description should have changed to "no vagrantfile" 
		assertSameMessage(dialog, NO_VAGRANTFILE);
		containerPage.setFolder(NON_EXISTING_PATH);
		assertSameMessage(dialog, DOES_NOT_EXIST);
		containerPage.setFolder(VAGRANTFILE_PATH);
		assertDiffMessage(dialog, DOES_NOT_EXIST);
		new WaitUntil(new WidgetIsEnabled(new NextButton()), TimePeriod.SHORT, false);
		assertTrue("Expected Finish button is not enabled", dialog.isFinishEnabled());
		dialog.finish();
	}
}