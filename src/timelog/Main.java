package timelog;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Main extends JFrame {
	private final String newline = System.getProperty("line.separator");
	private final String configFilename = "config.txt";
	private final Properties config = new Properties();
	private final int intervalInSeconds;
	private final String projectsFilename, logFilename;
	private final Color defaultColor, activeColor, semiActiveColor;
	private final boolean minimise;
	private final Map<String, String> projects;
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private enum State {STOPPED, RUNNING, AUTOMATIC}

	private State state = State.STOPPED;
	private Timer timer;
	private long startTime = 0;
	private String runningProject = null;
	private String runningSubProject = null;
	private JButton runningButton = null;

	private Main() throws IOException {
		// loading settings
		config.load(new FileInputStream(configFilename));
		setTitle(get("title", "Time Log"));
		setLocation(get("locX", 400), get("locY", 400));
		intervalInSeconds = get("intervalInSeconds", 3600);
		defaultColor = get("defaultColor", Color.GREEN);
		activeColor = get("activeColor", Color.RED);
		semiActiveColor = get("semiActiveColor", Color.CYAN);
		minimise = get("behaviour", "minimise").equals("minimise");
		logFilename = get("logFilename", "log.txt");
		// loading projects + GUI
		getContentPane().setLayout(new GridLayout(0, 1));
		projectsFilename = get("projectsFilename", "projects.txt");
		projects = loadProjectNames();
		for (String project : projects.keySet())
			addButton(project, projects.get(project));
		addStopButton();
		pack();
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				try {stopRecording();}
				catch (IOException ex) {ex.printStackTrace();}
				System.exit(0);
			}
		});
		timer = createTimer();
		setVisible(true);
	}

	private String get(String key, String defaultValue) {
		String v = config.getProperty(key);
		return v == null ? defaultValue : v;
	}

	private int get(String key, int defaultValue) {
		String v = config.getProperty(key);
		return v == null ? defaultValue : Integer.parseInt(v);
	}

	private Color get(String key, Color defaultValue) {
		String v = config.getProperty(key);
		if (v == null) return defaultValue;
		String[] rgb = v.split(",");
		return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
	}

	private Map<String, String> loadProjectNames() throws IOException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(projectsFilename));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			int hash = line.indexOf('#');
			if (hash != -1)
				line = line.substring(0, hash).trim();
			if (line.isEmpty()) continue;
			int colon = line.indexOf(':');
			if (colon == -1)
				result.put(line, null);
			else {
				String project = line.substring(0, colon);
				result.put(project, null);
				for (String subProject : line.substring(colon + 1).trim().split(","))
					if (!subProject.trim().isEmpty())
						result.put(subProject.trim(), project);
			}
		}
		br.close();
		return result;
	}

	private void addButton(String projectWTT, String superProjectWTT) {
		// caption
		final String project = removeTooltip(projectWTT);
		final String superProject = removeTooltip(superProjectWTT);
		String caption = (superProject == null ? "" : "> ") + project;
		final JButton b = new JButton(caption);
		// tooltip preprocessing
		String tooltip = extractTooltip(projectWTT);
		if (tooltip != null)
			b.setToolTipText(tooltip);
		// style
		b.setBackground(defaultColor);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		// action
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String mainProject = superProject == null ? project : superProject;
					String subProject = superProject == null ? "" : project;
					if (state == State.AUTOMATIC) state = State.RUNNING;
					startRecording(b, mainProject, subProject);
					if (minimise) setExtendedState(ICONIFIED);
					else setVisible(false);
					timer.restart();
				} catch (IOException ex) {showProblem(ex);}
			}
		});
		add(b);
	}

	private String removeTooltip(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		if (left == -1) return s;
		return s.substring(0, left).trim();
	}

	private String extractTooltip(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		int right = s.lastIndexOf('}');
		if (left == -1 || right == -1) return null;
		return s.substring(left + 1, right).trim();
	}

	private void addStopButton() {
		JButton b = new JButton("STOP");
		b.setBackground(Color.BLACK);
		b.setForeground(Color.GRAY);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {stopRecording();}
				catch (IOException ex) {showProblem(ex);}
			}
		});
		add(b);
	}

	private Timer createTimer() {
		Timer t = new Timer(intervalInSeconds * 1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (state == State.AUTOMATIC) {
						stopRecording();
					} else if (state == State.RUNNING) {
						stopRecording();
						startRecording(runningButton, runningProject, runningSubProject);
						state = State.AUTOMATIC;
						if (runningButton != null)
							runningButton.setBackground(semiActiveColor);
					}
				} catch (IOException ex) {showProblem(ex);}
				if (minimise) setExtendedState(NORMAL);
				else setVisible(true);
			}
		});
		return t;
	}

	private void startRecording(JButton b, String project, String subProject) throws IOException {
		if (state != State.STOPPED) stopRecording();
		startTime = System.currentTimeMillis();
		runningProject = project;
		runningSubProject = subProject;
		runningButton = b;
		runningButton.setBackground(activeColor);
		state = State.RUNNING;
	}

	private void stopRecording() throws IOException {
		switch (state) {
			case STOPPED: return;
			case AUTOMATIC:
				timer.stop();
				break;
			case RUNNING:
				long endTime = System.currentTimeMillis();
				writeLogEntry(runningProject, runningSubProject, startTime, endTime);
		}
		runningButton.setBackground(defaultColor);
		state = State.STOPPED;
	}

	private void writeLogEntry(String project, String subProject, long startTime, long endTime) throws IOException {
		String startTimeS = df.format(new Date(startTime));
		String endTimeS = df.format(new Date(endTime));
		String entry = runningProject + ", " + runningSubProject + ", " + startTimeS + ", " + endTimeS;
		// System.err.println(entry);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFilename, true)));
		out.write(entry + newline);
		out.close();
	}

	private void showProblem(Exception e) {
		JOptionPane.showMessageDialog(this, "A problem has occurred: " + e.getMessage());
	}

	public static void main(String[] args) throws IOException {
		new Main();
	}
}
