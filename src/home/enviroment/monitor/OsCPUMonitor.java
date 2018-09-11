package home.enviroment.monitor;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OsCPUMonitor extends AbstractMonitor {
	
	private static final String COMMAND = "vmstat";
	
	public OsCPUMonitor() {
		super(COMMAND);
	}
	
	@Override
	protected Properties getProperties(List<String> responseLines) {
		Properties usages = new Properties();
		String[] lineParts = responseLines.get(2).split("\\s+");
		usages.put("user", lineParts[lineParts.length - 5]);
		usages.put("system", lineParts[lineParts.length - 4]);
		usages.put("idle", lineParts[lineParts.length - 3]);
		usages.put("waiting", lineParts[lineParts.length - 2]);
		usages.put("stolen", lineParts[lineParts.length - 1]);
		return usages;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Properties props = exec.submit(new OsCPUMonitor()).get();
		System.out.println(props);
		exec.shutdown();
	}
}
