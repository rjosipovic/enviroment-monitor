package home.enviroment.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import home.enviroment.services.FileTransferedListener;

public class SenseMesurementTransferJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementTransferJob.class.getName());
	
	public static final String REMOTE_USER_PROP = "user";
	public static final String REMOTE_PASS_PROP = "pass";
	public static final String REMOTE_HOST_PROP = "host";
	public static final String REMOTE_PORT_PROP = "port";
	public static final String CONNECT_TO_PROP = "timeout";
	public static final String REMOTE_FOLDER_PROP = "folder";
	
	private Collection<FileTransferedListener> listeners = new LinkedList<>();
	private List<Path> files;
	private JSch jsch = null;
	private Session session = null;
	private ChannelSftp channel = null;
	
	private String remoteUser;
	private String remotePass;
	private String remoteHost;
	private int remotePort;
	private int connectTimeout;
	private String remoteFolder;
	
	public SenseMesurementTransferJob(List<Path> files, Map<String, String> remoteProperties) {
		this.files = files;
		jsch = new JSch();
		if(!remoteProperties.containsKey(REMOTE_USER_PROP) || 
				!remoteProperties.containsKey(REMOTE_PASS_PROP) ||
				!remoteProperties.containsKey(REMOTE_HOST_PROP) ||
				!remoteProperties.containsKey(REMOTE_PORT_PROP) || 
				!remoteProperties.containsKey(CONNECT_TO_PROP) || 
				!remoteProperties.containsKey(REMOTE_FOLDER_PROP)) {
			throw new RuntimeException(String.format("Unable to construct %s, missing remote property", SenseMesurementTransferJob.class.getName()));
		}
		remoteUser = remoteProperties.get(REMOTE_USER_PROP);
		remotePass = remoteProperties.get(REMOTE_PASS_PROP);
		remoteHost = remoteProperties.get(REMOTE_HOST_PROP);
		remotePort = Integer.parseInt(remoteProperties.get(REMOTE_PORT_PROP));
		connectTimeout = Integer.parseInt(remoteProperties.get(CONNECT_TO_PROP));
		remoteFolder = remoteProperties.get(REMOTE_FOLDER_PROP);
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
				LOG.info(String.format("About to transfer: %s", file.toString()));
				try(BufferedOutputStream out = new BufferedOutputStream(channel.put(remoteFolder + file.getFileName()));
						BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {

					int byteRead;
					while( (byteRead = in.read()) != -1 ) {
						out.write(byteRead);
					}
					notifyListeners(file.toString());
					LOG.info(String.format("File %s transfered", file.toString()));
					Files.delete(file);
				} catch (IOException | SftpException e) {
					LOG.log(Level.SEVERE, "Error in transfering files to remote host.", e);
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
	
	public void addFileTransferedListener(FileTransferedListener listener) {
		this.listeners.add(listener);
	}
	
	private void connect() {
		try {
			session = jsch.getSession(remoteUser, remoteHost, remotePort);
			session.setPassword(remotePass);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(connectTimeout);
		
		} catch (JSchException e) {
			LOG.log(Level.SEVERE, String.format("Unable to establish remote connection to: %s", remoteHost), e);
		}
	}
	
	private void connectChannel() {
		if(isConnected()) {
			try{
				channel = (ChannelSftp)session.openChannel("sftp");
				channel.connect(connectTimeout);
			} catch(JSchException e) {
				LOG.log(Level.SEVERE, String.format("Unable to open SFTP channel to port: %d", remotePort), e);
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
	
	private void notifyListeners(String fileName) {
		for(FileTransferedListener listener : listeners) {
			listener.onFileTransfered(fileName);
		}
	}	
}
