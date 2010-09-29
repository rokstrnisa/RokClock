package timelog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
class ProjectsTree extends JTree implements TreeSelectionListener, TimeLog {
	private final String newline = System.getProperty("line.separator");
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private final Frame frame;
	private final Config config;
	private final DefaultMutableTreeNode root;
	
	private String[] currentPojectPath = null;
	
	enum State {STOPPED, RUNNING, AUTOMATIC}
	private State state = State.STOPPED;

	private final Timer timer;
	private long startTime = 0;

	ProjectsTree(Frame frame, Config config) throws IOException {
		super(new DefaultMutableTreeNode());
		this.frame = frame;
		this.config = config;
		root = (DefaultMutableTreeNode) getModel().getRoot();
		loadProjects(root);
		expandAllNodes();
		setRootVisible(false);
		setBackground(config.getDefaultColor());
		setCellRenderer(getTreeCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		timer = createTimer();
	}
	
	private void loadProjects(DefaultMutableTreeNode root) throws IOException {
		//ProjectTree projectTree = new ProjectTree();
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
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(extractName(line));
			// String tooltip = extractTooltip(line);
			if (depth == 0) root.add(node);
			else nodeChain.get(depth - 1).add(node);
			nodeChain.add(node);
		}
		br.close();
	}
	
	private String extractName(String s) {
		if (s == null) return null;
		int left = s.indexOf('{');
		if (left == -1) return s;
		return s.substring(0, left).trim();
	}
	
//	private String extractTooltip(String s) {
//		if (s == null) return null;
//		int left = s.indexOf('{');
//		int right = s.lastIndexOf('}');
//		if (left == -1 || right == -1) return null;
//		return s.substring(left + 1, right).trim();
//	}
	
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
	
	private TreeCellRenderer getTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			public Color getBackgroundNonSelectionColor() {
				return config.getDefaultColor();
			}

			public Color getBackgroundSelectionColor() {
				return config.getActiveColor();
			}

			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				if (d.width < 100) d.width = 100;
				return d;
			}

			public Icon getLeafIcon() {
				return super.getLeafIcon();
			}
		};
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (node == null)
			return;
		TreeNode[] path = node.getPath();
		String[] projectPath = new String[path.length - 1];
		for (int i = 1; i < path.length; i++)
			projectPath[i-1] = ((DefaultMutableTreeNode) path[i]).getUserObject().toString();
		try {
			if (state == State.AUTOMATIC)
				state = State.RUNNING;
			startRecording(projectPath);
			timer.restart();
		} catch (Exception ex) {displayProblem(ex);}
	}

	public void startRecording(String[] projectPath) throws Exception {
		if (state != State.STOPPED) stopRecording();
		this.currentPojectPath = projectPath;
		startTime = System.currentTimeMillis();
		switchToActiveState(projectPath);
		new Thread() {
			public void run() {
				try {Thread.sleep(300);}
				catch (InterruptedException e) {e.printStackTrace();}
				//minimiseOrHide();
			}
		}.start();
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
		//unminimiseOrShow();
	}
	
	public void doPeriodicAction() {
		try {
			if (state == State.AUTOMATIC) {
				stopRecording();
			} else if (state == State.RUNNING) {
				stopRecording();
				startRecording(currentPojectPath);
				switchToSemiActiveState();
			}
		} catch (Exception ex) {displayProblem(ex);}
	}
	
	public void writeLogEntry(long startTime, long endTime) throws Exception {
		String startTimeS = df.format(new Date(startTime));
		String endTimeS = df.format(new Date(endTime));
		String entry = startTimeS + "," + endTimeS;
		for (String projectPathNode : currentPojectPath)
			entry += "," + projectPathNode;
		System.err.println(entry);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(config.getLogFilename(), true)));
		out.write(entry + newline);
		out.close();
	}
	
	public void switchToActiveState(String[] projectPath) {
		state = State.RUNNING;
		// runningButton.setBackground(activeColor);
	}
	
	public void switchToSemiActiveState() {
		state = State.AUTOMATIC;
		// TODO: set the selection to appropriate colour
	}

	public void switchToStoppedState() {
		state = State.STOPPED;
		this.setSelectionPath(null);
		// runningButton.setBackground(config.getDefaultColor());
	}

	public void minimiseOrHide() {
		if (config.getMinimise())
			frame.setExtendedState(Frame.ICONIFIED);
		else
			setVisible(false);
	}
	
	public void unminimiseOrShow() {
		if (config.getMinimise())
			frame.setExtendedState(Frame.NORMAL);
		else
			setVisible(true);		
	}

	public void displayProblem(Exception e) {
		JOptionPane.showMessageDialog(this, "A problem has occurred: " + e.getMessage());
	}
}
