package timelog;

import java.awt.Color;
import java.io.*;
import java.text.*;
import java.util.Properties;

class Config {
	static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private final String configFilename = "config.txt";
	private final Properties properties = new Properties();
	
	enum AutoCountTowards {PREVIOUS, UNKNOWN, NOTHING}
	enum Behaviour {MINIMISE, HIDE, SHOW}

	Config() throws IOException {
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
	
	int getWaitInSeconds() {
		return get("waitInSeconds", 3600);
	}

	AutoCountTowards getAutoCountTowards() {
		return get(AutoCountTowards.class, AutoCountTowards.PREVIOUS);
	}

	Behaviour getBehaviour() {
		return get(Behaviour.class, Behaviour.MINIMISE);
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
		return new Color(Integer.parseInt(rgb[0]),
			Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
	}

	<T extends Enum<T>> T get(Class<T> c, T defaultValue) {
		String propertyName = c.getSimpleName();
		propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
		String defaultValueS = defaultValue.toString().toLowerCase();
		String value = get(propertyName, defaultValueS).toUpperCase();
		T result = defaultValue;
		try {result = Enum.valueOf(c, value);}
		catch (IllegalArgumentException e) {
			System.err.println("Could not recognise the specified option for '"
					+ propertyName + "': "
					+ properties.getProperty("behaviour"));
		}
		return result;
	}
}
