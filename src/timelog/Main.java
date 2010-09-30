package timelog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class Main extends JFrame {
	private final String configFilename = "config.txt";
	private final Config config = new Config(configFilename);
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
		JButton stopButton = createStopButton();
		constraints.gridy = 1;
		constraints.weighty = 0;
		layout.setConstraints(stopButton, constraints);
		getContentPane().add(stopButton);
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
				try {projectsTree.stopRecording();}
				catch (Exception ex) {projectsTree.displayProblem(ex);}
			}
		});
		return b;
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}
}
