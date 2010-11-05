package rokclock;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class defines the dialog that appears when the selects the option to add
 * a sub-project.
 */
@SuppressWarnings("serial")
class ProjectDialog extends JDialog {
	/**
	 * The label for the sub-project name.
	 */
	private JLabel projectNameL = new JLabel("[Sub-]Project Name: ");
	/**
	 * The label for the description.
	 */
	private JLabel descriptionL = new JLabel("Description (optional): ");
	/**
	 * The text field for the sub-project name.
	 */
	private JTextField projectNameTF = new JTextField();
	/**
	 * The text field for the description.
	 */
	private JTextField descriptionTF = new JTextField();
	/**
	 * The cancel button. If pressed, the action is cancelled.
	 */
	private JButton cancelB = new JButton("Cancel");
	/**
	 * The add button. If pressed, a sub-project is added with the specified
	 * data.
	 */
	private JButton addB = new JButton("Add");

	/**
	 * The constructor for the dialog.
	 *
	 * @param frame
	 *            The parent frame.
	 * @param projectsTree
	 *            The tree of projects to insert into.
	 * @param parent
	 *            The parent project node to insert under.
	 */
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
