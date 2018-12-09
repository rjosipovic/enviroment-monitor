package home.enviroment.modulemanager.test.services;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import home.enviroment.config.Prop;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.MonitorService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonitorServiceTest {

	private static ConfigurationService configurationService = ConfigurationService.getInstance();;
	private static MonitorService monitor = MonitorService.getInstance();
	private static Socket socket;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		startServices();
		assertTrue(configurationService.isRunning());
		assertTrue(monitor.isRunning());
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopMonitor();
		assertFalse(monitor.isRunning());
		
		try{
			connect();
		}catch(Exception ex) {
			assertTrue(ex instanceof ConnectException);
		}
		
		stopConfig();
		assertFalse(configurationService.isRunning());
	}
	
	private static void startServices() {
		ServiceManager.startService(configurationService);
		ServiceManager.startService(monitor);
	}
	
	private static void stopMonitor() {
		ServiceManager.stopService(monitor);
	}
	
	private static void stopConfig() {
		ServiceManager.stopService(configurationService);
	}
	
	private static void connect() throws UnknownHostException, IOException {
		String serverIP = configurationService.getProperty(Prop.MONITOR_SERVER_IP);
		int serverPort = Integer.parseInt(configurationService.getProperty(Prop.MONITOR_SERVER_PORT));
		socket = new Socket(serverIP, serverPort);
	}
	
	private String readData() throws IOException {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			return reader.readLine();
		}
	}
	
	private void disconnect() throws IOException {
		if(socket != null && socket.isConnected()) {
			socket.close();
		}
	}

	@Test
	public void basic() throws UnknownHostException, IOException {
		connect();
		assertEquals(configurationService.getProperty(Prop.MONITOR_STATUS_MSG), readData());
		assertTrue(socket.isConnected());
		disconnect();
	}
}
