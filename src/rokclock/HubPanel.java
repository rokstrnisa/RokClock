package rokclock;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

@SuppressWarnings("serial")
class HubPanel extends JPanel {
	private final ReviewDialog reviewDialog;
	private final Config config;
	private final String baseAddress;
	private final JLabel teamLabel = new JLabel("Team:", SwingConstants.RIGHT);
	private final JComboBox teamCB = new JComboBox();
	private final JCheckBox defaultTeamCB = new JCheckBox("Set as default.", null, true);
	private final JButton submitButton;
	private boolean ready = false;

	HubPanel(final ReviewDialog reviewDialog, Config config) {
		this.reviewDialog = reviewDialog;
		this.config = config;
		this.baseAddress = config.getHub();
		submitButton = createSubmitButton();
		// filling in data
		try {
			for (String team : config.fetchTeams())
				teamCB.addItem(team);
			teamCB.setSelectedItem(config.getTeam());
		} catch (IOException e) {
			teamCB.addItem("ERROR!");
			System.err.println("Could not load hub's \"teams.txt\" file.");
		}
		// layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.ipadx = 10;
		gbc.gridx = 0; gbc.gridy = 0;
		gbl.setConstraints(teamLabel, gbc);
		gbc.gridx = 1; gbc.gridy = 0;
		gbl.setConstraints(teamCB, gbc);
		gbc.gridx = 3; gbc.gridy = 0;
		gbl.setConstraints(defaultTeamCB, gbc);
		gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
		gbl.setConstraints(submitButton, gbc);
		add(teamLabel);
		add(teamCB);
		add(defaultTeamCB);
		add(submitButton);
		ready = true;
	}

	/**
	 * The function creates a button, which sends the data to the hub, also
	 * saving any user properties selected in the form.
	 * 
	 * @return The 'submit' button.
	 */
	private JButton createSubmitButton() {
		JButton b = new JButton("SUBMIT TO HUB");
		b.setToolTipText("Save the above percentages into the specified hub location ('" + config.getHub() + "').");
		b.setBackground(Color.BLACK);
		b.setForeground(Color.GRAY);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (reviewDialog.isIntervalCustom()) {
					JOptionPane
					.showMessageDialog(
							reviewDialog,
							"Please specify the interval only through year/week selection. "
							+ "Custom date selection is not supported when using the hub.",
							"No custom dates",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String team = teamCB.getSelectedItem().toString();
				if (defaultTeamCB.isSelected())
					config.setTeam(team);
				String weekID = reviewDialog.getSelectedWeekID();
				String weekDir = baseAddress + "/raw/" + team + "/" + weekID + "/";
				int random = findFreshRandom(weekDir);
				File logFile = new File(weekDir + "/" + random + ".log");
				File submittedFile = new File(weekDir + "/submitted.txt");
				try {
					String user = config.getUsernameOnHub();
					if (user.equals("undefined"))
						throw new IOException(
								"Please set the property 'usernameOnHub' in RokClock's 'config.txt' to your unix username (on the filesystem that contains '"
								+ baseAddress + "').");
					String date = Config.df.format(new Date());
					reviewDialog.writeToFile(logFile);
					BufferedWriter bw = new BufferedWriter(new FileWriter(submittedFile, true));
					bw.write(random + "," + user + "," + date + "\r\n");
					bw.close();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(reviewDialog,
							ex.getMessage(), "Error occurred",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(reviewDialog,
						"Data successfully saved to the hub.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				reviewDialog.setVisible(false);
			}
		});
		return b;
	}

	private int findFreshRandom(String weekDir) {
		Random randomGen = new Random();
		File f = null;
		int random;
		do {
			random = 10000 + randomGen.nextInt(90000);
			f = new File(weekDir + "/" + random + ".log");
		} while (f.exists());
		return random;
	}

	public boolean isReady() {
		return ready;
	}
}
