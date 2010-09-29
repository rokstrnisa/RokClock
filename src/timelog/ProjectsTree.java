package timelog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;

@SuppressWarnings("serial")
class ProjectsTree extends JTree implements TimeLog {
	class ProjectNode extends DefaultMutableTreeNode {
		  private String tooltip;

		  public ProjectNode(String caption, String tooltip) {
		    super(caption);
		    this.tooltip = tooltip;
		  }

		  public String getTooltip() {
		    return tooltip;
		  }
	}
	
	private final String newline = System.getProperty("line.separator");
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private final Frame frame;
	private final Config config;
	private final DefaultMutableTreeNode root;
	private PopupMenu popupMenu;
	private TreePath lastRightClickedPath;
	
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
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				switch (e.getButton()) {
					case MouseEvent.BUTTON1: onLeftMouseClick(); break;
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
				lastRightClickedPath.getLastPathComponent();
				// TODO: delete node, save projects
			}			
		});
		MenuItem addChildMI = new MenuItem("Add child to");
		addChildMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: add child node, save projects
			}			
		});
		popupMenu.add(deleteMI);
		popupMenu.add(addChildMI);
		return popupMenu;	
	}
	
	private void loadProjects(DefaultMutableTreeNode root) throws IOException {
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
	
	private TreeCellRenderer getTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			public Color getBackgroundNonSelectionColor() {
				return config.getDefaultColor();
			}

			public Color getBackgroundSelectionColor() {
				return getCurrentSelectionColor();
			}

			public Color getBorderSelectionColor() {
				return null;
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
	
	private void onLeftMouseClick() {
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
			minimiseOrHide();
			timer.restart();
		} catch (Exception ex) {displayProblem(ex);}
	}
	
	private void onRightMouseClick(MouseEvent e) {
		lastRightClickedPath = getPathForLocation(e.getX(), e.getY());
		popupMenu.show(frame, e.getX(), e.getY());
	}

	public void startRecording(String[] projectPath) throws Exception {
		if (state != State.STOPPED) stopRecording();
		this.currentPojectPath = projectPath;
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
				startRecording(currentPojectPath);
				switchToSemiActiveState();
			}
		} catch (Exception ex) {displayProblem(ex);}
		unminimiseOrShow();
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
	
	private Color getCurrentSelectionColor() {
		switch (state) {
			case AUTOMATIC: return config.getSemiActiveColor();
			case RUNNING: return config.getActiveColor();
			case STOPPED:
			default: return config.getDefaultColor();
		}
	}
	
	public void switchToActiveState(String[] projectPath) {
		state = State.RUNNING;
		repaint();
	}
	
	public void switchToSemiActiveState() {
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
				try {Thread.sleep(300);}
				catch (InterruptedException e) {e.printStackTrace();}
				if (config.getMinimise())
					frame.setExtendedState(Frame.ICONIFIED);
				else
					setVisible(false);
			}
		}.start();
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
