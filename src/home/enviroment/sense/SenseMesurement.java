package home.enviroment.sense;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SenseMesurement implements Serializable {

	private static final long serialVersionUID = 20180901L;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	private Date mesureTime;
	private float humidity;
	private float temperature;
	private float pressure;
	
	public SenseMesurement(Date date, float humidity, float temperature, float pressure) {
		this.mesureTime = date;
		this.pressure = pressure;
		this.humidity = humidity;
		this.temperature = temperature;
	}
	
	@Override
	public String toString() {
		String formatedMesureTime = sdf.format(mesureTime);
		return String.format("MesureTime: %s Temperature: %.2f Humidity: %.2f Pressure: %.2f",formatedMesureTime, temperature, humidity, pressure);
	}
}
