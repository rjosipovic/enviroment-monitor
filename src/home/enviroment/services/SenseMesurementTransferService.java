package home.enviroment.services;

import home.enviroment.config.Prop;
import home.enviroment.job.SenseMesurementTransferJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SenseMesurementTransferService extends AbstractScheduledService implements FileTransferedListener{
	
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
	private ConfigurationService configurationService = null;
	private ExecutorService exec = null;

	@Override
	protected void startUp() throws Exception {
		LOG.info("Starting SenseMesurementTransferService");
		exec = Executors.newSingleThreadExecutor();
		configurationService = ConfigurationService.getInstance();
		super.startUp();
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		LOG.info("About to transfer Sense Mesurement files to remote destination");
		executeFileTransfer();
	}
	
	private Map<String, String> getRemoteProps() {
		Map<String, String> remoteProps = new HashMap<>();
		remoteProps.put(SenseMesurementTransferJob.REMOTE_USER_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_USER));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_PASS_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_PASS));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_HOST_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_HOST));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_PORT_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_PORT));
		remoteProps.put(SenseMesurementTransferJob.CONNECT_TO_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_CONNECT_TO));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_FOLDER_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_FOLDER));
		return remoteProps;
	}
	
	private void executeFileTransfer() {
		if(!files.isEmpty()) {
			LOG.info(String.format("Transfering: %d files", files.size()));
			SenseMesurementTransferJob transferJob = new SenseMesurementTransferJob(new ArrayList<Path>(files), getRemoteProps());
			transferJob.addFileTransferedListener(this);
			exec.execute(transferJob);
		} else {
			LOG.info("There are no files to transfer");
		}
	}
	
	public synchronized void transferFile(Path file){
		this.files.add(file);
	}
	
	@Override
	public void onFileTransfered(String transferedFile) {
		Path file = Paths.get(transferedFile);
		if(files.contains(file)) {
			files.remove(file);
		}		
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SenseMesurementTransferService");
		//TODO not working as expected
		if(!files.isEmpty()) {
			LOG.info(String.format("There are still: [%d] files to transfer", files.size()));
			LOG.info(String.format("Transfering: %d files before shutdown", files.size()));
			executeFileTransfer();
		}
		while(!files.isEmpty()) {
			LOG.info(String.format("Waiting for files to be transfered. There are still: [%d] files to transfer", files.size()));
			sleep(2);
		}
		exec.shutdown();
		super.shutDown();
		LOG.info("SenseMesurementTransferService stopped");
	}
	
	private void sleep(int seconds) {
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}		
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 2, TimeUnit.MINUTES);
	}
}
