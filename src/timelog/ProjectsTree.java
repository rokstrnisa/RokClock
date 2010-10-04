package timelog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;

@SuppressWarnings("serial")
class ProjectsTree extends JTree implements TimeLog {
	class ProjectNode extends DefaultMutableTreeNode {
		private final String tooltip;
		private final JLabel label;

		public ProjectNode(String caption, String tooltip) {
			super(caption);
			this.tooltip = tooltip;
			label = new JLabel(caption);
			label.setOpaque(true);
		}

		public String getTooltip() {
			return tooltip;
		}
		
		public JLabel getLabel() {
			return label;
		}
	}

	private final String nl = System.getProperty("line.separator");
	private final DateFormat df = Config.df;

	private final Frame frame;
	private final Config config;
	private final DefaultMutableTreeNode root;
	private final DefaultTreeModel model;
	private PopupMenu popupMenu;
	private TreePath lastRightClickedPath;
	private ProjectNode currentProjectNode = null;
	private String[] currentProjectPath = null;

	enum State {STOPPED, RUNNING, AUTOMATIC}
	private State state = State.STOPPED;

	private final Timer timer;
	private long startTime = 0;

	ProjectsTree(Frame frame, Config config) throws IOException {
		super(new DefaultMutableTreeNode());
		this.frame = frame;
		this.config = config;
		root = (DefaultMutableTreeNode) getModel().getRoot();
		model = (DefaultTreeModel) getModel();
		loadProjects();
		expandAllNodes();
		setRootVisible(false);
		setBackground(config.getDefaultColor());
		setCellRenderer(new ProjectTreeCellRenderer(this, config));
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				switch (e.getButton()) {
					case MouseEvent.BUTTON1: onLeftMouseClick(e); break;
					case MouseEvent.BUTTON2: onMiddleMouseClick(e); break;
					case MouseEvent.BUTTON3: onRightMouseClick(e); break;
				}
			}
		});
		timer = createTimer();
		ToolTipManager.sharedInstance().registerComponent(this);
		frame.add(createPopupMenu());
	}

	public String getToolTipText(MouseEvent e) {
		if (getRowForLocation(e.getX(), e.getY()) == -1)
			return null;
		TreePath path = getPathForLocation(e.getX(), e.getY());
		return ((ProjectNode) path.getLastPathComponent()).getTooltip();
	}

	private PopupMenu createPopupMenu() {
		popupMenu = new PopupMenu();
		MenuItem deleteMI = new MenuItem("Delete");
		deleteMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MutableTreeNode node = (MutableTreeNode) lastRightClickedPath.getLastPathComponent();
				model.removeNodeFromParent(node);
				try {saveProjects();}
				catch (IOException ex) {displayProblem(ex);}
			}
		});
		MenuItem addChildMI = new MenuItem("Add child to");
		addChildMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) lastRightClickedPath.getLastPathComponent();
				new ProjectDialog(frame, ProjectsTree.this, parent).setVisible(true);
			}
		});
		popupMenu.add(deleteMI);
		popupMenu.add(addChildMI);
		return popupMenu;
	}

	void addChildNodeTo(DefaultMutableTreeNode parent, String project, String tooltip) {
		ProjectNode child = new ProjectNode(project, tooltip);
		parent.add(child);
		model.reload();
		expandPath(new TreePath(parent.getPath()));
		try {saveProjects();}
		catch (IOException e) {displayProblem(e);}
	}

	private void loadProjects() throws IOException {
		List<DefaultMutableTreeNode> nodeChain = new ArrayList<DefaultMutableTreeNode>();
		nodeChain.add(root);
		BufferedReader br = new BufferedReader(new FileReader(config.getProjectsFilename()));
		String line;
		while ((line = br.readLine()) != null) {
			// pre-processing and comments
			int hash = line.indexOf('#');
			if (hash != -1)
				line = line.substring(0, hash);
			if (line.trim().isEmpty()) continue;
			// processing
			int depth = 0;
			while (line.charAt(depth) == '\t') depth++; // compute the depth of the new element
			line = line.substring(depth);
			while (depth < nodeChain.size())
				nodeChain.remove(depth); // remove irrelevant part of the chain
			ProjectNode node = new ProjectNode(extractName(line), extractTooltip(line));
			if (depth == 0) root.add(node);
			else nodeChain.get(depth - 1).add(node);
			nodeChain.add(node);
		}
		br.close();
	}

	private void saveProjects() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("# For main projects, you should only use the approved 3-letter acronyms." + nl);
		sb.append("# You are free to comment out or delete any line that does not apply to you." + nl + nl);
		sb.append("# Syntax:" + nl + "# main_project[{tooltip}]" + nl);
		sb.append("# \tsub_project[{tooltip}]" + nl + "# \t\tsub_sub_project[{tooltip}]" + nl + nl);
		saveChildrenOf(sb, root, 0);
		BufferedWriter bw = new BufferedWriter(new FileWriter(config.getProjectsFilename()));
		bw.write(sb.toString());
		bw.close();
	}

	private void saveChildrenOf(StringBuilder sb, TreeNode parent, int depth) {
		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			ProjectNode child = (ProjectNode) parent.getChildAt(i);
			String projectName = child.getUserObject().toString();
			String tooltip = child.getTooltip();
			for (int j = 0; j < depth; j++) sb.append('\t');
			sb.append(projectName);
			if (!tooltip.isEmpty()) sb.append("{" + tooltip + "}");
			sb.append(nl);
			saveChildrenOf(sb, child, depth + 1);
		}
	}

	private String extractName(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		if (left == -1) return s;
		return s.substring(0, left).trim();
	}

	private String extractTooltip(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		int right = s.lastIndexOf('}');
		if (left == -1 || right == -1) return null;
		return s.substring(left + 1, right).trim();
	}

	private Timer createTimer() {
		Timer t = new Timer(config.getIntervalInSeconds() * 1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPeriodicAction();
			}
		});
		return t;
	}

	private void expandAllNodes() {
		for (int i = 0; i < getRowCount(); i++)
			expandRow(i);
	}

	private void onLeftMouseClick(MouseEvent e) {
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path == null)
			return;
		ProjectNode node = (ProjectNode) path.getLastPathComponent();
		currentProjectNode = node;
		String[] projectPath = new String[path.getPathCount() - 1];
		for (int i = 1; i < path.getPathCount(); i++)
			projectPath[i-1] = ((ProjectNode) path.getPathComponent(i)).getUserObject().toString();
		try {
			if (state == State.AUTOMATIC)
				state = State.RUNNING;
			startRecording(projectPath);
			minimiseOrHide();
			timer.restart();
		} catch (Exception ex) {displayProblem(ex);}
	}

	private void onMiddleMouseClick(MouseEvent e) {
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (isExpanded(path)) collapsePath(path);
		else expandPath(path);
	}

	private void onRightMouseClick(MouseEvent e) {
		lastRightClickedPath = getPathForLocation(e.getX(), e.getY());
		if (lastRightClickedPath != null)
			popupMenu.show(frame, e.getX(), e.getY());
	}

	public void startRecording(String[] projectPath) throws Exception {
		if (state != State.STOPPED) stopRecording();
		currentProjectPath = projectPath;
		startTime = System.currentTimeMillis();
		switchToActiveState(projectPath);
	}

	public void stopRecording() throws Exception {
		switch (state) {
			case STOPPED:
				return;
			case AUTOMATIC:
				timer.stop();
				break;
			case RUNNING:
				writeLogEntry(startTime, System.currentTimeMillis());
		}
		switchToStoppedState();
	}

	public void doPeriodicAction() {
		try {
			if (state == State.AUTOMATIC) {
				stopRecording();
			} else if (state == State.RUNNING) {
				stopRecording();
				startRecording(currentProjectPath);
				switchToSemiActiveState();
			}
		} catch (Exception ex) {displayProblem(ex);}
		unminimiseOrShow();
	}

	public void writeLogEntry(long startTime, long endTime) throws Exception {
		String startTimeS = df.format(new Date(startTime));
		String endTimeS = df.format(new Date(endTime));
		String entry = startTimeS + "," + endTimeS;
		for (String projectPathNode : currentProjectPath)
			entry += "," + projectPathNode;
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(config.getLogFilename(), true)));
		out.write(entry + nl);
		out.close();
	}

	public void switchToActiveState(String[] projectPath) {
		int delay = config.getIntervalInSeconds() * 1000;
		timer.setDelay(delay);
		timer.setInitialDelay(delay);
		timer.restart();		
		state = State.RUNNING;
		repaint();
	}

	public void switchToSemiActiveState() {
		int delay = config.getWaitInSeconds() * 1000;
		timer.setDelay(delay);
		timer.setInitialDelay(delay);
		timer.restart();
		state = State.AUTOMATIC;
		repaint();
	}

	public void switchToStoppedState() {
		state = State.STOPPED;
		repaint();
	}

	public void minimiseOrHide() {
		new Thread() {
			public void run() {
				try {Thread.sleep(150);}
				catch (InterruptedException e) {e.printStackTrace();}
				switch (config.getBehaviour()) {
					case MINIMISE:
						frame.setExtendedState(Frame.ICONIFIED);
						break;
					case HIDE:
						frame.setVisible(false);
						break;
					case SHOW:
						break;				
				}
			}
		}.start();
	}

	public void unminimiseOrShow() {
		switch (config.getBehaviour()) {
			case MINIMISE:
				frame.setExtendedState(Frame.NORMAL);
				break;
			case HIDE:
			case SHOW:
				frame.setVisible(true);
				break;				
		}
	}

	public void displayProblem(Exception e) {
		JOptionPane.showMessageDialog(this, "A problem has occurred: " + e.getMessage());
	}
	
	ProjectNode getCurrentPojectNode() {
		return currentProjectNode;
	}
	
	State getState() {
		return state;
	}
}
