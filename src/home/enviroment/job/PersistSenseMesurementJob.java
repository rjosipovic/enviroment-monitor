package home.enviroment.job;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		LOG.log(Level.INFO, String.format("About to persist [%d] mesurements", mesurements.length));
		
		Path file = Paths.get(getFileName());
		try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
			for(int i=0; i<mesurements.length; i++) {
				writer.write(mesurements[i].toString());
				writer.write("\n");
			}
			writer.flush();
		}catch(IOException ex) {
			LOG.log(Level.SEVERE, "Unable to persist mesurements to a file.", ex);
		}
	}
}
