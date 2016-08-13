package kitamura.ffc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class FVL3 extends JFrame implements TreeSelectionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static String version = "3.0";
	final String logfile = "FVL.log";
	private Logger logger = null;

	Database db;
	DefaultMutableTreeNode root;
	DefaultTreeModel model;
	JTree tree;
	JLabel label,errlabel;

	String delimiter = null;
	String os = System.getProperty("os.name").toLowerCase();
	String vlcPath = null;

	public static void main(String[] args) {
		new FVL3("FVL " + "ver." + version);
	}

	FVL3(String title) {
		// System.out.println("Start");
		logger = Logger.getLogger(this.getClass().getName());
		try {
			FileHandler fh = new FileHandler(logfile);
			fh.setFormatter(new java.util.logging.SimpleFormatter());
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
			// logger.log(Level.SEVERE, "ERROR:", e);
		}
		logger.setLevel(Level.CONFIG);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10, 10, 600, 600);
		setTitle(title);
		setVisible(true);

		root = new DefaultMutableTreeNode(".");
		tree = new JTree(root);
		model = (DefaultTreeModel) tree.getModel();
		tree.addTreeSelectionListener(this);
		TreeWillExpandListener tel = new ExpandListener();
		tree.addTreeWillExpandListener(tel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(tree);
		scrollPane.setPreferredSize(new Dimension(500, 500));

		JPanel panel = new JPanel();
		label = new JLabel("");
		label.setPreferredSize(new Dimension(500, 20));
		errlabel = new JLabel("");
		errlabel.setPreferredSize(new Dimension(500, 20));
		panel.add(label, BorderLayout.NORTH);
		panel.add(errlabel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(panel);

		if (os.startsWith("windows")) {
			delimiter = "\\";
			File vlc = new File("C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe");
			if (vlc.exists())
				vlcPath = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";

			if (vlcPath == null) {
				errlabel.setText("VLCを探しています．");
				vlcPath = findVlc(new File("C:\\"));
				if (vlcPath != null)
					logger.log(Level.INFO, vlcPath);
			}

			if (vlcPath == null) {
				errlabel.setText("VLCがインストールされていません．");
			}
		}
		if (os.startsWith("mac")) {
			delimiter = "/";
			vlcPath = "/Applications/VLC.app";
		}

		db = new Database();

		if (vlcPath != null) {
			root.setUserObject(new DefaultMutableTreeNode("."));
			addVideo(new File("."), root, false);
			tree.expandRow(0);
			showStatus();
		}
	}

	String findVlc(File src) {
		String path = null;

		// System.out.println(src);

		if (src.isDirectory()) {
			String[] files = src.list();
			if (files == null)
				return null;
			for (String file : files) {
				File srcFile = new File(src, file);
				path = findVlc(srcFile);
				if (path != null)
					return path;
			}
		} else {

			String filename = src.getPath().toString();
			if (filename.endsWith("vlc.exe"))
				return src.getPath().toString();
		}
		return null;
	}

	void addVideo(File src, DefaultMutableTreeNode node, boolean cont) {

		if (src.isDirectory()) {
			String[] files = src.list();
			for (String file : files) {

				for (int i = 0; i < node.getChildCount(); i++) {
					//System.out.println(node.getChildAt(i) + ":" + file);
					if (node.getChildAt(i).toString().equals(file)) {
						//System.out.println("MATCH:" + node.getChildAt(i) + ":" + file);
						return;
					}
				}

				File srcFile = new File(src, file);
				if (srcFile.isDirectory()) {
					DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(file);
					node.add(node1);
					if (cont) {
						addVideo(srcFile, node1, false);
					}
					// System.out.println(srcFile.toString());
				} else if (checkSuffix(file)) {
					node.add(new DefaultMutableTreeNode(file));
					// System.out.println(srcFile.toString());
				}
			}
		} else {
			node.add(new DefaultMutableTreeNode(src));
		}

		return;
	}

	void showStatus() {
		int watchtime = db.getWatchTime();
		// label.setText(watchtime + "秒で" + v.size() + "本中" + count + "本見ました！");
		label.setText("この1週間の視聴時間： " + watchtime + " 秒");
	}

	boolean checkSuffix(String name) {
		if (name.endsWith(".MP4"))
			return true;
		if (name.endsWith(".mp4"))
			return true;
		if (name.endsWith(".MOV"))
			return true;
		if (name.endsWith(".mov"))
			return true;
		if (name.endsWith(".JPG"))
			return true;
		if (name.endsWith(".jpg"))
			return true;
		if (name.endsWith(".MOD"))
			return true;
		if (name.endsWith(".mod"))
			return true;
		if (name.endsWith(".MPG"))
			return true;
		if (name.endsWith(".mpg"))
			return true;
		return false;
	}

	public void valueChanged(TreeSelectionEvent e) {

		try {

			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			//DefaultMutableTreeNode node0 = node;
			String file = node.toString();
			//String file = headerfile.substring(headerfile.indexOf("]") + 1);
			// System.out.println(file);
			while (node.getParent() != null) {
				file = node.getParent().toString() + delimiter + file;
				node = (DefaultMutableTreeNode) node.getParent();
			}

			long start = System.currentTimeMillis();

			ProcessBuilder pb = null;
			if (os.startsWith("windows"))
				pb = new ProcessBuilder(vlcPath, file);
			if (os.startsWith("mac")) {
				pb = new ProcessBuilder("open", "-W", "-a", vlcPath, file);
			}
			Process process = pb.start();

			InputStream is = process.getErrorStream();
			try {
				while (is.read() >= 0) {
					//エラーメッセージ読み飛ばし
				}
			} finally {
				is.close();
			}

			process.waitFor();

			long end = System.currentTimeMillis();
			// System.out.println(""+end+":"+start+":"+(end-start));
			db.putWatchTime((int) (end - start) / 1000);

			showStatus();
			tree.clearSelection();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE, "ERROR:", ex);
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

	class ExpandListener implements TreeWillExpandListener {

		// ツリーを開く際に呼ばれる
		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

			TreePath path = event.getPath();
			TreeNode obj = (TreeNode) path.getLastPathComponent(); // 一番末端の(今回開こうとしている)ノード
			String file = "";
			for (Object o : path.getPath()) {
				file = file + delimiter + o;
				//System.out.println(obj);
			}
			file = file.substring(1);
			for (int i = 0; i < obj.getChildCount(); i++) {
				//System.out.println(obj.getChildAt(i));
				File f = new File(file + delimiter + obj.getChildAt(i));
				if (f.isDirectory())
					addVideo(f, (DefaultMutableTreeNode) obj.getChildAt(i), false);
			}
		}

		// ツリーを閉じる際に呼ばれる
		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		}
	}
}
