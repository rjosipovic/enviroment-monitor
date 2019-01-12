package home.enviroment.modulemanager.test.services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import home.enviroment.config.Prop;
import home.enviroment.modulemanager.test.utils.MesurementDataProvider;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.SensePersistanceService;

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
	public void test() throws InterruptedException, IOException {
		List<SenseMesurement> mesurements = mesurementsDataProvider.getMesurements();
		for(SenseMesurement mesurement : mesurements) {
			persistanceService.addMesurement(mesurement);
		}
		ServiceManager.stopService(persistanceService);
		ServiceManager.stopService(configService);
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(configService.getProperty(Prop.SENSE_MESUREMENT_PERSIST_FOLDER)), "sense-mesurement*");
		
		Path persistedFile = directoryStream.iterator().next();
		List<String> persistedLines = Files.readAllLines(persistedFile);
		
		Path rawFile = Paths.get("persist/input/raw.txt");
		List<String> rowLines = Files.readAllLines(rawFile);
		assertTrue(persistedLines.containsAll(rowLines));
	}
}
