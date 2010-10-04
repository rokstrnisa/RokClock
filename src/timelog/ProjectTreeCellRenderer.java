package timelog;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import timelog.ProjectsTree.ProjectNode;

class ProjectTreeCellRenderer implements TreeCellRenderer {
	private final ProjectsTree projectsTree;
	private final Config config;
	private final Icon leafIcon, openIcon, closedIcon;
	
	ProjectTreeCellRenderer(ProjectsTree projectsTree, Config config) {
		this.projectsTree = projectsTree;
		this.config = config;
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
			label.setBackground(config.getDefaultColor());
		return label;
	}

	private Color getSelectionColorForState(ProjectsTree.State state) {
		switch (state) {
			case AUTOMATIC: return config.getSemiActiveColor();
			case RUNNING: return config.getActiveColor();
			case STOPPED:
			default: return config.getDefaultColor();
		}
	}
}
