package home.enviroment.services;

import home.enviroment.job.PersistSenseMesurementJob;
import home.enviroment.sense.SenseMesurement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SensePersistanceService extends AbstractScheduledService {
	
	private static final Logger LOG = Logger.getLogger(SensePersistanceService.class.getName());
	
	private static SensePersistanceService instance = null;
	
	public static synchronized SensePersistanceService  getInstance() {
		if(instance == null) {
			instance = new SensePersistanceService();
		}
		return instance;
	}
	
	private LinkedBlockingQueue<SenseMesurement> queue;
	private ExecutorService exec;
	private ConfigurationService configService = ConfigurationService.getInstance();
	
	public SensePersistanceService() {
		queue = new LinkedBlockingQueue<>();
		exec = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void startUp() throws Exception {
		super.startUp();
		LOG.info("Starting SensePersistanceService");
	}

	@Override
	protected void runOneIteration() throws Exception {
		LOG.log(Level.INFO, "About to perist mesurements");
		if(queue.size() == 0) {
			LOG.log(Level.INFO, "There are no mesurements to persist");
		} else {
			persistMesurements();			
		}		
	}
	
	private void persistMesurements() {
		List<SenseMesurement> mesurements = new ArrayList<>();
		while(queue.peek() != null) {
			mesurements.add(queue.poll());
		}
		exec.execute(new PersistSenseMesurementJob(mesurements, configService.getSensePersistFolder()));
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
	}

	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SensePersistanceService");
		exec.shutdown();
		super.shutDown();
	}
	
	public void addMesurement(SenseMesurement mesurement) {
		if(queue.offer(mesurement)){
			LOG.log(Level.FINE, String.format("Mesurement: %s put on queue", mesurement));
		} else {
			LOG.log(Level.WARNING, String.format("Unable to put messurement: %s, on queue", mesurement));
		}		
	}
}
