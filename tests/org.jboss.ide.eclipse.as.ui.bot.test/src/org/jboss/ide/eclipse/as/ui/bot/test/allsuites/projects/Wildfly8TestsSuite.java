package org.jboss.ide.eclipse.as.ui.bot.test.allsuites.projects;

import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.CreateWildfly8Server;
import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.DeleteServerWildfly8Server;
import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.DeployJSPProjectWildfly8Server;
import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.HotDeployJSPFileWildfly8Server;
import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.OperateWildfly8Server;
import org.jboss.ide.eclipse.as.ui.bot.test.wildfly8.UndeployJSPProjectWildfly8Server;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(RedDeerSuite.class)
@Suite.SuiteClasses({
		CreateWildfly8Server.class,
		OperateWildfly8Server.class,
		DeployJSPProjectWildfly8Server.class,
		HotDeployJSPFileWildfly8Server.class,
		UndeployJSPProjectWildfly8Server.class,
		DeleteServerWildfly8Server.class
})
public class Wildfly8TestsSuite {

}
