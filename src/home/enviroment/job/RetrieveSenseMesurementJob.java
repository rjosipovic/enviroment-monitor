package home.enviroment.job;

import home.enviroment.sense.SenseMesurement;
import home.enviroment.services.SensePersistanceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveSenseMesurementJob implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(RetrieveSenseMesurementJob.class.getCanonicalName());
	
	private String[] commands;
	
	private Pattern temperaturePattern;
	private Pattern humidityPattern;
	private Pattern pressurePattern;
	
	private SensePersistanceService persistenceService;
	
	public RetrieveSenseMesurementJob(String senseHatScript, String tempPattern, String humidityPattern, String pressurePattern) {
		commands = new String[]{"python", senseHatScript};
		this.temperaturePattern = Pattern.compile(tempPattern);
		this.humidityPattern = Pattern.compile(humidityPattern);
		this.pressurePattern = Pattern.compile(pressurePattern);
		persistenceService = SensePersistanceService.getInstance();
	}
	
	@Override
	public void run() {
		List<String> lines = readLines();
		
		if(lines != null && !lines.isEmpty()) {
			SenseMesurement mesurement = parseLines(lines);
			persistenceService.addMesurement(mesurement);
		}
	}
	
	private List<String> readLines() {
		ProcessBuilder pb = new ProcessBuilder(commands);
		Process p = null;

		try {
			LOG.log(Level.FINE, String.format("About to execute command: %s", Arrays.toString(commands)));
			p = pb.start();
			int responseCode = p.waitFor();
			if(responseCode == 0) {
				LOG.log(Level.FINE, "Command executed successfully");
				return readLinesFromStream(p.getInputStream());
			} else {
				List<String> error = readLinesFromStream(p.getErrorStream());
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
	
	private List<String> readLinesFromStream(InputStream in) throws IOException {
		List<String> lines = new ArrayList<>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while( (line = reader.readLine()) != null ) {
			lines.add(line);						
		}
		return lines;		
	}
	
	private SenseMesurement parseLines(List<String> lines) {
		float temperature = 0.0f;
		float humidity = 0.0f;
		float pressure = 0.0f;
		for(String line : lines) {
			Matcher temperatureMatcher = temperaturePattern.matcher(line);
			Matcher humidityMatcher = humidityPattern.matcher(line);
			Matcher pressureMatcher = pressurePattern.matcher(line);
			if(temperatureMatcher.matches()) {
				temperature = Float.parseFloat(temperatureMatcher.group(2));
			} else if(humidityMatcher.matches()) {
				humidity = Float.parseFloat(humidityMatcher.group(2));
			} else if(pressureMatcher.matches()) {
				pressure = Float.parseFloat(pressureMatcher.group(2));
			}
		}
		return new SenseMesurement(new Date(), humidity, temperature, pressure);
	}
}
