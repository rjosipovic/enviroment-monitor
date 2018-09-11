package home.enviroment.monitor;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OsDiskMonitor extends AbstractMonitor {

	private static final String COMMAND = "df -h";
	
	public OsDiskMonitor() {
		super(COMMAND);
	}

	@Override
	protected Properties getProperties(List<String> responseLines) {
		Properties usages = new Properties();
		for(int i=1; i<responseLines.size(); i++) {
			String[] lineParts = responseLines.get(i).split("\\s+");
			usages.put(lineParts[lineParts.length-1], lineParts[lineParts.length-2]);
		}
		return usages;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Properties props = exec.submit(new OsDiskMonitor()).get();
		System.out.println(props);
		exec.shutdown();
	}
}
