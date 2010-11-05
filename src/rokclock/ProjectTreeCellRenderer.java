package rokclock;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import rokclock.ProjectsTree.ProjectNode;

/**
 * This class defines how the tree of projects is rendered.
 */
class ProjectTreeCellRenderer implements TreeCellRenderer {
	/**
	 * The tree of projects to render.
	 */
	private final ProjectsTree projectsTree;
	/**
	 * The icon used for the leaf nodes.
	 */
	private final Icon leafIcon;
	/**
	 * The icon used for the open (expanded) parent nodes.
	 */
	private final Icon openIcon;
	/**
	 * The icon used for the closed parent nodes.
	 */
	private final Icon closedIcon;
	/**
	 * The colour used for non-active elements and the background.
	 */
	private final Color defaultColor;
	/**
	 * The colour used for semi-active elements.
	 */
	private final Color semiActiveColor;
	/**
	 * The colour used for active elements.
	 */
	private final Color activeColor;
	
	/**
	 * The constructor that initialises all the fields according to the values
	 * from the configuration.
	 *
	 * @param projectsTree
	 *            The tree of projects.
	 * @param config
	 *            The configuration.
	 */
	ProjectTreeCellRenderer(ProjectsTree projectsTree, Config config) {
		this.projectsTree = projectsTree;
		defaultColor = config.getDefaultColor();
		semiActiveColor = config.getSemiActiveColor();
		activeColor = config.getActiveColor();
		DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
		leafIcon = defaultRenderer.getDefaultLeafIcon();
		openIcon = defaultRenderer.getDefaultOpenIcon();
		closedIcon = defaultRenderer.getDefaultClosedIcon();
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		ProjectNode node = (ProjectNode) value;
		JLabel label = node.getLabel();
		label.setIcon(leaf ? leafIcon : (expanded ? openIcon : closedIcon));
		if (projectsTree.getCurrentPojectNode() == node)
			label.setBackground(getSelectionColorForState(projectsTree.getState()));
		else
			label.setBackground(defaultColor);
		return label;
	}

	/**
	 * The method defines the mapping between the state of the time logging
	 * program ({@link ProjectsTree.State}) and the colours.
	 *
	 * @param state
	 *            The state to obtain the colour for.
	 * @return The colour.
	 */
	private Color getSelectionColorForState(ProjectsTree.State state) {
		switch (state) {
			case AUTOMATIC: return semiActiveColor;
			case RUNNING: return activeColor;
			case STOPPED:
			default: return defaultColor;
		}
	}
}
