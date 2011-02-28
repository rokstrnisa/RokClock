package rokclock;

import java.awt.Color;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * This class encapsulates all configuration settings, most of which can be
 * user-specified.
 */
public class Config {
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
	private final String userConfigFilename = "config.txt";
	/**
	 * The name of the default (template) configuration file.
	 */
	private final String defaultConfigFilename = userConfigFilename + ".default";
	/**
	 * The properties stored within the default configuration file.
	 */
	private final Properties defaultProperties = new Properties();
	/**
	 * The properties stored within the user's configuration file.
	 */
	private final Properties userProperties = new Properties();
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
	public Config() throws IOException {
		File defaultConfigFile = new File(defaultConfigFilename);
		File userConfigFile = new File(userConfigFilename);
		if (!userConfigFile.exists())
			Main.copyFile(defaultConfigFile, userConfigFile);
		defaultProperties.load(new FileInputStream(defaultConfigFile));
		userProperties.load(new FileInputStream(userConfigFile));
		USER_HOME = System.getProperty("user.home");
	}

	/**
	 * Obtains the name of the projects' file. Default is "projects.txt".
	 *
	 * @return The projects' file name.
	 */
	String getProjectsFilename() {
		return processFilePath(get("projectsFilename", String.class));
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
		return processFilePath(get("logFilename", String.class));
	}

	boolean getUseHub() {
		return get("useHub", Boolean.class);
	}

	/**
	 * Obtains the absolute file address for the hub where to accumulate data
	 * from all users.
	 *
	 * @return The file address of the hub.
	 */
	public String getHub() {
		return get("hub", String.class);
	}

	/**
	 * Obtains the current user's username at the specified hub. This is used
	 * for logging purposes.
	 *
	 * @return The user's username.
	 */
	public String getUsernameOnHub() {
		return get("usernameOnHub", String.class);
	}

	/**
	 * Obtains the interval in seconds of the active period. Default is 3600.
	 *
	 * @return The interval in seconds.
	 */
	int getIntervalInSeconds() {
		return get("intervalInSeconds", Integer.class);
	}

	/**
	 * Obtains the period in seconds of the semi-active period. Default is 3600.
	 *
	 * @return The wait period in seconds.
	 */
	int getWaitInSeconds() {
		return get("waitInSeconds", Integer.class);
	}

	/**
	 * Obtains the option for counting user-interrupted semi-active time.
	 *
	 * @return One of the {@link AutoCountTowards} options.
	 */
	AutoCountTowards getAutoCountTowards() {
		return get(AutoCountTowards.class);
	}

	/**
	 * Obtains the option for window behaviour for when the active period ends.
	 *
	 * @return One of the {@link Behaviour} options.
	 */
	Behaviour getBehaviour() {
		return get(Behaviour.class);
	}

	/**
	 * Obtains the boolean setting that determines whether semi-active period
	 * timeouts should be written into the log.
	 *
	 * @return Whether timeouts should be written.
	 */
	boolean getWriteTimeouts() {
		return get("writeTimeouts", Boolean.class);
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
	 * Obtains user's team.
	 *
	 * @return User's team.
	 */
	String getTeam() {
		return get("team", String.class);
	}

	/**
	 * Sets the user's team setting to the specified value.
	 *
	 * @param team
	 *            The specified team value.
	 */
	void setTeam(String team) {
		userProperties.setProperty("team", team);
		try {
			userProperties.store(new FileOutputStream(userConfigFilename), null);
		} catch (IOException e) {
			System.err.println("Could not save the user configuration file.");
		}
	}

	/**
	 * Obtains the starting X coordinate of the window.
	 *
	 * @return Starting X coordinate.
	 */
	int getLocX() {
		return get("locX", Integer.class);
	}

	/**
	 * Obtains the starting Y coordinate of the window.
	 *
	 * @return Starting Y coordinate.
	 */
	int getLocY() {
		return get("locY", Integer.class);
	}

	/**
	 * Obtains the starting width of the window.
	 *
	 * @return Starting width.
	 */
	int getWidth() {
		return get("width", Integer.class);
	}

	/**
	 * Obtains the starting height of the window.
	 *
	 * @return Starting height.
	 */
	int getHeight() {
		return get("height", Integer.class);
	}

	/**
	 * Obtains the colour used for non-active elements and the background.
	 *
	 * @return The default colour.
	 */
	Color getDefaultColor() {
		return get("defaultColor", Color.class);
	}

	/**
	 * Obtains the colour used for active elements.
	 *
	 * @return The active colour.
	 */
	Color getActiveColor() {
		return get("activeColor", Color.class);
	}

	/**
	 * Obtains the colour used for semi-active elements.
	 *
	 * @return The semi-active colour.
	 */
	Color getSemiActiveColor() {
		return get("semiActiveColor", Color.class);
	}

	/**
	 * A generic method for obtaining values from either the user or the default
	 * configuration file. The type of the value required is determined from the
	 * class object provided.
	 *
	 * @param <T>
	 *            The inferred generic type of the value required.
	 * @param key
	 *            The name of the property.
	 * @param c
	 *            The class of which value we want to obtain.
	 * @return The resulting value.
	 */
	private <T> T get(String key, Class<T> c) {
		final T userValue = getPropertyFrom(userProperties, key, c);
		return userValue != null ? userValue : getPropertyFrom(defaultProperties, key, c);
	}

	/**
	 * A generic method for obtaining values from the specified configuration
	 * file. The type of the value required is determined from the class object
	 * provided.
	 *
	 * @param <T>
	 *            The inferred generic type of the value required.
	 * @param key
	 *            The name of the property.
	 * @param c
	 *            The class of which value we want to obtain.
	 * @return The resulting value.
	 */
	@SuppressWarnings("unchecked")
	private <T> T getPropertyFrom(Properties p, String key, Class<T> c) {
		String v = p.getProperty(key);
		if (v != null)
			try {
				if (c == String.class)
					return (T) v;
				if (c == Boolean.class)
					return (T) Boolean.valueOf(v);
				if (c == Integer.class)
					return (T) Integer.valueOf(v);
				if (c == Color.class) {
					String[] rgb = v.split(",");
					return (T) new Color(Integer.parseInt(rgb[0]), Integer
							.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
				}
			} catch (Exception e) {
				System.err.println("Couldn't parse "
						+ c.getSimpleName().toLowerCase()
						+ " specification for '" + key + "': " + v);
			}
			return null;
	}

	/**
	 * A generic method for obtaining a value from the user or the default
	 * configuration file where the value that corresponds to an enumeration
	 * member.
	 *
	 * @param <T>
	 *            The inferred enumeration type.
	 * @param c
	 *            The enumeration class of which value we want to obtain.
	 * @return The resulting value.
	 */
	private <T extends Enum<T>> T get(Class<T> c) {
		final T userValue = getPropertyFrom(userProperties,  c);
		return userValue != null ? userValue : getPropertyFrom(defaultProperties, c);
	}

	/**
	 * A generic method for obtaining a value from the specified properties file
	 * where the value that corresponds to an enumeration member.
	 *
	 * @param <T>
	 *            The inferred enumeration type.
	 * @param p
	 *            The properties file to obtain the value from.
	 * @param c
	 *            The enumeration class of which value we want to obtain.
	 * @return The resulting value.
	 */
	private <T extends Enum<T>> T getPropertyFrom(Properties p, Class<T> c) {
		String propertyName = c.getSimpleName();
		propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
		String value = getPropertyFrom(p, propertyName, String.class).toUpperCase();
		T result = null;
		try {result = Enum.valueOf(c, value);}
		catch (IllegalArgumentException e) {
			System.err.println("Could not recognise the specified option for '"
					+ propertyName + "': " + value);
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

	/**
	 * Fetches a list of pre-defined teams specified in the
	 * <code>settings/teams.txt</code> file at the hub's location.
	 *
	 * @return The list of pre-defined team.
	 * @throws IOException
	 *             Thrown if problems when reading the file.
	 */
	public List<String> fetchTeams() throws IOException {
		final List<String> teamList = new ArrayList<String>();
		File teamsFile = new File(getHub() + "/settings/teams.txt");
		BufferedReader br = new BufferedReader(new FileReader(teamsFile));
		String line = null;
		while ((line = br.readLine()) != null)
			teamList.add(line);
		return teamList;
	}
}
