package home.enviroment.modulemanager.test;

import static org.junit.Assert.*;

import home.enviroment.ModuleManager;

import org.junit.BeforeClass;
import org.junit.Test;

public class ModuleManagerTester {
	
	private ModuleManager moduleManager;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
	}

	@Test
	public void start() throws Exception {
		moduleManager = ModuleManager.getInstance();
		assertFalse(moduleManager.isRunning());
		moduleManager.start();
		assertTrue(moduleManager.isRunning());
		assertEquals("EnviromentMonitor", moduleManager.getName());
		moduleManager.stop();
		assertFalse(moduleManager.isRunning());
	}
}
