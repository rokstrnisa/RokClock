package rokclock;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.FileChannel;

import javax.swing.*;

/**
 * This is the main class of the application. It contains the high-level GUI
 * layout, and some common, application-level functionality. Once ran, the
 * application shows the main user frame, which waits for user input.
 */
@SuppressWarnings("serial")
public class Main extends JFrame {
	/**
	 * The application-level copy of the configuration class.
	 */
	private final Config config = new Config();
	/**
	 * The application-level reference to the tree of user-specified projects.
	 */
	private final ProjectsTree projectsTree;

	/**
	 * The only constructor of the application, which sets up the cross-platform
	 * look-and-feel, performs the component layout, configures basic window
	 * functionality, and shows the window.
	 *
	 * @throws Exception
	 *             Thrown if cannot change the look-and-feel, or if the tree of
	 *             projects cannot be parsed.
	 */
	private Main() throws Exception {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		// basic settings
		setTitle(config.getTitle());
		setLocation(config.getLocX(), config.getLocY());
		setSize(config.getWidth(), config.getHeight());
		setBackground(Color.BLACK);
		// loading projects + GUI
		projectsTree = new ProjectsTree(this, config);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		getContentPane().setLayout(layout);
		constraints.gridx = constraints.gridy = 0;
		constraints.weightx = 0.5;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(projectsTree);
		layout.setConstraints(scrollPane, constraints);
		getContentPane().add(scrollPane);
		constraints.weighty = 0;
		JButton stopButton = createStopButton();
		JButton reviewAndSendButton = createReviewAndSendButton();
		constraints.gridy = 1;
		layout.setConstraints(stopButton, constraints);
		constraints.gridy = 2;
		layout.setConstraints(reviewAndSendButton, constraints);
		getContentPane().add(stopButton);
		getContentPane().add(reviewAndSendButton);
		// pack();
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				try {projectsTree.stopRecording();}
				catch (Exception ex) {ex.printStackTrace();}
				System.exit(0);
			}
		});
		setVisible(true);
	}

	/**
	 * Creates a simple button, which stops the recording when pressed.
	 *
	 * @return The stop button.
	 */
	private JButton createStopButton() {
		JButton b = new JButton("STOP");
		b.setBackground(Color.BLACK);
		b.setForeground(Color.GRAY);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopRecording();
			}
		});
		return b;
	}

	/**
	 * Creates a simple button, which pops up a child window for reviewing and
	 * saving a log summary for a particular period.
	 *
	 * @return The "Review and Save" button.
	 */
	private JButton createReviewAndSendButton() {
		JButton b = new JButton("REVIEW & SAVE");
		b.setBackground(Color.BLACK);
		b.setForeground(Color.GRAY);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopRecording();
				new ReviewDialog(Main.this, config);
			}
		});
		return b;
	}

	/**
	 * If the timer is running, this method stops the recording and creates a
	 * log entry according to the settings. Otherwise, it does nothing.
	 */
	private void stopRecording() {
		try {projectsTree.stopRecording();}
		catch (Exception ex) {projectsTree.displayProblem(ex);}
	}

	/**
	 * Gets the user-specified tree of projects.
	 *
	 * @return The tree of projects.
	 */
	ProjectsTree getProjectsTree() {
		return projectsTree;
	}

	/**
	 * A helper, platform-independent function to copy a file.
	 *
	 * @param sourceFile
	 *            The file to copy.
	 * @param destFile
	 *            The destination.
	 * @throws IOException
	 *             Thrown if the copying failed.
	 */
	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists())
			destFile.createNewFile();
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null)
				source.close();
			if (destination != null)
				destination.close();
		}
	}

	/**
	 * The entry method, which creates a new instance of the application. It
	 * ignores any command-line arguments.
	 *
	 * @param args
	 *            Any command-line arguments.
	 * @throws Exception
	 *             Thrown if creation of the new instance throws an exception.
	 */
	public static void main(String[] args) throws Exception {
		new Main();
	}
}
