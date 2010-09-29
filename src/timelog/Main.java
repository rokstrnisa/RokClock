package timelog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Main extends JFrame {
	private final String configFilename = "config.txt";
	private final Config config = new Config(configFilename);
	private final ProjectsTree projectsTree;

	private Main() throws Exception {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		// loading settings
		setTitle(config.getTitle());
		setLocation(config.getLocX(), config.getLocY());
		// loading projects + GUI
		projectsTree = new ProjectsTree(this, config);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		getContentPane().setLayout(layout);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = constraints.gridy = 0;
		layout.setConstraints(projectsTree, constraints);
		getContentPane().add(projectsTree);
		JButton stopButton = createStopButton();
		constraints.gridy = 1;
		layout.setConstraints(stopButton, constraints);
		getContentPane().add(stopButton);
		pack();
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
