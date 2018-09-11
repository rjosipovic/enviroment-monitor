package home.enviroment.job;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.enviroment.sense.SenseMesurement;

public class PersistSenseMesurementJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(PersistSenseMesurementJob.class.getName());
	
	private SenseMesurement[] mesurements;
	private String storageFolder;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-hhmm");
	
	public PersistSenseMesurementJob(SenseMesurement[] mesurements, String folder) {
		this.mesurements = mesurements;
		this.storageFolder = folder;
	}
	
	private String getFileName() {
		String dateFormated = sdf.format(new Date());
		return String.format("%s%s-sense-mesurement.txt", storageFolder, dateFormated);		
	}
	
	@Override
	public void run() {
		LOG.log(Level.FINE, "About to persist mesurements");
		try(ObjectOutputStream out =  new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFileName())))) {
			out.writeObject(mesurements);
			out.flush();
			out.close();
		}catch(IOException ex) {
			LOG.log(Level.SEVERE, "Unable to persist mesurements to a file.", ex);
		}		
	}
}
