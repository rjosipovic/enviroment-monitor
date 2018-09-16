package home.enviroment.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import home.enviroment.services.SenseMesurementTransferService;

public class SenseMesurementTansferJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementTansferJob.class.getName());
	
	private SenseMesurementTransferService listener;
	private List<Path> files;
	
	public SenseMesurementTansferJob(List<Path> files, SenseMesurementTransferService listener) {
		this.files = files;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		for(Path file : files) {
			LOG.info(String.format("About to transfer: %s", file.getFileName()));
			try {
				Files.delete(file);
				listener.removeFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
