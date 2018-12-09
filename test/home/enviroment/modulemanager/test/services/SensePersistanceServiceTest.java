package home.enviroment.modulemanager.test.services;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import home.enviroment.modulemanager.test.utils.MesurementDataProvider;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.FileCreatedListener;
import home.enviroment.services.SensePersistanceService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensePersistanceServiceTest {
	
	private static ConfigurationService configService;
	private static SensePersistanceService persistanceService;
	private static MesurementDataProvider mesurementsDataProvider;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		configService = ConfigurationService.getInstance();
		persistanceService = SensePersistanceService.getInstance();
		mesurementsDataProvider = MesurementDataProvider.getInstance();
		ServiceManager.startService(configService);
		ServiceManager.startService(persistanceService);
		assertTrue(configService.isRunning());
		assertTrue(persistanceService.isRunning());
	}

	@Test
	public void test() throws InterruptedException {
		List<SenseMesurement> mesurements = mesurementsDataProvider.getMesurements();
		for(SenseMesurement mesurement : mesurements) {
			persistanceService.addMesurement(mesurement);
		}
		final Set<String> finishedFiles = new HashSet<>();
		persistanceService.addFileFinishedListener(new FileCreatedListener() {
			
			@Override
			public void onFileCreated(String file) {
				System.out.println(file);
				finishedFiles.add(file);
			}
		});
		for(String file : finishedFiles) {
			assertTrue(Files.exists(Paths.get(file)));
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ServiceManager.stopService(persistanceService);
		ServiceManager.stopService(configService);
		assertFalse(configService.isRunning());
		assertFalse(persistanceService.isRunning());		
	}
}
