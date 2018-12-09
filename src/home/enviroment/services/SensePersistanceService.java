package home.enviroment.services;

import home.enviroment.config.Prop;
import home.enviroment.job.PersistSenseMesurementJob;
import home.enviroment.sense.SenseMesurement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SensePersistanceService extends AbstractScheduledService {
	
	private static final Logger LOG = Logger.getLogger(SensePersistanceService.class.getName());
	
	private static SensePersistanceService instance = null;
	
	public static synchronized SensePersistanceService getInstance() {
		if(instance == null) {
			instance = new SensePersistanceService();
		}
		return instance;
	}
	
	private LinkedBlockingQueue<SenseMesurement> queue;
	private ExecutorService exec;
	private ConfigurationService configService = ConfigurationService.getInstance();
	private Set<Future<Path>> files = new HashSet<>();
	private boolean acceptingMesurements = false;
	private int minFileRecordCount;
	private FileCreatedListener listener;
	
	public SensePersistanceService() {
		queue = new LinkedBlockingQueue<>();
		exec = Executors.newSingleThreadExecutor();
	}
	
	public void addFileFinishedListener(FileCreatedListener listener) {
		this.listener = listener;		
	}

	@Override
	protected void startUp() throws Exception {
		super.startUp();
		LOG.info("Starting SensePersistanceService");
		LOG.info("Accepting mesurements");
		minFileRecordCount = Integer.parseInt(configService.getProperty(Prop.SENSE_MESUREMENT_MIN_FILE_RECORDS));
		acceptingMesurements = true;
	}

	private void removeFinishedFiles() {
		Set<Future<Path>> toRemove = new HashSet<Future<Path>>();
		for(Future<Path> future : files) {
			if(future.isDone()) {
				try{
					Path file = future.get();
					if(listener != null) {
						listener.onFileCreated(file.toString());
					}
					toRemove.add(future);
				} catch(ExecutionException | InterruptedException ex) {
					LOG.log(Level.SEVERE, ex.getMessage(), ex);
				}
			}
		}
		files.removeAll(toRemove);
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		LOG.log(Level.INFO, "About to check finished files");
		removeFinishedFiles();

		if(queue.size() == 0) {
			LOG.log(Level.INFO, "There are no mesurements to persist");
		} else if(queue.size() < minFileRecordCount) {
			LOG.log(Level.INFO, String.format("Not persisting, number of mesurements[%s] < %d", queue.size(), minFileRecordCount));			
		} else {
			LOG.log(Level.INFO, String.format("Persisting mesurements[%s]", queue.size()));
			persistMesurements();
		}
	}
	
	private void persistMesurements() {
		List<SenseMesurement> mesurements = new ArrayList<>();
		while(queue.peek() != null) {
			mesurements.add(queue.poll());
		}
		this.files.add(exec.submit(new PersistSenseMesurementJob(mesurements, configService.getProperty(Prop.SENSE_MESUREMENT_PERSIST_FOLDER))));
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
	}

	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SensePersistanceService");
		LOG.info("No longer accepting mesurements");
		acceptingMesurements = false;

		if(!queue.isEmpty()) {
			LOG.info("Persisting mesurements before shutdown");
			persistMesurements();
		}
		exec.shutdown();
		
		while(!files.isEmpty()) {
			LOG.info("Waiting for files to be persisted");
			TimeUnit.SECONDS.sleep(1);
			removeFinishedFiles();
		}
		super.shutDown();
	}
	
	public void addMesurement(SenseMesurement mesurement) {
		if(queue.offer(mesurement) && acceptingMesurements){
			LOG.log(Level.FINE, String.format("Mesurement: %s put on queue", mesurement));
		} else {
			String msg = String.format("Unable to put messurement: %s, on queue", mesurement);
			LOG.log(Level.WARNING, msg);
			throw new RuntimeException(msg);			
		}		
	}
}
