package rokclock;

import java.awt.Color;
import java.io.*;
import java.text.*;
import java.util.Properties;

/**
 * This class encapsulates all configuration settings, most of which can be
 * user-specified.
 */
class Config {
	/**
	 * The string definition of the date-format {@link #df}.
	 */
	static final String dfS = "dd/MM/yyyy HH:mm:ss";
	/**
	 * The date-format used for parsing and writing dates.
	 */
	static final DateFormat df = new SimpleDateFormat(dfS);
	/**
	 * The name of the configuration file.
	 */
	private final String configFilename = "config.txt";
	/**
	 * The name of the default (template) configuration file.
	 */
	private final String configFilenameDefault = configFilename + ".default";
	/**
	 * The properties stored within the configuration file.
	 */
	private final Properties properties = new Properties();
	/**
	 * The user's home directory. Used to replace tilde symbols in any
	 * user-specified path.
	 */
	private final String USER_HOME;

	/**
	 * The options for counting the user-interrupted semi-active time.
	 */
	enum AutoCountTowards {PREVIOUS, UNKNOWN, NOTHING}
	/**
	 * The options of window behaviour when the active time ends.
	 */
	enum Behaviour {MINIMISE, HIDE, SHOW}

	/**
	 * A constructor that reads the configuration file. If the configuration
	 * file does not exist, it copies the default configuration file into the
	 * standard place.
	 *
	 * @throws IOException
	 *             Thrown if either the copying or the reading of the
	 *             configuration file fails.
	 */
	Config() throws IOException {
		File configFile = new File(configFilename);
		if (!configFile.exists())
			Main.copyFile(new File(configFilenameDefault), configFile);
		properties.load(new FileInputStream(configFile));
		USER_HOME = System.getProperty("user.home");
	}

	/**
	 * Obtains the name of the projects' file. Default is "projects.txt".
	 *
	 * @return The projects' file name.
	 */
	String getProjectsFilename() {
		return processFilePath(get("projectsFilename", "projects.txt"));
	}

	/**
	 * Obtains the name of the default projects' file, i.e.
	 * "projects.txt.default".
	 *
	 * @return The name of the default projects' file.
	 */
	String getProjectsFilenameDefault() {
		return "projects.txt.default";
	}

	/**
	 * Obtains the name of the log file. Default is "log.txt". The
	 * user-specified path can start with a '~'.
	 *
	 * @return The name of the log file.
	 */
	String getLogFilename() {
		return processFilePath(get("logFilename", "log.txt"));
	}

	/**
	 * Obtains the interval in seconds of the active period. Default is 3600.
	 *
	 * @return The interval in seconds.
	 */
	int getIntervalInSeconds() {
		return get("intervalInSeconds", 3600);
	}

	/**
	 * Obtains the period in seconds of the semi-active period. Default is 3600.
	 *
	 * @return The wait period in seconds.
	 */
	int getWaitInSeconds() {
		return get("waitInSeconds", 3600);
	}

	/**
	 * Obtains the option for counting user-interrupted semi-active time.
	 *
	 * @return One of the {@link AutoCountTowards} options.
	 */
	AutoCountTowards getAutoCountTowards() {
		return get(AutoCountTowards.class, AutoCountTowards.PREVIOUS);
	}

	/**
	 * Obtains the option for window behaviour for when the active period ends.
	 *
	 * @return One of the {@link Behaviour} options.
	 */
	Behaviour getBehaviour() {
		return get(Behaviour.class, Behaviour.MINIMISE);
	}

	/**
	 * Obtains the boolean setting that determines whether semi-active period
	 * timeouts should be written into the log.
	 *
	 * @return Whether timeouts should be written.
	 */
	boolean getWriteTimeouts() {
		return get("writeTimeouts", false);
	}

	/**
	 * Obtains the window title. This is currently not user-configurable.
	 *
	 * @return The window title.
	 */
	String getTitle() {
		return "RokClock";
	}

	/**
	 * Obtains the starting X coordinate of the window.
	 *
	 * @return Starting X coordinate.
	 */
	int getLocX() {
		return get("locX", 400);
	}

	/**
	 * Obtains the starting Y coordinate of the window.
	 *
	 * @return Starting Y coordinate.
	 */
	int getLocY() {
		return get("locY", 400);
	}

	/**
	 * Obtains the starting width of the window.
	 *
	 * @return Starting width.
	 */
	int getWidth() {
		return get("width", 150);
	}

	/**
	 * Obtains the starting height of the window.
	 *
	 * @return Starting height.
	 */
	int getHeight() {
		return get("height", 400);
	}

	/**
	 * Obtains the colour used for non-active elements and the background.
	 *
	 * @return The default colour.
	 */
	Color getDefaultColor() {
		return get("defaultColor", Color.GREEN);
	}

	/**
	 * Obtains the colour used for active elements.
	 *
	 * @return The active colour.
	 */
	Color getActiveColor() {
		return get("activeColor", Color.RED);
	}

	/**
	 * Obtains the colour used for semi-active elements.
	 *
	 * @return The semi-active colour.
	 */
	Color getSemiActiveColor() {
		return get("semiActiveColor", Color.CYAN);
	}

	/**
	 * A generic method for obtaining values from the configuration file. The
	 * type of the value required is determined from the type of the default
	 * value provided.
	 *
	 * @param <T>
	 *            The inferred generic type of the value required.
	 * @param key
	 *            The name of the property.
	 * @param defaultValue
	 *            The default value.
	 * @return The resulting value.
	 */
	@SuppressWarnings("unchecked")
	private <T> T get(String key, T defaultValue) {
		String v = properties.getProperty(key);
		try {
			if (v == null)
				return defaultValue;
			if (defaultValue instanceof String)
				return (T) v;
			if (defaultValue instanceof Boolean)
				return (T) Boolean.valueOf(v);
			if (defaultValue instanceof Integer)
				return (T) Integer.valueOf(v);
			if (defaultValue instanceof Color) {
				String[] rgb = v.split(",");
				return (T) new Color(Integer.parseInt(rgb[0]), Integer
						.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
			}
		} catch (Exception e) {
			System.err.println("Couldn't parse "
					+ defaultValue.getClass().getSimpleName().toLowerCase()
					+ " specification for '" + key + "': " + v);
		}
		return defaultValue;
	}

	/**
	 * A generic method for obtaining a value from the configuration file where
	 * the value that corresponds to an enumeration member.
	 *
	 * @param <T>
	 *            The inferred enumeration type.
	 * @param c
	 *            The enumeration class of which value we want to obtain.
	 * @param defaultValue
	 *            The default value.
	 * @return The resulting value.
	 */
	private <T extends Enum<T>> T get(Class<T> c, T defaultValue) {
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

	/**
	 * Replaces all occurrences of '~' in the provided path with the current
	 * user's home path.
	 *
	 * @param path
	 *            The provided path.
	 * @return The resulting path.
	 */
	private String processFilePath(String path) {
		return path.replace("~", USER_HOME);
	}
}
