package home.enviroment.services;

import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractIdleService;

public class ConfigurationService extends AbstractIdleService {
	
	private static final Logger LOG = Logger.getLogger(ConfigurationService.class.getName());
	
	//SenseJob
	private static final String SENSE_HAT_SCRIPT_PATH = "/home/pi/sense-hat/log-enviroment.py";
	private static final String TEMP_PATTERN = "^(temperature:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$";
	private static final String HUMIDITY_PATTERN = "^(humidity:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$";
	private static final String PRESSURE_PATTERN = "^(pressure:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$";
	private static final String SENSE_PERSIST_FOLDER = "/tmp/";
	private static final int MAX_PERSISTED_ELEMENTS = 100;
	//SenseJob
	
	private static ConfigurationService instance = null;
	
	public static synchronized ConfigurationService getInstance() {
		if(instance == null) {
			instance = new ConfigurationService();
		}
		return instance;
	}
	
	public ConfigurationService() {
	}

	@Override
	protected void startUp() throws Exception {
		LOG.info("About to start ...");
	}
	
	public void reloadConfiguration() {
		LOG.info("Reloading configuration");
		//TODO
	}
	
	public int getMaxPersistedElements() {
		return MAX_PERSISTED_ELEMENTS;
	}
	
	public String getSensePersistFolder() {
		return SENSE_PERSIST_FOLDER;
	}
	
	public String getSenseHatScriptPath() {
		return SENSE_HAT_SCRIPT_PATH;
	}
	
	public String getTempPattern() {
		return TEMP_PATTERN;
	}
	
	public String getHumidityPattern() {
		return HUMIDITY_PATTERN;
	}
	
	public String getPressurePattern() {
		return PRESSURE_PATTERN;
	}

	@Override
	protected void shutDown() throws Exception {
		LOG.info("About to stop ...");
	}
}
