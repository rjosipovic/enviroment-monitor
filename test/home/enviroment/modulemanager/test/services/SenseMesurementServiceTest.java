package home.enviroment.modulemanager.test.services;

import static org.junit.Assert.*;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.SenseMesurementService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SenseMesurementServiceTest {
	
	private static ConfigurationService configurationService;
	private static SenseMesurementService mesurementService;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		
		configurationService = ConfigurationService.getInstance();
		mesurementService = SenseMesurementService.getInstance();
		
		ServiceManager.startService(configurationService);
		assertTrue(configurationService.isRunning());
		ServiceManager.startService(mesurementService);
		assertTrue(mesurementService.isRunning());
	}

	@Test
	public void test() {
		System.out.println("!placeholder");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ServiceManager.stopService(mesurementService);
		assertFalse(mesurementService.isRunning());
		ServiceManager.stopService(configurationService);
		assertFalse(configurationService.isRunning());
	}
}
