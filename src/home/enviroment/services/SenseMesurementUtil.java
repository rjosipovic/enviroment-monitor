package home.enviroment.services;

import home.enviroment.config.Prop;
import home.enviroment.job.RetrieveSenseMesurementJob;
import home.enviroment.sense.MesurementType;
import home.enviroment.sense.SenseMesurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class SenseMesurementUtil {
	
	private static ConfigurationService configService = ConfigurationService.getInstance();
	
	public static List<RetrieveSenseMesurementJob> getSenseMesurementJobs() {
		List<RetrieveSenseMesurementJob> jobs = new ArrayList<RetrieveSenseMesurementJob>();
		for(MesurementType type : MesurementType.values()) {
			RetrieveSenseMesurementJob job = new RetrieveSenseMesurementJob(type);
			jobs.add(job);
		}
		return jobs;		
	}
	
	public static String getMesurementScriptNameFromType(MesurementType type) {
		
		switch (type) {
		case TEMPERATURE:
			return configService.getProperty(Prop.SENSE_MESUREMENT_TEMPERATURE_SCRIPT);
		case PRESSURE:
			return configService.getProperty(Prop.SENSE_MESUREMENT_PRESSURE_SCRIPT);
		case HUMIDITY:
			return configService.getProperty(Prop.SENSE_MESUREMENT_HUMIDITY_SCRIPT);
		default:
			return null;
		}		
	}
}
