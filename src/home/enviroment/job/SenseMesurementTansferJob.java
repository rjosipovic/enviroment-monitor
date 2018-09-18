package home.enviroment.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import home.enviroment.services.SenseMesurementTransferService;

public class SenseMesurementTansferJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementTansferJob.class.getName());
	
	private static final String REMOTE_USER = "roman";
	private static final String REMOTE_PASS = "rJ38538!";
	private static final String REMOTE_HOST = "dorat";
	private static final int REMOTE_PORT = 22;
	private static final int CONNECT_TO = 5000;
	private static final String REMOTE_FOLDER = "/tmp/";

	private SenseMesurementTransferService listener;
	private List<Path> files;
	private JSch jsch = null;
	private Session session = null;
	private ChannelSftp channel = null;
	
	public SenseMesurementTansferJob(List<Path> files, SenseMesurementTransferService listener) {
		this.files = files;
		this.listener = listener;
		jsch = new JSch();
	}
	
	private void connect() {
		try {
			session = jsch.getSession(REMOTE_USER, REMOTE_HOST, REMOTE_PORT);
			session.setPassword(REMOTE_PASS);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(CONNECT_TO);
		
		} catch (JSchException e) {
			LOG.log(Level.SEVERE, String.format("Unable to establish remote connection to: %s", REMOTE_HOST), e);
		}
	}
	
	private void connectChannel() {
		if(isConnected()) {
			try{
				channel = (ChannelSftp)session.openChannel("sftp");
				channel.connect(CONNECT_TO);
			} catch(JSchException e) {
				LOG.log(Level.SEVERE, String.format("Unable to open SFTP channel to port: %d", REMOTE_PORT), e);
			}
		}
	}
	
	private boolean isChannelConnected() {
		return (channel != null && channel.isConnected());
	}
	
	private void disconnectChannel() {
		if(isChannelConnected()) {
			channel.disconnect();
		} else {
			LOG.log(Level.WARNING, "Channell already disconnected");
		}
	}
	
	private void disconnect() {
		if(isConnected()) {
			session.disconnect();
		} else {
			LOG.log(Level.WARNING, "Already disconnected");
		}
	}
	
	private boolean isConnected() {
		return (session != null && session.isConnected());
	}
	
	@Override
	public void run() {
		
		try{
			connect();
			if(!isConnected()) {
				return;
			}
			connectChannel();
			
			if(!isChannelConnected()) {
				return;
			}
			
			for(Path file : files) {
				LOG.info(String.format("About to transfer: %s", file.getFileName()));
				try(BufferedOutputStream out = new BufferedOutputStream(channel.put(REMOTE_FOLDER + file.getFileName()));
						BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {

					int byteRead;
					while( (byteRead = in.read()) != -1 ) {
						out.write(byteRead);
					}
					
					Files.delete(file);
					listener.removeFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SftpException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if(isChannelConnected()) {
				disconnectChannel();
			}
			if(isConnected()) {
				disconnect();
			}
		}
	}
}
