package rokclock;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
class ProjectDialog extends JDialog {
	private JLabel projectNameL = new JLabel("[Sub-]Project Name: ");
	private JLabel descriptionL = new JLabel("Description (optional): ");
	private JTextField projectNameTF = new JTextField();
	private JTextField descriptionTF = new JTextField();
	private JButton cancelB = new JButton("Cancel");
	private JButton addB = new JButton("Add");

	public ProjectDialog(final Frame frame, final ProjectsTree projectsTree, final DefaultMutableTreeNode parent) {
		// basic configuration
		super(frame, true);
		setLocation(frame.getLocation());
		setBackground(Color.BLACK);
		setTitle("Address Dialog");
		// button semantics
		cancelB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProjectDialog.this.setVisible(false);
			}
		});
		addB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String projectName = projectNameTF.getText().trim();
				if (projectName.isEmpty())
					JOptionPane.showMessageDialog(frame, "Please enter a [sub-]project name.");
				else {
					ProjectDialog.this.setVisible(false);
					String tooltip = descriptionTF.getText().trim();
					projectsTree.addChildNodeTo(parent, projectName, tooltip);
				}
			}
		});
		// layout
		setLayout(new GridLayout(3, 2));
		add(projectNameL);
		add(projectNameTF);
		add(descriptionL);
		add(descriptionTF);
		add(cancelB);
		add(addB);
		pack();
	}
}
