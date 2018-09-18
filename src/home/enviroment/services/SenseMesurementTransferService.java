package home.enviroment.services;

import home.enviroment.job.SenseMesurementTansferJob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.nio.file.Path;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SenseMesurementTransferService extends AbstractScheduledService {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementTransferService.class.getName());
	
	private static SenseMesurementTransferService instance = null;
	public static synchronized SenseMesurementTransferService getInstance() {
		if(instance == null) {
			instance = new SenseMesurementTransferService();
		}
		return instance;
	}
	
	public SenseMesurementTransferService() {
	}
	
	private Set<Path> files = new HashSet<>();
	private ExecutorService exec = null;

	@Override
	protected void startUp() throws Exception {
		LOG.info("Starting SenseMesurementTransferService");
		exec = Executors.newSingleThreadExecutor();
		super.startUp();
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		LOG.info("About to transfer Sense Mesurement files to remote destination");
		transferFiles();
	}
	
	private void transferFiles() {
		if(!files.isEmpty()) {
			LOG.info(String.format("Transfering: %d files", files.size()));
			exec.execute(new SenseMesurementTansferJob(new ArrayList<Path>(files), this));
		} else {
			LOG.info("There are no files to transfer");
		}
	}
	
	public void addFile(Path file){
		this.files.add(file);
	}
	
	public void removeFile(Path file) {
		if(files.contains(file)) {
			files.remove(file);
		}
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SenseMesurementTransferService");
		//TODO not working as expected
		if(!files.isEmpty()) {
			LOG.info(String.format("There are stil: [%d] files to transfer", files.size()));
			LOG.info(String.format("Transfering: %d files before shutdown", files.size()));
			transferFiles();
		}
		exec.shutdown();
		super.shutDown();
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 2, TimeUnit.MINUTES);
	}
}
