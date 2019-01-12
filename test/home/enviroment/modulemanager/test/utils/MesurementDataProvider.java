package home.enviroment.modulemanager.test.utils;

import home.enviroment.sense.MesurementType;
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

	private final String REG_EXP = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\|([A-Z]+)\\|([0-9]+.[0-9]+)$";
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
					String dateTime = m.group(1);
					String t = m.group(2);
					String v = m.group(3);
					Date dt = null;
					try{
						dt = sdf.parse(dateTime);
					}catch(ParseException ex) {
						ex.printStackTrace();
						continue;
					}
					MesurementType type = MesurementType.fromAbbreviation(m.group(2));
					float value = Float.parseFloat(m.group(3));
					SenseMesurement mesurement = new SenseMesurement(dt, value, type);
					System.out.println(mesurement.toPersistString());
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
