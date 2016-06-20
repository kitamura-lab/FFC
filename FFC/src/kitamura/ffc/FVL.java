package kitamura.ffc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FVL extends JFrame implements TreeSelectionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static String version = "1.1";

	Database db;
	DefaultMutableTreeNode root;
	DefaultTreeModel model;
	JTree tree;
	JLabel label;

	public static void main(String[] args) {
		new FVL("FVL " + "ver." + version);
	}

	FVL(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10, 10, 600, 600);
		setTitle(title);
		setVisible(true);

		root = new DefaultMutableTreeNode("Fighters");
		tree = new JTree(root);
		model = (DefaultTreeModel) tree.getModel();
		tree.addTreeSelectionListener(this);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(tree);
		scrollPane.setPreferredSize(new Dimension(500, 500));

		JPanel panel = new JPanel();
		label = new JLabel("");
		label.setPreferredSize(new Dimension(500, 20));
		panel.add(label, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(panel);

		db = new Database();

		addVideo();
		tree.expandRow(0);
		int row = tree.getRowCount() - 1;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}

		showStatus();
	}

	void addVideo() {
		ArrayList<Video> v = db.getVideo();
		for (int i = 0; i < v.size(); i++) {
			String header = "";
			if (v.get(i).watch == 0)
				header = "[未]";
			else
				header = "[済]";
			String path = v.get(i).name;
			if (path.indexOf("\\") < 0)
				continue;
			path = path.substring(2);
			// System.out.println(path.substring(0,path.indexOf("\\")));
			root.setUserObject(new DefaultMutableTreeNode(path.substring(0, path.indexOf("\\"))));
			DefaultMutableTreeNode node = root;
			path = path.substring(path.indexOf("\\") + 1);
			while (path.indexOf("\\") > 0) {
				String nodeName = path.substring(0, path.indexOf("\\"));
				// System.out.println(nodeName);
				DefaultMutableTreeNode node1 = null;
				for (int j = 0; j < node.getChildCount(); j++) {
					// System.out.println("CHILD:"+node.getChildAt(j));
					if (((DefaultMutableTreeNode) node.getChildAt(j)).getUserObject().equals(nodeName)) {
						// System.out.println("FOUND"+nodeName);
						node1 = (DefaultMutableTreeNode) node.getChildAt(j);
						break;
					}
				}
				if (node1 == null)
					node1 = new DefaultMutableTreeNode(nodeName);
				node.add(node1);
				node = node1;
				path = path.substring(path.indexOf("\\") + 1);
			}
			// System.out.println(path);
			node.add(new DefaultMutableTreeNode(header + path));
			// System.out.println(path.substring(path.indexOf("\\")+1));
		}
	}

	void showStatus() {
		ArrayList<Video> v = db.getVideo();
		int count = 0;
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).watch == 1)
				count++;
		}
		int watchtime = db.getWatchTime();

		label.setText(watchtime + "秒で" + v.size() + "本中" + count + "本見ました！");
	}

	public void valueChanged(TreeSelectionEvent e) {
		try {
			TreePath path = tree.getSelectionPath();
			if(path==null) return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			DefaultMutableTreeNode node0 = node;
			String headerfile = node.toString();
			String file = headerfile.substring(headerfile.indexOf("]") + 1);
			// System.out.println(file);
			while (node.getParent() != null) {
				file = node.getParent().toString() + "\\" + file;
				node = (DefaultMutableTreeNode) node.getParent();
			}
			file = ".\\" + file;
			// System.out.println(file);

			String os = System.getProperty("os.name").toLowerCase();
			//System.out.println(os);
			String vlcPath="";
			if(os.startsWith("windows")) vlcPath="C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
			if(os.startsWith("mac")) vlcPath="open /Applications/VLC.app";
			
			long start = System.currentTimeMillis();
			ProcessBuilder pb = new ProcessBuilder(vlcPath, file);
			Process process = pb.start();
			process.waitFor();
			long end = System.currentTimeMillis();
			// System.out.println((end - start)/1000 + "秒");
			db.putWatchTime((int) (end - start) / 1000);

			checkVideo(node0);

			tree.clearSelection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void checkVideo(DefaultMutableTreeNode node) {
		// System.out.println(node.toString() + node.getChildCount());
		if (node.getChildCount() == 0) {

			String file = "[済]" + node.toString().substring(3);
			// System.out.println(node.toString() + ":" + file);
			node.setUserObject(new DefaultMutableTreeNode(file));
			model.nodeChanged(node);

			file = node.toString().substring(3);
			while (node.getParent() != null) {
				file = node.getParent().toString() + "\\" + file;
				node = (DefaultMutableTreeNode) node.getParent();
			}
			file = ".\\" + file;
			// System.out.println("DB:"+file);
			db.setVideo(file, 1);
			showStatus();

			return;
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			checkVideo(child);
		}
	}

	public void windowClosing(WindowEvent e) {
		db.close();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
