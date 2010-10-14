package timelog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.FileChannel;

import javax.swing.*;

@SuppressWarnings("serial")
public class Main extends JFrame {
	private final Config config = new Config();
	private final ProjectsTree projectsTree;

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

	private void stopRecording() {
		try {projectsTree.stopRecording();}
		catch (Exception ex) {projectsTree.displayProblem(ex);}
	}

	ProjectsTree getProjectsTree() {
		return projectsTree;
	}

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

	public static void main(String[] args) throws Exception {
		new Main();
	}
}
