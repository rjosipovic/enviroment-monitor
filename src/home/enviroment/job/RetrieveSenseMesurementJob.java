package home.enviroment.job;

import home.enviroment.sense.MesurementType;
import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.MesureTakenListener;
import home.enviroment.services.SenseMesurementUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveSenseMesurementJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(RetrieveSenseMesurementJob.class.getCanonicalName());
	
	private String[] commands;
	private MesurementType type;
	
	private List<MesureTakenListener> listeners = new LinkedList<MesureTakenListener>();
	
	public RetrieveSenseMesurementJob(MesurementType type) {
		String script = SenseMesurementUtil.getMesurementScriptNameFromType(type);
		commands = new String[]{"/bin/bash", script};
		this.type = type;
	}
	
	public void addMesureTakenListener(MesureTakenListener listener) {
		listeners.add(listener);
	}
	
	private void notifyListeners(SenseMesurement mesurement) {
		for(MesureTakenListener listener : listeners) {
			listener.onMesureTaken(mesurement);
		}
	}
	
	@Override
	public void run() {
		String response = readLine();
		
		if(response != null) {
			SenseMesurement mesurement = getMesurementFromString(response);
			if(mesurement != null) {
				notifyListeners(mesurement);
			} else {
				LOG.log(Level.WARNING, "Mesurement not defined");
			}
		}
	}
	
	private String readLine() {
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = null;

		try {
			LOG.log(Level.FINE, String.format("About to execute command: %s", Arrays.toString(commands)));
			p = pb.start();
			int responseCode = p.waitFor();
			if(responseCode == 0) {
				LOG.log(Level.FINE, "Command executed successfully");
				return readLineFromStream(p.getInputStream());
			} else {
				String error = readLineFromStream(p.getErrorStream());
				LOG.log(Level.SEVERE, String.format("Error in executing command, code: %d, details: %s", responseCode, error));
			}
		} catch (IOException | InterruptedException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if(p != null) {
				p.destroy();
			}
		}
		return null;
	}
	
	private String readLineFromStream(InputStream in) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader.readLine();
	}
	
	private SenseMesurement getMesurementFromString(String line) {
		try {
			float value = Float.parseFloat(line);
			SenseMesurement mesurement = new SenseMesurement(new Date(), value, type);
			return mesurement;
		}catch(NumberFormatException ex) {
			LOG.log(Level.WARNING, String.format("Unable to parse to float value: %s of type [%s]", line, type.name()));
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
}
