package home.enviroment.modulemanager.test.utils;

import home.enviroment.sense.SenseMesurement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MesurementDataProvider {
	
	private static MesurementDataProvider instance = null;
	
	public synchronized static MesurementDataProvider getInstance() {
		if(instance == null) {
			instance = new MesurementDataProvider();
		}
		return instance;
	}

	private final String REG_EXP = "^(MesureTime: (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})) (Temperature: (\\d{2}.\\d{2})) (Humidity: (\\d{2}.\\d{2})) (Pressure: (\\d{3,4}.\\d{2}))$";
	private final Pattern pattern = Pattern.compile(REG_EXP);
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final String INPUT_FILE = "persist/input/raw.txt";
	private List<SenseMesurement> mesurements = new ArrayList<SenseMesurement>();
	private List<String> inputLines = null;
	
	
	public MesurementDataProvider() {
		Path raw = Paths.get(INPUT_FILE);
		try{
			inputLines = Files.readAllLines(raw);
			for(String line : inputLines) {
				Matcher m = pattern.matcher(line);
				if(m.find()) {
					String dateTime = m.group(2);
					Date dt = null;
					try{
						dt = sdf.parse(dateTime);
					}catch(ParseException ex) {
						ex.printStackTrace();
						continue;
					}
					String temperature = m.group(4);
					float t = Float.parseFloat(temperature.replace(",", "."));
					String humidity = m.group(6);
					float h = Float.parseFloat(humidity.replace(",", "."));
					String pressure = m.group(8);
					float p = Float.parseFloat(pressure.replace(",", "."));
					SenseMesurement mesurement = new SenseMesurement(dt, h, t, p);
					mesurements.add(mesurement);
				}
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public List<SenseMesurement> getMesurements() {
		if(!mesurements.isEmpty()) {
			return new ArrayList<SenseMesurement>(this.mesurements);			
		} else {
			return null;
		}
	}
	
	public List<String> getInputLines() {
		if(inputLines != null) {
			return new ArrayList<String>(this.inputLines);			
		} else {
			return null;
		}
	}
}
