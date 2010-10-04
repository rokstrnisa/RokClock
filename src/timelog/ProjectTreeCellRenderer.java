package timelog;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {
	private ProjectsTree projectsTree;
	private Config config;
	
	ProjectTreeCellRenderer(ProjectsTree projectsTree, Config config) {
		this.projectsTree = projectsTree;
		this.config = config;
		setBackgroundNonSelectionColor(config.getDefaultColor());
		setBorderSelectionColor(null);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (leaf) {
			this.setIcon(leafIcon);
		} else if (expanded) {
			this.setIcon(openIcon);
		} else {
			this.setIcon(closedIcon);
		}
		this.setText(value.toString());
//		System.out.println(value.getClass());
		if (projectsTree.getCurrentPojectNode() == value) {
			System.out.println("Found current node: " + value);
			Color c = getSelectionColorForState(projectsTree.getState());
			System.out.println("Selection color should be: " + c);
			this.setBackground(c);
			this.setBackgroundNonSelectionColor(c);
			this.setBackgroundSelectionColor(c);
		}
		return this;
//		return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
//				row, hasFocus);
	}

//	public Color getBackgroundNonSelectionColor() {
//		return config.getDefaultColor();
//	}

//	public Color getBackgroundSelectionColor() {
//		return getCurrentSelectionColor();
//	}

//	public Color getBorderSelectionColor() {
//		return null;
//	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (d.width < 100) d.width = 100;
		return d;
	}

	public Icon getLeafIcon() {
		return super.getLeafIcon();
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
