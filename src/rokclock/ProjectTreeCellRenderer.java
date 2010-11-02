package rokclock;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import rokclock.ProjectsTree.ProjectNode;

class ProjectTreeCellRenderer implements TreeCellRenderer {
	private final ProjectsTree projectsTree;
	private final Icon leafIcon, openIcon, closedIcon;
	private final Color defaultColor, semiActiveColor, activeColor;
	
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

	private Color getSelectionColorForState(ProjectsTree.State state) {
		switch (state) {
			case AUTOMATIC: return semiActiveColor;
			case RUNNING: return activeColor;
			case STOPPED:
			default: return defaultColor;
		}
	}
}
