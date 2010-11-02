package rokclock;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
class ReviewDialog extends JDialog implements CaretListener {
	private class DateLabel extends JLabel {
		private GregorianCalendar calendar = new GregorianCalendar();

		DateLabel() {
			super("", CENTER);
			setToolTipText("Click to change.");
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					DateChooser dc = new DateChooser(ReviewDialog.this, calendar);
					if (dc.showDateChooser() == DateChooser.OK_OPTION) {
						calendar = dc.getDate();
						refresh();
						refreshReviewTable();
					}
				}
			});
		}

		void setDate(GregorianCalendar calendar) {
			this.calendar = (GregorianCalendar) calendar.clone();
			refresh();
		}

		Date getDate() {
			return calendar.getTime();
		}

		private void refresh() {
			setText(dateFormat.format(calendar.getTime()));
		}
	}

	private class Row {
		private double hours = 0;
		private JTextField hoursTF = new JTextField();
		private JLabel percentL = new JLabel("N/A", SwingConstants.RIGHT);

		Row() {
			hoursTF.addCaretListener(ReviewDialog.this);
			hoursTF.setHorizontalAlignment(JTextField.RIGHT);
		}
	}

	private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	private Main main;
	private Config config;
	private Analyser analyser = new Analyser();

	private Color normalColour = Color.BLACK;
	private Color errorColour = Color.RED;

	private JLabel fromLabel = new JLabel("From (inclusive):", SwingConstants.RIGHT);
	private JLabel toLabel = new JLabel("To (exclusive):", SwingConstants.RIGHT);
	private DateLabel fromDate = new DateLabel();
	private DateLabel toDate = new DateLabel();
	private JPanel reviewPanel = new JPanel();
	private Map<String, Row> rows = new HashMap<String, Row>();
	private JLabel totalLabel = new JLabel("", SwingConstants.RIGHT);
	private JButton saveButton = createSaveButton();
	private JFileChooser fileChooser = new JFileChooser();

	ReviewDialog(Main main, Config config) {
		super(main, "Review & Save");
		this.main = main;
		this.config = config;
		// obtain default dates
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		calendar.set(GregorianCalendar.MINUTE, 0);
		calendar.set(GregorianCalendar.SECOND, 0);
		toDate.setDate(calendar);
		calendar.add(GregorianCalendar.DAY_OF_MONTH, -7);
		fromDate.setDate(calendar);
		// layout date components
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.ipadx = 10;
		gbc.gridx = gbc.gridy = 0;
		gbl.setConstraints(fromLabel, gbc);
		gbc.weightx = 1;
		gbc.gridx = 1;
		gbl.setConstraints(fromDate, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 0;
		gbc.gridx = 0; gbc.gridy = 1;
		gbl.setConstraints(toLabel, gbc);
		gbc.weightx = 1;
		gbc.gridx = 1;
		gbl.setConstraints(toDate, gbc);
		gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 5, 10, 5);
		gbc.weighty = 1;
		JScrollPane scrollReviewPanel = new JScrollPane(reviewPanel);
		gbl.setConstraints(scrollReviewPanel, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridy = 3;
		gbc.weighty = 0;
		gbl.setConstraints(saveButton, gbc);
		add(fromLabel);
		add(fromDate);
		add(toLabel);
		add(toDate);
		add(scrollReviewPanel);
		add(saveButton);
		// layout results
		refreshReviewTable();
		setVisible(true);
		setLocation(main.getLocation());
	}

	private void refreshReviewTable() {
		reviewPanel.removeAll();
		rows.clear();
		GridBagLayout gbl = new GridBagLayout();
		reviewPanel.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		try {
			Map<String, Long> sums = analyser.processLogFile(config.getLogFilename(), fromDate.getDate(), toDate.getDate());
			for (Entry<String, Long> entry : sums.entrySet()) {
				String project = entry.getKey();
				double hours = 1.0 * entry.getValue() / (1000 * 3600);
				addRow(gbl, gbc, project, hours);
			}
			for (String project : main.getProjectsTree().getTopLevelProjects())
				if (!rows.containsKey(project))
					addRow(gbl, gbc, project, 0);
			gbc.insets = new Insets(10, 0, 0, 0);
			addLeftLabel(gbl, gbc, "TOTAL");
			gbc.gridx = 1;
			gbc.weightx = 1;
			totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
			gbl.setConstraints(totalLabel, gbc);
			reviewPanel.add(totalLabel);
			gbc.weightx = 0;
			addRightLabel(gbl, gbc);
		} catch (IOException e) {e.printStackTrace();}
		recomputeTotal();
		pack();
	}

	private void addRow(GridBagLayout gbl, GridBagConstraints gbc, String title, double hours) {
		Row row = new Row();
		addLeftLabel(gbl, gbc, title);
		addMiddleField(gbl, gbc, row, hours);
		addRightLabel(gbl, gbc);
		addPercentLabel(gbl, gbc, row);
		rows.put(title, row);
		gbc.gridy++;
	}

	private void addLeftLabel(GridBagLayout gbl, GridBagConstraints gbc, String title) {
		JLabel projectLabel = new JLabel(title + ": ", SwingConstants.RIGHT);
		gbc.gridx = 0;
		gbl.setConstraints(projectLabel, gbc);
		reviewPanel.add(projectLabel);
	}

	private void addMiddleField(GridBagLayout gbl, GridBagConstraints gbc, Row row, double hours) {
		row.hoursTF.setText(decimalFormat.format(hours));
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbl.setConstraints(row.hoursTF, gbc);
		gbc.weightx = 0;
		reviewPanel.add(row.hoursTF);
	}

	private void addRightLabel(GridBagLayout gbl, GridBagConstraints gbc) {
		JLabel hLabel = new JLabel("h", SwingConstants.CENTER);
		gbc.gridx = 2;
		gbc.ipadx = 5;
		gbl.setConstraints(hLabel, gbc);
		gbc.ipadx = 0;
		reviewPanel.add(hLabel);
	}

	private void addPercentLabel(GridBagLayout gbl, GridBagConstraints gbc, Row row) {
		gbc.gridx = 3;
		gbc.ipadx = 5;
		gbl.setConstraints(row.percentL, gbc);
		gbc.ipadx = 0;
		reviewPanel.add(row.percentL);
	}

	private void recomputeTotal() {
		double total = 0;
		for (Row row : rows.values()) {
			try {
				row.hours = Double.parseDouble(row.hoursTF.getText());
				total += row.hours;
				row.hoursTF.setForeground(normalColour);
			} catch (NumberFormatException e) {
				row.hoursTF.setForeground(errorColour);
				totalLabel.setText("ERROR");
				totalLabel.setForeground(errorColour);
				return;
			}
		}
		totalLabel.setText(decimalFormat.format(total));
		totalLabel.setForeground(normalColour);
		for (Row row : rows.values())
			row.percentL.setText("(" + decimalFormat.format(100 * row.hours / total) + "%)");
		pack();
	}

	private JButton createSaveButton() {
		JButton b = new JButton("SAVE");
		b.setBackground(Color.BLACK);
		b.setForeground(Color.GRAY);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String defaultFilename = "time-sheet-"
						+ dateFormat.format(fromDate.getDate()) + "-"
						+ dateFormat.format(toDate.getDate()) + ".txt";
				defaultFilename = defaultFilename.replaceAll("/", "");
				fileChooser.setSelectedFile(new File(defaultFilename));
				int returnValue = fileChooser.showDialog(ReviewDialog.this, "Save");
				if (returnValue != JFileChooser.APPROVE_OPTION) return;
				writeToFile(fileChooser.getSelectedFile());
				ReviewDialog.this.setVisible(false);
			}
		});
		return b;
	}

	private void writeToFile(File f) {
		final String nl = "\r\n";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			double total = Double.parseDouble(totalLabel.getText());
			for (Entry<String, Row> entry : rows.entrySet()) {
				double hours = Double.parseDouble(entry.getValue().hoursTF.getText());
				double fraction = hours / total;
				bw.write(entry.getKey() + "," + decimalFormat.format(fraction) + nl);
			}
			bw.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public void caretUpdate(CaretEvent e) {
		recomputeTotal();
	}
}
