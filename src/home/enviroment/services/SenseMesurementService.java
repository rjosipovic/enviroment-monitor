package home.enviroment.services;

import home.enviroment.job.RetrieveSenseMesurementJob;
import home.enviroment.sense.SenseMesurement;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SenseMesurementService extends AbstractScheduledService implements MesureTakenListener {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementService.class.getName());
	
	private static SenseMesurementService instance = null;
	
	public static SenseMesurementService getInstance() {
		if(instance == null) {
			instance = new SenseMesurementService();
		}
		return instance;
	}
	
	private ExecutorService exec;
	private SensePersistanceService persistanceService;
	
	@Override
	protected void startUp() throws Exception {
		LOG.info("Starting SenseMesurementService");
		super.startUp();
		exec = Executors.newSingleThreadExecutor();
		persistanceService = SensePersistanceService.getInstance();
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		List<RetrieveSenseMesurementJob> jobs = SenseMesurementUtil.getSenseMesurementJobs();
		for(RetrieveSenseMesurementJob job : jobs) {
			job.addMesureTakenListener(this);
			exec.execute(job);
		}
	}
	
	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
	}
	
	@Override
	public void onMesureTaken(SenseMesurement mesurement) {
		persistanceService.addMesurement(mesurement);		
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SenseMesurementService");
		exec.shutdown();
		super.shutDown();
	}
}
