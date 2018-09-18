package home.enviroment.job;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.SenseMesurementTransferService;

public class PersistSenseMesurementJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(PersistSenseMesurementJob.class.getName());
	
	private List<SenseMesurement> mesurements;
	private String storageFolder;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");
	private SenseMesurementTransferService transferService = null;
	
	public PersistSenseMesurementJob(List<SenseMesurement> mesurements, String folder) {
		this.mesurements = mesurements;
		this.storageFolder = folder;
		transferService = SenseMesurementTransferService.getInstance();
	}
	
	private String getFileName() {
		String dateFormated = sdf.format(new Date());
		return String.format("%ssense-mesurement_%s.txt", storageFolder, dateFormated);		
	}
	
	@Override
	public void run() {
		LOG.log(Level.INFO, String.format("About to persist [%d] mesurements", mesurements.size()));
		
		Path file = Paths.get(getFileName());
		try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
			for(SenseMesurement mesurement : mesurements) {
				writer.write(mesurement.toString());
				writer.write("\n");
				
			}
			writer.flush();
			transferService.addFile(file);
		}catch(IOException ex) {
			LOG.log(Level.SEVERE, "Unable to persist mesurements to a file.", ex);
		}
	}
}
