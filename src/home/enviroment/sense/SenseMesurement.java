package home.enviroment.sense;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SenseMesurement implements Serializable {

	private static final long serialVersionUID = 20180901L;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	private Date mesurementTime;
	private float value;
	private MesurementType type;
	
	public SenseMesurement(Date mesurementTime, float value, MesurementType type) {
		this.mesurementTime = mesurementTime;
		this.value = value;
		this.type = type;
	}
	
	public Date getMesureTime() {
		return mesurementTime;
	}
	
	public float getValue() {
		return value;
	}
	
	public MesurementType getType() {
		return type;
	}
	
	public String toPersistString() {
		String formatedMesureTime = sdf.format(mesurementTime);
		return String.format("%s|%s|%.2f", formatedMesureTime, type.getAbbreviation(), value);
	}
	
	@Override
	public String toString() {
		String formatedMesureTime = sdf.format(mesurementTime);
		return String.format("Type: %s MesureTime: %s Value: %.2f", type.name(), formatedMesureTime, value);
	}
}
