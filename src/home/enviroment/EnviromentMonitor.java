package home.enviroment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

import home.enviroment.services.ConfigurationService;
import home.enviroment.services.MonitorService;
import home.enviroment.services.SenseMesurementService;
import home.enviroment.services.SenseMesurementTransferService;
import home.enviroment.services.SensePersistanceService;

public class EnviromentMonitor implements EnviromentMonitorMBean {
	
	private static final String LOG_PROPS_FILE = "/home/enviroment/resources/logging.properties";
	private static final String JMX_SERVICE_URL_STR = "service:jmx:rmi:///jndi/rmi://:1099/jmxrmi";

	private static final String NAME = "EnviromentMonitor";
	private static final int CHECK_STATUS_TIMEOUT = 5;
	
	private ServiceManager serviceManager = null;
	private Set<Service> services = new HashSet<>();
	
	private MBeanServer mbeanServer = null;
	private ObjectName objectName = null;
	
	
	public EnviromentMonitor() {
	}
	
	private void addServices() {
		services.add(ConfigurationService.getInstance());
		services.add(MonitorService.getInstance());
		services.add(SensePersistanceService.getInstance());
		services.add(SenseMesurementService.getInstance());
		services.add(SenseMesurementTransferService.getInstance());
		serviceManager = new ServiceManager(services);
	}
	
	private void loadLoggerProps() throws SecurityException, IOException {
		InputStream inputStream = EnviromentMonitor.class.getResourceAsStream(LOG_PROPS_FILE);
		LogManager.getLogManager().readConfiguration(inputStream);
	}
	
	private void registerMBean() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		mbeanServer = ManagementFactory.getPlatformMBeanServer();
		objectName = new ObjectName(EnviromentMonitor.class.getPackage().getName() + ":type=EnviromentMonitor");
		mbeanServer.registerMBean(this, objectName);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void start() {
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
	}
	
	@Override
	public void stop() {
		if(isRunning()) {
			serviceManager.stopAsync();
			serviceManager.awaitStopped();
		} else {
			Logger.getAnonymousLogger().log(Level.WARNING, "Unable to stop. EnviromentMonitor is not running");
		}
	}

	@Override
	public void reload() {
		for(Service service : services) {
			if(service instanceof ConfigurationService) {
				ConfigurationService confService = (ConfigurationService)service;
				confService.reloadConfiguration();
			}
		}
	}
	
	@Override
	public boolean isRunning() {
		return serviceManager.isHealthy();
	}

	private static void startSequence() {
		EnviromentMonitor enviromentMonitor = new EnviromentMonitor();
		
		try {
			enviromentMonitor.loadLoggerProps();
		} catch (SecurityException | IOException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to load log properties", e);
			System.exit(1);
		}
		
		try {
			enviromentMonitor.registerMBean();
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException
				| MBeanRegistrationException | NotCompliantMBeanException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to register MBean", e);
			System.exit(1);
		}
		
		enviromentMonitor.addServices();
		enviromentMonitor.start();
		if(enviromentMonitor.isRunning()) {
			Logger.getAnonymousLogger().log(Level.INFO, "EnviromentMonitor started");
		}
		
		while(enviromentMonitor.isRunning()) {
			try {
				TimeUnit.SECONDS.sleep(CHECK_STATUS_TIMEOUT);
			} catch (InterruptedException e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "EnviromentMonitor interrupted");
			}
		}
		Logger.getAnonymousLogger().log(Level.INFO, "EnviromentMonitor stopped");
	}
	
	private static EnviromentMonitorMBean getEnviromentMonitorMBean() throws IOException, MalformedObjectNameException {
		JMXServiceURL url = new JMXServiceURL(JMX_SERVICE_URL_STR);
		JMXConnector connector = JMXConnectorFactory.connect(url, null);
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		ObjectName name = new ObjectName(EnviromentMonitor.class.getPackage().getName() + ":type=EnviromentMonitor");
		EnviromentMonitorMBean enviromentMonitor = JMX.newMBeanProxy(connection, name, EnviromentMonitorMBean.class, true);
		return enviromentMonitor;
	}
	
	private static void stopSequence() {
		try{
			getEnviromentMonitorMBean().stop();
		} catch(Exception e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to stop EnviromentMonitor", e);
			System.exit(1);
		}
	}
	
	private static void reloadSequence() {
		try{
			getEnviromentMonitorMBean().reload();
		} catch(Exception e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to reload EnviromentMonitor", e);
			System.exit(1);
		}
	}
	
	private static void isRunningSequence() {
		try{
			if(getEnviromentMonitorMBean().isRunning()) {
				System.exit(0);
			} else {
				System.exit(1);
			}
		} catch(Exception e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to get status", e);
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		String command = args[0];
		
		ARGS arg = ARGS.fromName(command);
		if(arg == null) {
			Logger.getAnonymousLogger().log(Level.SEVERE, String.format("Unknown argument: %s. Allowed arguments are: %s ", command, Arrays.toString(ARGS.values())));
			System.exit(1);
		}
		
		switch(arg) {
		case START:
			startSequence();
			break;
		case STOP:
			stopSequence();
			break;
		case RELOAD:
			reloadSequence();
			break;
		case STATUS:
			isRunningSequence();
			break;
		}
	}
	
	enum ARGS {
		START,
		STOP,
		RELOAD,
		STATUS;
		
		public static ARGS fromName(String name) {
			if(name.equalsIgnoreCase("start")) {
				return START;
			}else if(name.equalsIgnoreCase("stop")) {
				return STOP;
			} else if(name.equalsIgnoreCase("reload")) {
				return RELOAD;
			} else if(name.equalsIgnoreCase("status")) {
				return STATUS;
			} else {
				return null;
			}
		}
	}
}
