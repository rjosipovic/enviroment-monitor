package home.enviroment.sense;

import java.io.Serializable;

public class SenseMesurement implements Serializable {

	private static final long serialVersionUID = 20180901L;
	
	private float humidity;
	private float temperature;
	private float pressure;
	
	public SenseMesurement(float humidity, float temperature, float pressure) {
		this.pressure = pressure;
		this.humidity = humidity;
		this.temperature = temperature;
	}
	
	@Override
	public String toString() {
		return String.format("Temperature: %.2f\nHumidity: %.2f\nPressure: %.2f", temperature, humidity, pressure);
	}
}
