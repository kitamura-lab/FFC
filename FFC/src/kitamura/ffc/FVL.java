package kitamura.ffc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class FVL extends JFrame implements TreeSelectionListener {

	static String version = "0.1";

	Database db;
	DefaultMutableTreeNode root;
	JTree tree;

	public static void main(String[] args) {
		new FVL("FVL " + "ver." + version);
	}

	FVL(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(10, 10, 600, 600);
		setTitle(title);

		root = new DefaultMutableTreeNode("Fighters");
		// DefaultMutableTreeNode swing = new DefaultMutableTreeNode("Swing");
		tree = new JTree(root);
		tree.addTreeSelectionListener(this);

		// root.add(swing);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(tree);
		scrollPane.setPreferredSize(new Dimension(500, 500));

		JPanel p = new JPanel();
		p.add(scrollPane);

		getContentPane().add(p, BorderLayout.CENTER);

		db = new Database();

		addVideo();

		setVisible(true);
		// tree.setRootVisible(true);

		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}

	}

	void addVideo() {
		ArrayList<Video> v = db.getVideo();
		for (int i = 0; i < v.size(); i++) {
			root.add(new DefaultMutableTreeNode(v.get(i).name));
		}
	}

	public void valueChanged(TreeSelectionEvent e) {
		try {
			TreePath path = tree.getSelectionPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			System.out.println(node);
			// Runtime r = Runtime.getRuntime();

			// Process process = r.exec("C:\\Program Files
			// (x86)\\VideoLAN\\VLC\\vlc.exe "+node);
			ProcessBuilder pb = new ProcessBuilder("C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe", node.toString());
			Process process = pb.start();
			int ret = process.waitFor();
			tree.clearSelection();
		} catch (Exception ex) {
			//ex.printStackTrace();
		}

	}

}
