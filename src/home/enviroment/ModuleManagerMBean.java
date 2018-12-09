package home.enviroment;

public interface ModuleManagerMBean {

	public String getName();
	public void start();
	public void stop();
	public boolean isRunning();
}
