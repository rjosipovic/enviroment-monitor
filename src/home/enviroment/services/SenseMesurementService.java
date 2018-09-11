package home.enviroment.services;

import home.enviroment.job.RetrieveSenseMesurementJob;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SenseMesurementService extends AbstractScheduledService {
	
	private static final Logger LOG = Logger.getLogger(SenseMesurementService.class.getName());
	
	private static SenseMesurementService instance = null;
	
	public static SenseMesurementService getInstance() {
		if(instance == null) {
			instance = new SenseMesurementService();
		}
		return instance;
	}
	
	private ExecutorService exec;
	private ConfigurationService confService;
	
	@Override
	protected void startUp() throws Exception {
		LOG.info("Starting SenseMesurementService");
		super.startUp();
		exec = Executors.newSingleThreadExecutor();
		confService = ConfigurationService.getInstance();
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		LOG.info("Executing SenseEnviroment iteration");
		RetrieveSenseMesurementJob senseJob = new RetrieveSenseMesurementJob(confService.getSenseHatScriptPath(), 
				confService.getTempPattern(),
				confService.getHumidityPattern(),
				confService.getPressurePattern());
		exec.execute(senseJob);
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.SECONDS);
	}
	
	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SenseMesurementService");
		exec.shutdown();
		super.shutDown();
	}
}
