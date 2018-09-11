package home.enviroment;

public interface EnviromentMonitorMBean {

	public String getName();
	public void start();
	public void stop();
	public void reload();
	public boolean isRunning();
}
