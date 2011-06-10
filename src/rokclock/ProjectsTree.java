package rokclock;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;

/**
 * This class implements the main logic of the program.
 */
@SuppressWarnings("serial")
class ProjectsTree extends JTree implements TimeLog {
	/**
	 * A customisation of the default tree node. It includes a tooltip.
	 */
	static class ProjectNode extends DefaultMutableTreeNode {
		/**
		 * The tooltip text for the node.
		 */
		private final String tooltip;
		/**
		 * The label containing a project's name.
		 */
		private final JLabel label;

		/**
		 * A simple constructor.
		 *
		 * @param caption
		 *            A project's name.
		 * @param tooltip
		 *            The project's description.
		 */
		public ProjectNode(String caption, String tooltip) {
			super(caption);
			this.tooltip = tooltip;
			label = new JLabel(caption);
			label.setOpaque(true);
		}

		/**
		 * Obtains the tooltip.
		 *
		 * @return The tooltip.
		 */
		public String getTooltip() {
			return tooltip;
		}

		/**
		 * Obtains the label associated with this node.
		 *
		 * @return The label.
		 */
		public JLabel getLabel() {
			return label;
		}
	}

	/**
	 * A platform-independent newline.
	 */
	private final String nl = System.getProperty("line.separator");
	/**
	 * A link to the configuration's date format.
	 */
	private final DateFormat df = Config.df;
	/**
	 * A link to the parent component.
	 */
	private final Frame frame;
	/**
	 * A link to the configuration object.
	 */
	private final Config config;
	/**
	 * The invisible root node of the tree.
	 */
	private final DefaultMutableTreeNode root;
	/**
	 * The tree's model containing the tree's data.
	 */
	private final DefaultTreeModel model;
	/**
	 * The popup menu that appears when the user right-clicks.
	 */
	private PopupMenu popupMenu;
	/**
	 * The last right-clicked project path. Can be null.
	 */
	private TreePath lastRightClickedPath;
	/**
	 * The currently running project node. Can be null.
	 */
	private ProjectNode currentProjectNode = null;
	/**
	 * The currently running project path. Can be null.
	 */
	private String[] currentProjectPath = null;

	/**
	 * The enumeration of the possible states of the program.
	 */
	enum State {STOPPED, RUNNING, AUTOMATIC}
	/**
	 * The current state of the program.
	 */
	private State state = State.STOPPED;
	/**
	 * The timer used for triggering events after specified periods.
	 */
	private final Timer timer;
	/**
	 * The time that the last (can be current) activity started, in milliseconds
	 * from epoch.
	 */
	private long startTime = 0;

	/**
	 * Creates a projects tree given the parent component and the link to the
	 * configuration object.
	 *
	 * @param frame
	 *            The parent component.
	 * @param config
	 *            The configuration object.
	 * @throws IOException
	 *             Thrown if problems occur when loading the projects' file.
	 */
	ProjectsTree(Frame frame, Config config) throws IOException {
		super(new ProjectNode("root", "root"));
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
			@Override
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

	@Override
	public String getToolTipText(MouseEvent e) {
		if (getRowForLocation(e.getX(), e.getY()) == -1)
			return null;
		TreePath path = getPathForLocation(e.getX(), e.getY());
		return ((ProjectNode) path.getLastPathComponent()).getTooltip();
	}

	/**
	 * Creates a popup menu that gives the user the ability to either delete the
	 * selected tree node, or to create its child.
	 *
	 * @return The popup menu.
	 */
	private PopupMenu createPopupMenu() {
		popupMenu = new PopupMenu();
		MenuItem deleteMI = new MenuItem("Delete");
		deleteMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MutableTreeNode node = (MutableTreeNode) lastRightClickedPath.getLastPathComponent();
				model.removeNodeFromParent(node);
				try {saveProjects();}
				catch (IOException ex) {displayProblem(ex);}
			}
		});
		MenuItem addChildMI = new MenuItem("Add child to");
		addChildMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) lastRightClickedPath.getLastPathComponent();
				new ProjectDialog(frame, ProjectsTree.this, parent).setVisible(true);
			}
		});
		popupMenu.add(deleteMI);
		popupMenu.add(addChildMI);
		return popupMenu;
	}

	/**
	 * Adds a child node to a tree node.
	 *
	 * @param parent
	 *            The parent node.
	 * @param project
	 *            The sub-project that the new node represents.
	 * @param tooltip
	 *            The description of the sub-project.
	 */
	void addChildNodeTo(DefaultMutableTreeNode parent, String project, String tooltip) {
		ProjectNode child = new ProjectNode(project, tooltip);
		parent.add(child);
		model.reload();
		expandPath(new TreePath(parent.getPath()));
		try {saveProjects();}
		catch (IOException e) {displayProblem(e);}
	}

	/**
	 * Obtains the project file. If no file is found, it copies the default
	 * template into the expected place.
	 *
	 * @return The projects file.
	 * @throws IOException
	 *             Thrown if copying of the default template fails.
	 */
	private File getProjectsFile() throws IOException {
		File projectsFile = new File(config.getProjectsFilename());
		if (!projectsFile.exists())
			Main.copyFile(new File(config.getProjectsFilenameDefault()), projectsFile);
		return projectsFile;
	}

	/**
	 * Loads the projects file. On every line, everything after '#' is ignored.
	 * The number of tabs on the left indicates the depth of the node in the
	 * tree --- the node is creates as a child of a node corresponding to the
	 * previous line indented by one tab less. A description of a project can be
	 * specified after the project's name in curly brackets.
	 *
	 * @throws IOException
	 *             Thrown if there are problems reading the file.
	 */
	private void loadProjects() throws IOException {
		List<DefaultMutableTreeNode> nodeChain = new ArrayList<DefaultMutableTreeNode>();
		nodeChain.add(root);
		BufferedReader br = new BufferedReader(new FileReader(getProjectsFile()));
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

	/**
	 * Writes the current tree of projects back to the projects' file. This is
	 * used whenever the user changes the tree through the GUI.
	 *
	 * @throws IOException
	 *             Thrown if there are problems writing to the projects' file.
	 */
	private void saveProjects() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("# Syntax:" + nl + "# main_project[{tooltip}]" + nl);
		sb.append("# \tsub_project[{tooltip}]" + nl + "# \t\tsub_sub_project[{tooltip}]" + nl + nl);
		saveChildrenOf(sb, root, 0);
		BufferedWriter bw = new BufferedWriter(new FileWriter(config.getProjectsFilename()));
		bw.write(sb.toString());
		bw.close();
	}

	/**
	 * This is the recursive part of
	 * {@link #saveChildrenOf(StringBuilder, TreeNode, int)}.
	 *
	 * @param sb
	 *            The string builder to write to.
	 * @param parent
	 *            The parent node of which the children to save.
	 * @param depth
	 *            The current depth in the tree.
	 */
	private void saveChildrenOf(StringBuilder sb, TreeNode parent, int depth) {
		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			ProjectNode child = (ProjectNode) parent.getChildAt(i);
			String projectName = child.getUserObject().toString();
			String tooltip = child.getTooltip();
			for (int j = 0; j < depth; j++) sb.append('\t');
			sb.append(projectName);
			if (tooltip != null && !tooltip.isEmpty()) sb.append("{" + tooltip + "}");
			sb.append(nl);
			saveChildrenOf(sb, child, depth + 1);
		}
	}

	/**
	 * Extracts the project's name from the project's line.
	 *
	 * @param s
	 *            The project's line.
	 * @return The extracted project's name.
	 *
	 * @see {@link #loadProjects()}
	 */
	private String extractName(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		if (left == -1) return s;
		return s.substring(0, left).trim();
	}

	/**
	 * Extracts the project's description from the project's line.
	 *
	 * @param s
	 *            The project's line.
	 * @return The extracted project's description.
	 *
	 * @see {@link #loadProjects()}
	 */
	private String extractTooltip(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		int right = s.lastIndexOf('}');
		if (left == -1 || right == -1) return null;
		return s.substring(left + 1, right).trim();
	}

	/**
	 * Creates the time with the specified interval period (obtained from the
	 * configuration).
	 *
	 * @return The timer.
	 */
	private Timer createTimer() {
		Timer t = new Timer(config.getIntervalInSeconds() * 1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPeriodicAction();
			}
		});
		return t;
	}

	/**
	 * Expands all tree nodes in the GUI.
	 */
	private void expandAllNodes() {
		for (int i = 0; i < getRowCount(); i++)
			expandRow(i);
	}

	/**
	 * This method defines what happens when the user left-clicks: the clicked
	 * project path is determined, the recording is started, and the window is
	 * potentially hidden (depends on the configuration).
	 *
	 * @param e
	 *            The related mouse event.
	 */
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
			startRecording(projectPath);
			minimiseOrHide();
		} catch (Exception ex) {displayProblem(ex);}
	}

	/**
	 * This method defines what happens when the user middle-clicks: the clicked
	 * project path is determined, and expanded or collapsed depending on the
	 * previous state.
	 *
	 * @param e
	 *            The related mouse event.
	 */
	private void onMiddleMouseClick(MouseEvent e) {
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (isExpanded(path)) collapsePath(path);
		else expandPath(path);
	}

	/**
	 * This method defines what happens when the user right-clicks: the clicked
	 * project path is determined, saved, and used for the potential subsequent
	 * action from the popup menu.
	 *
	 * @param e
	 *            The related mouse event.
	 */
	private void onRightMouseClick(MouseEvent e) {
		lastRightClickedPath = getPathForLocation(e.getX(), e.getY());
		if (lastRightClickedPath != null)
			popupMenu.show(frame, e.getX(), e.getY());
	}

	@Override
	public void startRecording(String[] projectPath) throws Exception {
		if (state != State.STOPPED) stopRecording();
		currentProjectPath = projectPath;
		startTime = System.currentTimeMillis();
		switchToActiveState(projectPath);
	}

	@Override
	public void stopRecording() throws Exception {
		switch (state) {
		case STOPPED:
			return;
		case AUTOMATIC:
			timer.stop();
			stopAutomaticRecording();
			break;
		case RUNNING:
			writeLogEntry(startTime, System.currentTimeMillis());
		}
		switchToStoppedState();
	}

	/**
	 * This method is called when the user stops the semi-active period. The
	 * action depends on the configuration option
	 * {@link Config#getAutoCountTowards()}.
	 *
	 * @throws Exception
	 *             Thrown if a log entry cannot be written.
	 */
	private void stopAutomaticRecording() throws Exception {
		switch (config.getAutoCountTowards()) {
		case NOTHING:
			break;
		case UNKNOWN:
			currentProjectPath = new String[] {"unknown"};
			//$FALL-THROUGH$
		case PREVIOUS:
			writeLogEntry(startTime, System.currentTimeMillis());
		}
	}

	@Override
	public void doPeriodicAction() {
		try {
			if (state == State.AUTOMATIC) {
				timer.stop();
				potentiallyWriteTimeout();
				switchToStoppedState();
			} else if (state == State.RUNNING) {
				stopRecording();
				startRecording(currentProjectPath);
				switchToSemiActiveState();
			}
		} catch (Exception ex) {displayProblem(ex);}
		unminimiseOrShow();
	}

	/**
	 * This method is called when the semi-active period ends without user
	 * interaction. It potentially writes this event to the log, depending on
	 * the configuration option {@link Config#getWriteTimeouts()}.
	 *
	 * @throws Exception
	 *             Thrown if a log entry cannot be written.
	 */
	private void potentiallyWriteTimeout() throws Exception {
		if (!config.getWriteTimeouts()) return;
		currentProjectPath = new String[] {"(timed out)"};
		writeLogEntry(startTime, startTime);
	}

	@Override
	public void writeLogEntry(long startTime, long endTime) throws Exception {
		String startTimeS = df.format(new Date(startTime));
		String endTimeS = df.format(new Date(endTime));
		String uid = config.getUID();
		String entry = (uid.equals("") ? "" : uid + ",") + startTimeS + "," + endTimeS;
		for (String projectPathNode : currentProjectPath)
			entry += "," + projectPathNode;
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(config.getLogFilename(), true)));
		out.write(entry + nl);
		out.close();
	}

	@Override
	public void switchToActiveState(String[] projectPath) {
		int delay = config.getIntervalInSeconds() * 1000;
		timer.setDelay(delay);
		timer.setInitialDelay(delay);
		timer.restart();
		state = State.RUNNING;
		repaint();
	}

	@Override
	public void switchToSemiActiveState() {
		int delay = config.getWaitInSeconds() * 1000;
		timer.setDelay(delay);
		timer.setInitialDelay(delay);
		timer.restart();
		state = State.AUTOMATIC;
		repaint();
	}

	@Override
	public void switchToStoppedState() {
		state = State.STOPPED;
		repaint();
	}

	@Override
	public void minimiseOrHide() {
		new Thread() {
			@Override
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

	@Override
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

	@Override
	public void displayProblem(Exception e) {
		JOptionPane.showMessageDialog(this, "A problem has occurred: " + e.getMessage());
	}

	/**
	 * Obtains the currently active project node.
	 *
	 * @return The project node.
	 */
	ProjectNode getCurrentPojectNode() {
		return currentProjectNode;
	}

	/**
	 * Obtains the current state of the program.
	 *
	 * @return The state.
	 */
	State getState() {
		return state;
	}

	/**
	 * Obtains the array of top-level projects.
	 *
	 * @return The top-level projects.
	 */
	String[] getTopLevelProjects() {
		final int size = root.getChildCount();
		String[] projects = new String[size];
		for (int i = 0; i < size; i++)
			projects[i] = ((ProjectNode) root.getChildAt(i)).getUserObject().toString();
		return projects;
	}
}
