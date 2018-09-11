package home.enviroment.services;

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

	private static final String SERVER_IP = "192.168.0.15";
	private static final int SERVER_PORT = 1234;
	private static final int MAX_WORKER_THREADS = 10;
	private static final int SERVER_SOCKER_SO_TO = 2000;
	
	private static final Logger LOG = Logger.getLogger(MonitorService.class.getCanonicalName());
	
	private static MonitorService instance;
	private ExecutorService exec = Executors.newFixedThreadPool(MAX_WORKER_THREADS);
	
	public static synchronized MonitorService getInstance() {
		if(instance == null) {
			instance = new MonitorService();
		}
		return instance;
	}
	
	private ServerSocket serverSocket;
	
	public MonitorService() {
	}
	
	@Override
	protected void startUp() throws Exception {
		serverSocket = new ServerSocket(SERVER_PORT, 0, InetAddress.getByName(SERVER_IP));
		serverSocket.setSoTimeout(SERVER_SOCKER_SO_TO);
		LOG.log(Level.INFO, String.format("Server bound [%s]", serverSocket));
	}
	
	@Override
	protected void run() throws Exception {
		while(isRunning()) {
			try {
				acceptConnection();
			}catch (InterruptedIOException e) {
				//continue
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void acceptConnection() throws IOException {
		Socket socket = serverSocket.accept();
		LOG.log(Level.INFO, String.format("Connection: [%s] accepted", socket));
		exec.execute(new MonitorWorker(socket));
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
		exec.shutdown();
	}
}
