package home.enviroment.services;

import home.enviroment.config.Prop;
import home.enviroment.job.PersistSenseMesurementJob;
import home.enviroment.sense.MesurementType;
import home.enviroment.sense.SenseMesurement;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractScheduledService;

public class SensePersistanceService extends AbstractScheduledService implements FileCreatedListener{
	
	private static final Logger LOG = Logger.getLogger(SensePersistanceService.class.getName());
	
	private static SensePersistanceService instance = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
	
	public static synchronized SensePersistanceService getInstance() {
		if(instance == null) {
			instance = new SensePersistanceService();
		}
		return instance;
	}
	
	private Map<MesurementType, LinkedBlockingQueue<SenseMesurement>> queues = new HashMap<MesurementType, LinkedBlockingQueue<SenseMesurement>>();
	private ExecutorService exec;
	private ConfigurationService configService = ConfigurationService.getInstance();
	private SenseMesurementTransferService transferService = SenseMesurementTransferService.getInstance();
	private boolean acceptingMesurements = false;
	
	public SensePersistanceService() {
		for(MesurementType type : MesurementType.values()) {
			queues.put(type, new LinkedBlockingQueue<SenseMesurement>());
		}
		exec = Executors.newSingleThreadExecutor();
	}
	
	@Override
	protected void startUp() throws Exception {
		super.startUp();
		LOG.info("Starting SensePersistanceService");
		LOG.info("Accepting mesurements");
		acceptingMesurements = true;
	}

	private void checkMesurementQueue(LinkedBlockingQueue<SenseMesurement> queue, MesurementType type) {
		int minFileRecordCount = Integer.parseInt(configService.getProperty(Prop.SENSE_MESUREMENT_MIN_FILE_RECORDS));

		if(queue.size() < minFileRecordCount) {
			LOG.log(Level.INFO, String.format("Not persisting, number of mesurements[%s] < %d in [%s] queue", queue.size(), minFileRecordCount, type.name()));			
		} else {
			LOG.log(Level.INFO, String.format("Persisting mesurements[%s]", queue.size()));
			persistMesurements(queue, type);
		}
		
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		
		for(MesurementType type : queues.keySet()) {
			LinkedBlockingQueue<SenseMesurement> queue = queues.get(type);
			checkMesurementQueue(queue, type);
		}
	}
	
	private void persistMesurements(LinkedBlockingQueue<SenseMesurement> queue, MesurementType type) {
		List<SenseMesurement> mesurements = new ArrayList<>();
		while(queue.peek() != null) {
			mesurements.add(queue.poll());
		}
		String fileName = getFileName(type);
		PersistSenseMesurementJob job = new PersistSenseMesurementJob(mesurements, fileName);
		job.addFileCreatedListener(this);
		exec.execute(job);
	}
	
	private String getFileName(MesurementType type) {
		String dateFormated = sdf.format(new Date());
		String storageFolder = configService.getProperty(Prop.SENSE_MESUREMENT_PERSIST_FOLDER);
		return String.format("%s%s-mesurement_%s.txt", storageFolder, type.getFileName(), dateFormated);		
	}

	@Override
	protected Scheduler scheduler() {
		return AbstractScheduledService.Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
	}
	
	@Override
	public void onFileCreated(Path file) {
		transferService.transferFile(file);
	}

	@Override
	protected void shutDown() throws Exception {
		LOG.info("Stopping SensePersistanceService");
		LOG.info("No longer accepting mesurements");
		acceptingMesurements = false;

		for(MesurementType type : queues.keySet()) {
			LinkedBlockingQueue<SenseMesurement> queue = queues.get(type);
			if(!queue.isEmpty()) {
				LOG.info(String.format("Persisting mesurements before shutdown for [%s] queue", type.name()));
				persistMesurements(queue, type);
			}
		}
		
		exec.shutdown();
		if(exec.awaitTermination(30, TimeUnit.SECONDS)) {
			LOG.log(Level.INFO, "All persistance jobs finished");
		} else {
			LOG.log(Level.WARNING, "Persistance jobs NOT finished");
		}
		
		super.shutDown();
	}
	
	private void addMesuremntToQueue(LinkedBlockingQueue<SenseMesurement> queue, SenseMesurement mesurement) {
		if(queue.offer(mesurement) && acceptingMesurements){
			LOG.log(Level.FINE, String.format("Mesurement: %s put on queue", mesurement));
		} else {
			String msg = String.format("Unable to put messurement: %s, on queue", mesurement);
			LOG.log(Level.WARNING, msg);
			throw new RuntimeException(msg);			
		}		
	}
	
	public void addMesurement(SenseMesurement mesurement) {
		MesurementType type = mesurement.getType();
		addMesuremntToQueue(queues.get(type), mesurement);
	}
}
