package home.enviroment.monitor;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OsMemoryMonitor extends AbstractMonitor {
	
	private static final String COMMAND = "free -m";
	
	public OsMemoryMonitor() {
		super(COMMAND);
	}
	
	@Override
	protected Properties getProperties(List<String> responseLines) {
		Properties usages = new Properties();
		for(int i=1; i<responseLines.size(); i++) {
			String[] lineParts = responseLines.get(i).split("\\s+");
			if(i == 1) {
				usages.put("used", lineParts[2]);
				usages.put("free", lineParts[3]);
			} else if(i == 2) {
				usages.put("buffers-used", lineParts[2]);
				usages.put("buffers-free", lineParts[3]);
			} else if(i == 3) {
				usages.put("swap-used", lineParts[2]);
				usages.put("swap-free", lineParts[3]);
			}
		}
		return usages;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Properties props = exec.submit(new OsMemoryMonitor()).get();
		System.out.println(props);
		exec.shutdown();
	}

}
