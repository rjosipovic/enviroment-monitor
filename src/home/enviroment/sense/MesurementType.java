package home.enviroment.sense;

public enum MesurementType {
	
	TEMPERATURE("TEMP", "temperature"),
	HUMIDITY("HUM", "humidity"),
	PRESSURE("PRESS", "pressure");
	
	private String abbreviation;
	private String fileName;
	
	private MesurementType(String abbreviation, String fileName) {
		this.abbreviation = abbreviation;
		this.fileName = fileName;
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public static MesurementType fromAbbreviation(String abbreviation) {
		
		switch (abbreviation) {
		case "TEMP":
			return MesurementType.TEMPERATURE;
		case "HUM":
			return MesurementType.HUMIDITY;
		case "PRESS":
			return MesurementType.PRESSURE;
		default:
			throw new IllegalArgumentException();
		}		
	}
}
