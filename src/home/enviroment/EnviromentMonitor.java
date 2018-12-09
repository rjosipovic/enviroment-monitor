package home.enviroment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import home.enviroment.config.Prop;

public class EnviromentMonitor {
	
	private static final Logger LOG = Logger.getLogger(EnviromentMonitor.class.getName());
	
	public static void main(String[] args) {
		
		loadLoggingProperies();
		checkApplicationProperty();
		validateArguments(args);

		ARGS arg = ARGS.fromName(args[0]);
		
		switch(arg) {
		case START:
			startSequence();
			break;
		case STOP:
			stopSequence();
			break;
		case STATUS:
			isRunningSequence();
			break;
		}
	}

	private static void loadLoggingProperies() {
		String loggingPropertiesFile = System.getProperty("logging.configuration");
		if(loggingPropertiesFile == null || loggingPropertiesFile.isEmpty()) {
			String msg = "Logging properties not defined. Exiting program";
			LOG.log(Level.WARNING, msg);
			throw new RuntimeException(msg);			
		} else {
			try{
				InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(System.getProperty("logging.configuration"))));
				LogManager.getLogManager().readConfiguration(inputStream);				
			} catch (SecurityException | IOException e) {
				String msg = "Unable to set logger. Exiting program";
				LOG.log(Level.WARNING, msg, e);
				throw new RuntimeException(msg);									
			} 
		}
	}

	private static void checkApplicationProperty() {
		String applicationPropertiesFile = System.getProperty("application.configuration");
		if(applicationPropertiesFile == null || applicationPropertiesFile.isEmpty()) {
			String msg = "Application properties not defined. Exiting program";
			LOG.log(Level.WARNING, msg);
			throw new RuntimeException(msg);			
		}		
	}

	private static void validateArguments(String[] args) {
		String command = args[0];
		LOG.info(String.format("Application argument: {%s}", command));
		ARGS arg = ARGS.fromName(command);
		if(arg == null) {
			String msg = String.format("Unknown argument: %s\nAllowed arguments are: %s\nExiting program.", command, Arrays.toString(ARGS.values()));
			LOG.log(Level.WARNING, msg);
			throw new UnsupportedOperationException(msg);
		}		
	}
	
	private static void startSequence() {
		ModuleManagerMBean enviromentMonitorMBean = getEnviromentMonitorMBean();
		if(enviromentMonitorMBean == null) {
			ModuleManager moduleManager = null;
			try {
				moduleManager = ModuleManager.getInstance();
			} catch (Exception ex) {
				String msg = "Unable to get ModuleInstance";
				LOG.log(Level.SEVERE, msg, ex);
				return;
			}
			moduleManager.start();
			applicationLoop(moduleManager);
		} else {
			if(enviromentMonitorMBean.isRunning()) {
				LOG.log(Level.SEVERE, "EnviromentMonitor already running");
				return;
			} else {
				enviromentMonitorMBean.start();
			}				
		}
	}
	
	private static void applicationLoop(ModuleManager moduleManager) {
		LOG.log(Level.INFO, "Entering application loop");
		while(moduleManager.isRunning()) {
			try {
				TimeUnit.SECONDS.sleep(Long.parseLong(Prop.ENVIROMENT_SERVICE_APPLICATION_STATUS_TIMEOUT.getDefaultvalue()));
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "EnviromentMonitor interrupted");
			}
		}
		LOG.log(Level.INFO, "Exiting application loop");
	}
	
	private static void stopSequence() {
		LOG.log(Level.INFO, "About to begin stop sequence");
		ModuleManagerMBean enviromentMonitorMBean = getEnviromentMonitorMBean();
		if(enviromentMonitorMBean != null) {
			enviromentMonitorMBean.stop();
		} else {
			LOG.log(Level.SEVERE, "Unable to stop EnviromentMonitor");				
		}
	}
	
	private static void isRunningSequence() {
		ModuleManagerMBean enviromentMonitorMBean = getEnviromentMonitorMBean();
		if(enviromentMonitorMBean != null) {
			if(getEnviromentMonitorMBean().isRunning()) {
			} else {
			}			
		} else {
			LOG.log(Level.SEVERE, "Unable to get status");			
		}
	}

	private static ModuleManagerMBean getEnviromentMonitorMBean() {
		ModuleManagerMBean enviromentMonitor = null;
		try{
			JMXServiceURL url = new JMXServiceURL(Prop.JMX_SERVICE_URL.getDefaultvalue());
			JMXConnector connector = JMXConnectorFactory.connect(url, null);
			MBeanServerConnection connection = connector.getMBeanServerConnection();
			ObjectName name = new ObjectName(EnviromentMonitor.class.getPackage().getName() + ":type=EnviromentMonitor");
			enviromentMonitor = JMX.newMBeanProxy(connection, name, ModuleManagerMBean.class, true);			
		} catch(Exception e) {
			LOG.log(Level.INFO, "Unable to get ModuleManagerMBean");
			LOG.log(Level.FINE, "Reason:", e);
		}
		return enviromentMonitor;
	}

	enum ARGS {
		START,
		STOP,
		STATUS;
		
		public static ARGS fromName(String name) {
			if(name.equalsIgnoreCase("start")) {
				return START;
			}else if(name.equalsIgnoreCase("stop")) {
				return STOP;
			}else if(name.equalsIgnoreCase("status")) {
				return STATUS;
			} else {
				return null;
			}
		}
	}
}
