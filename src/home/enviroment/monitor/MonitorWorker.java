package home.enviroment.monitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorWorker implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(MonitorWorker.class.getName());
	
	private Socket socket;
	private String statusMsg;
	
	public MonitorWorker(Socket socket, String statusMsg) {
		this.socket = socket;
		this.statusMsg = statusMsg;
	}
	
	@Override
	public void run() {
		
		try{
			LOG.log(Level.INFO, "About to send status message");
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			writer.println(statusMsg);
			LOG.log(Level.INFO, String.format("Status message [%s] sent.", statusMsg));
			
		} catch(IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			disconnect();
		}
	}
	
	private void disconnect() {
		LOG.log(Level.INFO, "About to close connection");
		if(socket != null && !socket.isClosed()) {
			LOG.log(Level.INFO, "Closing connection");
			try {
				socket.close();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
			if(socket.isClosed()) {
				LOG.log(Level.INFO, "Disconnected");
			}
		} else {
			LOG.log(Level.INFO, "Connection already closed");
		}
	}
}
