package home.enviroment.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public abstract class AbstractMonitor implements Callable<Properties> {
	
	private static final Logger LOG = Logger.getLogger(AbstractMonitor.class.getName());
	
	private List<String> commands = new ArrayList<>();
	
	private ProcessBuilder procesBuilder;
	
	public AbstractMonitor(String command) {
		commands.add("/bin/bash");
		commands.add("-c");
		commands.add(command);
		procesBuilder = new ProcessBuilder(commands);
	}
	
	private List<String> getResponseLines(BufferedReader reader) throws IOException {
		List<String> responseLines = new ArrayList<>();
		String line;
		while((line = reader.readLine()) != null ) {
			responseLines.add(line);
		}
		return responseLines;
	}
	
	protected abstract Properties getProperties(List<String> responseLines);
	
	@Override
	public Properties call() {
		Properties diskUsages = null;
		LOG.info(String.format("Executing command %s", commands));
		
		try {
			Process process = procesBuilder.start();
			BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));			
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			int responseCode = process.waitFor();
			
			if(responseCode == 0) {
				LOG.info(String.format("Command %s executed successfully", commands));
				return getProperties(getResponseLines(outputReader));
			} else {
				LOG.severe(String.format("Error [%d] in executing commands %s, error message: [%s]", responseCode, commands, getResponseLines(errorReader)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return diskUsages;
	}
}
