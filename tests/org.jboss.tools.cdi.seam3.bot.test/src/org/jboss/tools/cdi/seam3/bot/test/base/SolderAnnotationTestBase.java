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

package org.jboss.tools.cdi.seam3.bot.test.base;

import java.util.List;

import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.tools.cdi.bot.test.CDIConstants;
import org.jboss.tools.cdi.bot.test.annotations.ProblemsType;
import org.jboss.tools.ui.bot.ext.helper.OpenOnHelper;

/**
 * 
 * @author jjankovi
 *
 */
public class SolderAnnotationTestBase extends Seam3TestBase {
	
	protected String APPLICATION_CLASS = "Application.java";
	
	/**
	 * 
	 * @param projectName
	 */
	protected void testNoBeanValidationProblemExists(String projectName) {
		
		testBeanValidationProblemExists(projectName, true);
		
	}
	
	/**
	 * 
	 * @param projectName
	 */
	protected void testMultipleBeansValidationProblemExists(String projectName) {
		
		testBeanValidationProblemExists(projectName, false);
		
	}
	
	/**
	 * 
	 * @param projectName
	 * @param noBeanEligible
	 */
	private void testBeanValidationProblemExists(String projectName, boolean noBeanEligible) {
		
		List<TreeItem> validationProblems = quickFixHelper.getProblems(
				ProblemsType.WARNINGS, projectName);
		assertTrue(validationProblems.size() > 0);
		String validationMessage = noBeanEligible?
				CDIConstants.NO_BEAN_IS_ELIGIBLE:
				CDIConstants.MULTIPLE_BEANS;
		for (TreeItem ti : validationProblems) {
			if (ti.getText().contains(validationMessage)) {
				return;
			}
		}
		fail("CDI Validation problem with text '" 
				+ validationMessage 
				+ "' was not found!");
	}
	
	/**
	 * 
	 * @param projectName
	 * @param openOnString
	 * @param openedClass
	 * @param producer
	 * @param producerMethod
	 */
	protected void testProperInjectBean(String projectName, 
			String openOnString, String openedClass) {
		
		testProperInject(projectName, openOnString, openedClass, false, null);
		
	}
	
	/**
	 * 
	 * @param projectName
	 * @param openOnString
	 * @param openedClass
	 */
	protected void testProperInjectProducer(String projectName, 
			String openOnString, String openedClass, 
			String producerMethod) {
		
		testProperInject(projectName, openOnString, openedClass, true, producerMethod);
		
	}
	
	/**
	 * 
	 * @param projectName
	 * @param openOnString
	 * @param openedClass
	 * @param producer
	 * @param producerMethod
	 */
	private void testProperInject(String projectName, String openOnString, 
			String openedClass, 
			boolean producer, String producerMethod) {
		
		List<TreeItem> validationProblems = quickFixHelper.getProblems(
				ProblemsType.WARNINGS, projectName);
		assertTrue(validationProblems.size() == 0);
		OpenOnHelper.checkOpenOnFileIsOpened(bot, APPLICATION_CLASS, 
				openOnString, CDIConstants.OPEN_INJECT_BEAN, openedClass + ".java");
		if (producer) {
			assertTrue(bot.activeEditor().toTextEditor().
					getSelection().equals(producerMethod));
		}
		
	}

}
