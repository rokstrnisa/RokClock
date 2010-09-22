package timelog;

import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.Timer;

@SuppressWarnings("serial")
class Main extends JFrame {
	private static final Color defaultColor = Color.GREEN;
	private static final Color activeColor = Color.RED;
	private static final String projFilename = "projects.txt";
	private final Map<String, String> projects;
	
	private boolean running = false;
	private long startTime = 0;
	private String runningProject = null;
	private String runningSubProject = null;
	private JButton runningButton = null;

	private Main() throws IOException {
		setTitle("Time Log");
		setLocation(400, 400);
		getContentPane().setLayout(new GridLayout(0, 1));
		projects = loadProjectNames();
		for (String project : projects.keySet())
			addButton(project, projects.get(project));
		pack();
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stopRecording();
				System.exit(0);
			}
		});
		setVisible(true);
	}
	
	private Map<String, String> loadProjectNames() throws IOException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(projFilename));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			int colon = line.indexOf(':');
			if (colon == -1)
				result.put(line, null);
			else {
				String project = line.substring(0, colon);
				result.put(project, null);
				for (String subProject : line.substring(colon + 1).trim().split(","))
					result.put(subProject.trim(), project);
			}
		}
		br.close();
		return result;
	}

	private void startRecording(JButton b, String project, String subProject) {
		if (running) stopRecording();
		startTime = System.currentTimeMillis();
		runningProject = project;
		runningSubProject = subProject;
		runningButton = b;
		runningButton.setBackground(activeColor);
		running = true;
	}
	
	private void stopRecording() {
		if (!running) return;
		long endTime = System.currentTimeMillis();
		System.err.println(runningProject + ", " + runningSubProject + ", " + startTime + ", " + endTime);
		runningButton.setBackground(defaultColor);
		running = false;
	}
	
	private void addButton(final String project, final String superProject) {
		String caption = (superProject == null ? "" : "> ") + project;
		final JButton b = new JButton(caption);
		b.setBackground(defaultColor);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (b == runningButton) {
					stopRecording();
				} else {
					String mainProject = superProject == null ? project : superProject;
					String subProject = superProject == null ? "" : project;
					startRecording(b, mainProject, subProject);
					// setVisible(false); // if we don't want it in toolbar
					setExtendedState(ICONIFIED);
					waitThenPopup();
				}
			}
		});
		add(b);
	}
	
	private void waitThenPopup() {
		int delay = 5000;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// setVisible(true); // if we don't want it in toolbar
				setExtendedState(NORMAL);
			}
		};
		Timer t = new Timer(delay, taskPerformer);
		t.setRepeats(false);
		t.start();
	}
	
	public static void main(String[] args) throws IOException {
		new Main();
	}
}
