package timelog;

import java.awt.Color;
import java.io.*;
import java.util.Properties;

class Config {
	private final Properties properties = new Properties();

	Config(String configFilename) throws IOException {
		properties.load(new FileInputStream(configFilename));
	}
	
	String getProjectsFilename() {
		return get("projectsFilename", "projects.txt");
	}
	
	String getLogFilename() {
		return get("logFilename", "log.txt");
	}
	
	int getIntervalInSeconds() {
		return get("intervalInSeconds", 3600);
	}
	
	boolean getMinimise() {
		return get("behaviour", "minimise").equals("minimise");
	}
	
	String getTitle() {
		return get("title", "Time Log");
	}
	
	int getLocX() {
		return get("locX", 400);
	}
	
	int getLocY() {
		return get("locY", 400);
	}
	
	int getWidth() {
		return get("width", 150);
	}
	
	int getHeight() {
		return get("height", 400);
	}
	
	Color getDefaultColor() {
		return get("defaultColor", Color.GREEN);
	}
	
	Color getActiveColor() {
		return get("activeColor", Color.RED);
	}
	
	Color getSemiActiveColor() {
		return get("semiActiveColor", Color.CYAN);
	}
	
	private String get(String key, String defaultValue) {
		String v = properties.getProperty(key);
		return v == null ? defaultValue : v;
	}

	private int get(String key, int defaultValue) {
		String v = properties.getProperty(key);
		return v == null ? defaultValue : Integer.parseInt(v);
	}

	private Color get(String key, Color defaultValue) {
		String v = properties.getProperty(key);
		if (v == null) return defaultValue;
		String[] rgb = v.split(",");
		return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
	}
}
