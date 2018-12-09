package home.enviroment.modulemanager.test.utils;

import com.google.common.util.concurrent.Service;

public class ServiceManager {
	
	public static void startService(Service service) {
		service.startAsync();
		service.awaitRunning();
	}
	
	public static void stopService(Service service) {
		service.stopAsync();
		service.awaitTerminated();
	}
}
