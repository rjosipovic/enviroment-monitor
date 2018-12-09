package home.enviroment.services;

import home.enviroment.config.Prop;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;

import com.google.common.util.concurrent.AbstractIdleService;

public class ConfigurationService extends AbstractIdleService implements EventListener<Event> {
	
	private static final Logger LOG = Logger.getLogger(ConfigurationService.class.getName());
	
	private static ConfigurationService instance = null;

	public static synchronized ConfigurationService getInstance() {
		if(instance == null) {
			instance = new ConfigurationService();
		}
		return instance;
	}
	
	private ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder;
	private PeriodicReloadingTrigger trigger;
	private Configuration config;
	private String configFile;
	
	public ConfigurationService() {
		configFile = System.getProperty("application.configuration");
	}

	@Override
	protected void startUp() throws Exception {
		LOG.info(String.format("About to start ConfigurationService with file: %s", configFile));
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		Parameters params = new Parameters();
		builder = new ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class);
		builder.configure(params.properties()
				.setFileName(configFile));
		builder.setAutoSave(Boolean.TRUE);
		builder.addEventListener(ConfigurationBuilderEvent.RESET, this);
		trigger = new PeriodicReloadingTrigger(builder.getReloadingController(), null, 1, TimeUnit.SECONDS, exec);
		trigger.start();
		config = builder.getConfiguration();
		LOG.info(String.format("%s started", ConfigurationService.class.getSimpleName()));
	}
	
	public String getProperty(Prop name) {
		LOG.fine(String.format("About to get property: {%s}", name.name()));
		String propValue = config.getString(name.getName(), name.getDefaultvalue());
		LOG.fine(String.format("Property: {%s} has value: {%s}", name.name(), propValue));
		return propValue;
	}
	
	public synchronized void setProperty(Prop name, String value) throws ConfigurationException {
		LOG.fine(String.format("About to set property: {%s} with value: {%s}", name.name(), value));
		config.setProperty(name.getName(), value);
	}
	
	private void shutdownTrigger() {
		if(trigger != null && trigger.isRunning()) {
			LOG.info("About to shutdown configuration reloading trigger");
			trigger.shutdown();
		}
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.info("About to stop ConfigurationService");
		shutdownTrigger();
	}

	@Override
	public void onEvent(Event event) {
		LOG.info("Configuration reset triggered");
		try {
			config = builder.getConfiguration();		
		} catch(ConfigurationException e) {
			LOG.log(Level.SEVERE, "Unable to reload configuration", e);
		}
	}
}
