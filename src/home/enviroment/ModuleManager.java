package home.enviroment;

import home.enviroment.config.Prop;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.MonitorService;
import home.enviroment.services.SenseMesurementService;
import home.enviroment.services.SenseMesurementTransferService;
import home.enviroment.services.SensePersistanceService;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.google.common.util.concurrent.Service;

public class ModuleManager implements ModuleManagerMBean {
	
	private static final Logger LOG = Logger.getLogger(ModuleManager.class.getName());
	
	public static ModuleManager getInstance() throws Exception {
		if(instance == null) {
			instance = new ModuleManager();
		}
		return instance;
	}
	
	private static ModuleManager instance;
	
	private LinkedList<Service> services = new LinkedList<>();
	
	private MBeanServer mbeanServer = null;
	private ObjectName objectName = null;
	
	private ModuleManager() throws Exception {

		//register MBean
		mbeanServer = ManagementFactory.getPlatformMBeanServer();
		objectName = new ObjectName(EnviromentMonitor.class.getPackage().getName() + ":type=EnviromentMonitor");
		mbeanServer.registerMBean(this, objectName);
		
		//add services on service stack
		services.add(ConfigurationService.getInstance());
		services.add(MonitorService.getInstance());
		services.add(SenseMesurementTransferService.getInstance());
		services.add(SensePersistanceService.getInstance());
		services.add(SenseMesurementService.getInstance());
	}
	
	@Override
	public String getName() {
		String name = null;
		if(isRunning()) {
			for(Service service : services) {
				if(service instanceof ConfigurationService) {
					ConfigurationService confService = (ConfigurationService)service;
					name = confService.getProperty(Prop.ENVIROMENT_SERVICE_APPLICATION_NAME);
				}
			}
		}
		return name;
	}
	
	@Override
	public void start() {
		LOG.info("About to start EniromentMonitor services");
		try{
			for(Service service : services) {
				service.startAsync();
				service.awaitRunning();
			}
		}catch(Exception e) {
			LOG.log(Level.SEVERE, "Unable to start EnviromentServices", e);
		}
		if(allServicesRunning()) {
			LOG.info("EnviromentMonitorServices are running");
		}
	}
	
	@Override
	public void stop() {
		LOG.log(Level.INFO, "About to stop EnviromentMonitor services");
		try{
			//stopping services in reverse order
			ListIterator<Service> it = services.listIterator(services.size());
			while(it.hasPrevious()) {
				Service service = it.previous();
				service.stopAsync();
				service.awaitTerminated();
			}
		}catch(Exception e) {
			LOG.log(Level.SEVERE, "Unable to stop EnviromentServices", e);			
		}
		if(allServicesTerminated()) {
			LOG.info("EnviromentMonitorServices stopped");
		} else {
			LOG.warning("Error in stopping EnviromentMonitorServices");
		}
	}
	
	@Override
	public boolean isRunning() {
		return allServicesRunning();
	}

	private boolean allServicesRunning() {
		boolean res = true;
		for(Service service : services) {
			if(service.isRunning()) {
				continue;
			} else {
				return false;
			}
		}
		return res;
	}

	private boolean allServicesTerminated() {
		boolean res = true;
		for(Service service : services) {
			if(!service.isRunning()) {
				continue;
			} else {
				return false;
			}
		}
		return res;
	}
}
