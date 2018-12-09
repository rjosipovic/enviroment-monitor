package home.enviroment.modulemanager.test.jobs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import home.enviroment.config.Prop;
import home.enviroment.job.RetrieveSenseMesurementJob;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.FileCreatedListener;
import home.enviroment.services.SensePersistanceService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "home.enviroment.job.RetrieveSenseMesurementJob")
public class RetrieveSenseMesurementJobTest {

	private static ConfigurationService configurationService;
	private static SensePersistanceService persistanceService;
	private static Map<String, String> rowValues = new LinkedHashMap<String, String>();
	private static Pattern pattern = Pattern
			.compile("^(MesureTime: (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})) (Temperature: (\\d{2}.\\d{2})) (Humidity: (\\d{2}.\\d{2})) (Pressure: (\\d{3,4}.\\d{2}))$");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rowValues.put("temperature", "34.08");
		rowValues.put("CPU temp", "54.8");
		rowValues.put("temp_calibrated", "30.30");
		rowValues.put("humidity", "33.94");
		rowValues.put("pressure", "1009.64");

		System.setProperty("logging.configuration",
				"conf/test/logging.properties");
		System.setProperty("application.configuration",
				"conf/test/enviroment-monitor.properties");
		configurationService = ConfigurationService.getInstance();
		persistanceService = SensePersistanceService.getInstance();
		ServiceManager.startService(configurationService);
		ServiceManager.startService(persistanceService);
		assertTrue(configurationService.isRunning());
		assertTrue(persistanceService.isRunning());
	}

	@Test
	public void test() throws Exception {

		persistanceService.addFileFinishedListener(new FileCreatedListener() {

			@Override
			public void onFileCreated(String file) {
				Path finishedFile = Paths.get(file);
				assertTrue(Files.exists(finishedFile));
				try {
					assertTrue(dataMatch(Files.readAllLines(finishedFile)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		RetrieveSenseMesurementJob job = new RetrieveSenseMesurementJob(
				configurationService
						.getProperty(Prop.SENSE_MESUREMENT_SCRIPT_PATH),
				configurationService
						.getProperty(Prop.SENSE_MESUREMENT_TEMP_PATTERN),
				configurationService
						.getProperty(Prop.SENSE_MESUREMENT_HUMIDITY_PATTERN),
				configurationService
						.getProperty(Prop.SENSE_MESUREMENT_PRESSURE_PATTERN));
		RetrieveSenseMesurementJob mock = PowerMockito.spy(job);
		PowerMockito.doReturn(getLines()).when(mock, "readLines");
		Thread t = new Thread(mock);
		t.start();
		t.join();

	}

	private boolean dataMatch(List<String> lines) {
		if (lines != null && lines.size() == 1) {
			String line = lines.get(0);
			Matcher m = pattern.matcher(line);
			if (m.find()) {
				String temperature = m.group(4);
				String humidity = m.group(6);
				String pressure = m.group(8);
				return temperature.equals(rowValues.get("temp_calibrated"))
						&& humidity.equals(rowValues.get("humidity"))
						&& pressure.equals(rowValues.get("pressure"));
			}
		}
		return false;
	}

	private List<String> getLines() {
		List<String> lines = new ArrayList<String>();
		for (String key : rowValues.keySet()) {
			lines.add(String.format("%s:%s", key, rowValues.get(key)));
		}
		return lines;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ServiceManager.stopService(configurationService);
		ServiceManager.stopService(persistanceService);
	}
}
