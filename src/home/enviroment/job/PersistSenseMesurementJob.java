package home.enviroment.job;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.FileCreatedListener;

public class PersistSenseMesurementJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(PersistSenseMesurementJob.class.getName());
	
	private List<SenseMesurement> mesurements;
	private String fileName;
	private List<FileCreatedListener> listeners = new LinkedList<FileCreatedListener>();
	
	public PersistSenseMesurementJob(List<SenseMesurement> mesurements, String fileName) {
		this.mesurements = mesurements;
		this.fileName = fileName;
	}
	
	public void addFileCreatedListener(FileCreatedListener listener) {
		this.listeners.add(listener);
	}
	
	private void notifListeners(Path file) {
		for(FileCreatedListener listener : listeners) {
			listener.onFileCreated(file);			
		}		
	}
	
	@Override
	public void run() {
		
		Path file = Paths.get(fileName);
		LOG.log(Level.INFO, String.format("About to persist [%d] mesurements to %s", mesurements.size(), fileName));

		try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
			for(SenseMesurement mesurement : mesurements) {
				writer.write(mesurement.toPersistString());
				writer.write("\n");				
			}
			writer.flush();
			notifListeners(file);
		} catch(IOException ex) {
			LOG.log(Level.SEVERE, "Unable to persist mesurements to a file.", ex);
		}
	}
}
