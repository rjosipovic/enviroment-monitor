package home.enviroment.config;

public enum Prop {
	
	ENVIROMENT_SERVICE_CONFIG_FILE("enviroment-service-config-file", "/etc/enviromental-monitor/enviroment-monitor.conf"),
	ENVIROMENT_SERVICE_LOG_PROPS_FILE("enviroment-service-log-props-file", "/etc/enviroment-monitor/logging.properties"),
	ENVIROMENT_SERVICE_APPLICATION_NAME("enviroment-service-application-name", "EnviromentMonitor"),
	ENVIROMENT_SERVICE_APPLICATION_STATUS_TIMEOUT("enviroment-service-application-status-timeout", "5"),
	
	SENSE_MESUREMENT_TEMPERATURE_SCRIPT("sense-mesurement-temperature-script", "/home/sense/sense-hat/scripts/temperature.sh"),
	SENSE_MESUREMENT_PRESSURE_SCRIPT("sense-mesurement-pressure-script", "/home/sense/sense-hat/scripts/pressure.sh"),
	SENSE_MESUREMENT_HUMIDITY_SCRIPT("sense-mesurement-humidity-script", "/home/sense/sense-hat/scripts/humidity.sh"),
	SENSE_MESUREMENT_TEMP_PATTERN("sense_mesurement_temp_pattern", "^(temp_calibrated:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$"),
	SENSE_MESUREMENT_PRESSURE_PATTERN("sense_mesurement_pressure_pattern", "^(pressure:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$"),
	SENSE_MESUREMENT_HUMIDITY_PATTERN("sense_mesurement_humidity_pattern", "^(humidity:)([-+]?([0-9]*\\.[0-9]+|[0-9]+))$"),
	SENSE_MESUREMENT_PERSIST_FOLDER("sense_mesurement_persist_folder", "/tmp/"),
	SENSE_MESUREMENT_MIN_FILE_RECORDS("sense_mesurement_min_file_records", "1000"),
	
	MONITOR_SERVER_IP("monitor-server-ip", "localhost"),
	MONITOR_SERVER_PORT("monitor-server-port", "1234"),
	MONITOR_MAX_WORKERS("monitor-max-workers", "10"),
	MONITOR_SERVER_SOCKET_TO("monitor-server-socket-timeout", "2000"),
	MONITOR_STATUS_MSG("monitor-status-msg", "EnviromentMonitor is RUNNING"),
	
	REMOTE_TRANSFER_HOST("remote-transfer-host", "localhost"),
	REMOTE_TRANSFER_USER("remote-transfer-user", "roman"),
	REMOTE_TRANSFER_PASS("remote-transfer-pass", "roman"),
	REMOTE_TRANSFER_PORT("remote-transfer-port", "22"),
	REMOTE_TRANSFER_CONNECT_TO("remote-transfer-connect-to", "5000"),
	REMOTE_TRANSFER_FOLDER("remote-transfer-folder", "/tmp/"),
	
	JMX_SERVICE_URL("jmx-serice-url", "service:jmx:rmi:///jndi/rmi://:1099/jmxrmi");
	
	private String name;
	private String defaultvalue;
	
	private Prop(String name, String defaultValue) {
		this.name = name;
		this.defaultvalue = defaultValue;
	}
	
	public String getDefaultvalue() {
		return defaultvalue;
	}
	
	public String getName() {
		return name;
	}
}
