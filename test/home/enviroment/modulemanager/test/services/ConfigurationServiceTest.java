package home.enviroment.modulemanager.test.services;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import home.enviroment.config.Prop;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.services.ConfigurationService;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationServiceTest {
	
	private static ConfigurationService configurationService = null;

	@BeforeClass
	public static void setUp() {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");

		configurationService = ConfigurationService.getInstance();
		ServiceManager.startService(configurationService);
	}
	
	@Test
	public void configServiceRunning() {
		assertTrue(configurationService.isRunning());
	}
	
	private String getValueFromFile(String propName) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(System.getProperty("application.configuration")));
		String propValue = null;
		for(String line : lines) {
			if(line.startsWith(propName)) {
				propValue = line.substring(line.indexOf('=')+1);
				break;
			}
		}
		return propValue;
	}

	@Test
	public void testGetValueFromFile() throws IOException {
		String propName = Prop.SENSE_MESUREMENT_PERSIST_FOLDER.getName();
		String propValue = getValueFromFile(propName);
		assertEquals(propValue, configurationService.getProperty(Prop.SENSE_MESUREMENT_PERSIST_FOLDER));
	}
	
	@Test
	public void configParamChanges() throws ConfigurationException {
		String oldValue = "EnviromentMonitor";
		String newValue = "HomeEnviroment";
		assertEquals(configurationService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME), oldValue);
		configurationService.setProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME, newValue);
		assertEquals(configurationService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME), newValue);
		configurationService.setProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME, oldValue);
		assertEquals(configurationService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME), oldValue);
	}
	
	private void changeConfigValueInFile(final Prop property, final String newValue) throws IOException, InterruptedException {
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					try{
						String propName = property.getName();
						Path path = Paths.get(System.getProperty("application.configuration"));
						List<String> lines = new ArrayList<>();
						BufferedReader reader = Files.newBufferedReader(path);
						String line = null;
						while((line = reader.readLine()) != null) {
							lines.add(line);
						}
						reader.close();
						
						for(int i=0; i<lines.size(); i++) {
							line = lines.get(i);
							if(line.startsWith(propName)) {
								line = line.substring(0, line.indexOf('=') + 1) + newValue;
								lines.set(i, line);
							}
						}
						BufferedWriter writer = Files.newBufferedWriter(path);
						for(String l : lines) {
							writer.write(l);
							writer.write("\n");
						}
						writer.flush();
						writer.close();
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		TimeUnit.SECONDS.sleep(2);
	}
	
	//@Test
	public void reloadConfig() throws IOException, InterruptedException {
		Prop property = Prop.ENVIROMENT_SERVICE_APPLICATION_NAME;
		String propName = property.getName();
		String propValue = getValueFromFile(propName);
		
		assertEquals(propValue, configurationService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME));
		
		changeConfigValueInFile(property, "changed");
		System.out.println("Waiting for property to change");
		TimeUnit.SECONDS.sleep(10);
		
		System.out.println("Checking change");
		assertEquals("changed", configurationService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME));
		changeConfigValueInFile(property, "EnviromentMonitor");
	}

	@Test
	public void configServiceStopping() {
		ServiceManager.stopService(configurationService);
		assertFalse(configurationService.isRunning());
	}

	@AfterClass
	public static void cleanUp() {
		ServiceManager.stopService(configurationService);
	}
}
