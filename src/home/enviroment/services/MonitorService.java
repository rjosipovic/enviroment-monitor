package home.enviroment.services;

import home.enviroment.config.Prop;
import home.enviroment.monitor.MonitorWorker;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class MonitorService extends AbstractExecutionThreadService {

	private static final Logger LOG = Logger.getLogger(MonitorService.class.getCanonicalName());
	
	private static MonitorService instance;
	
	public static synchronized MonitorService getInstance() {
		if(instance == null) {
			instance = new MonitorService();
		}
		return instance;
	}
	
	private ConfigurationService configurationService = ConfigurationService.getInstance();
	private ExecutorService exec = null;
	private ServerSocket serverSocket = null;
	
	public MonitorService() {
	}
	
	@Override
	protected void startUp() throws Exception {
		
		int maxWorkers = Integer.parseInt(configurationService.getProperty(Prop.MONITOR_MAX_WORKERS));
		int serverPort = Integer.parseInt(configurationService.getProperty(Prop.MONITOR_SERVER_PORT));
		String serverIP = configurationService.getProperty(Prop.MONITOR_SERVER_IP);
		int serverSocketTO = Integer.parseInt(configurationService.getProperty(Prop.MONITOR_SERVER_SOCKET_TO));
		
		exec = Executors.newFixedThreadPool(maxWorkers);
		
		serverSocket = new ServerSocket(serverPort, 0, InetAddress.getByName(serverIP));
		serverSocket.setSoTimeout(serverSocketTO);
		
		LOG.log(Level.INFO, String.format("Server bound [%s]", serverSocket));
	}
	
	@Override
	protected void run() throws Exception {
		while(isRunning()) {
			try {
				acceptConnection();
			}catch (InterruptedIOException e) {
				//continue if there are no connections in specified timeout 
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void acceptConnection() throws IOException {
		Socket socket = serverSocket.accept();
		LOG.log(Level.INFO, String.format("Connection: [%s] accepted", socket));
		String statusMsg = configurationService.getProperty(Prop.MONITOR_STATUS_MSG);
		exec.execute(new MonitorWorker(socket, statusMsg));
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.log(Level.INFO, "Closing server socket");
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(serverSocket.isClosed()) {
				LOG.log(Level.INFO, "Server socket closed");
			}
		}
		if(exec != null) {
			exec.shutdown();
		}
	}
}
