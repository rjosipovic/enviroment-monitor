package home.enviroment.modulemanager.test.jobs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import home.enviroment.config.Prop;
import home.enviroment.job.RetrieveSenseMesurementJob;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.sense.MesurementType;
import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.MesureTakenListener;

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
	private static Map<String, String> rowValues = new LinkedHashMap<String, String>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rowValues.put("temperature", "34.08");
		rowValues.put("humidity", "33.94");
		rowValues.put("pressure", "1009.64");

		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		configurationService = ConfigurationService.getInstance();
		ServiceManager.startService(configurationService);
		assertTrue(configurationService.isRunning());
	}

	@Test
	public void testTemperature() throws Exception {

		final MesurementType type = MesurementType.TEMPERATURE;
		RetrieveSenseMesurementJob job = new RetrieveSenseMesurementJob(type);
		job.addMesureTakenListener(new MesureTakenListener() {
			
			@Override
			public void onMesureTaken(SenseMesurement mesurement) {
				assertTrue(dataMatch(mesurement, type));
			}
		});
		
		RetrieveSenseMesurementJob mock = PowerMockito.spy(job);
		PowerMockito.doReturn(getLineForMesurementType(type)).when(mock, "readLine");
		Thread t = new Thread(mock);
		t.start();
		t.join();
	}

	@Test
	public void testPressure() throws Exception {

		final MesurementType type = MesurementType.PRESSURE;
		RetrieveSenseMesurementJob job = new RetrieveSenseMesurementJob(type);
		job.addMesureTakenListener(new MesureTakenListener() {
			
			@Override
			public void onMesureTaken(SenseMesurement mesurement) {
				assertTrue(dataMatch(mesurement, type));
			}
		});
		
		RetrieveSenseMesurementJob mock = PowerMockito.spy(job);
		PowerMockito.doReturn(getLineForMesurementType(type)).when(mock, "readLine");
		Thread t = new Thread(mock);
		t.start();
		t.join();
	}

	@Test
	public void testHumidity() throws Exception {

		final MesurementType type = MesurementType.HUMIDITY;
		RetrieveSenseMesurementJob job = new RetrieveSenseMesurementJob(type);
		job.addMesureTakenListener(new MesureTakenListener() {
			
			@Override
			public void onMesureTaken(SenseMesurement mesurement) {
				assertTrue(dataMatch(mesurement, type));
			}
		});
		
		RetrieveSenseMesurementJob mock = PowerMockito.spy(job);
		PowerMockito.doReturn(getLineForMesurementType(type)).when(mock, "readLine");
		Thread t = new Thread(mock);
		t.start();
		t.join();
	}

	private boolean dataMatch(SenseMesurement mesurement, MesurementType type) {
		float rowValueTemp = Float.parseFloat(rowValues.get("temperature"));
		float rowValuePressure = Float.parseFloat(rowValues.get("pressure"));
		float rowValueHumidity = Float.parseFloat(rowValues.get("humidity"));

		switch (type) {
		case TEMPERATURE:
			return mesurement.getValue() == rowValueTemp;
		case PRESSURE:
			return mesurement.getValue() == rowValuePressure;
		case HUMIDITY:
			return mesurement.getValue() == rowValueHumidity;
		default:
			return false;
		}
	}

	private String getLineForMesurementType(MesurementType type) {
		switch (type) {
		case TEMPERATURE:
			return rowValues.get("temperature");
		case PRESSURE:
			return rowValues.get("pressure");
		case HUMIDITY:
			return rowValues.get("humidity");
		default:
			return null;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ServiceManager.stopService(configurationService);
	}
}
