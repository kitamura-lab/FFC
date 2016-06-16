package kitamura.ffc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
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
	JLabel jl;

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
		//p.setLayout(new BorderLayout());
		jl = new JLabel("");	
		jl.setPreferredSize(new Dimension(500, 20));
		p.add(jl, BorderLayout.NORTH);
		p.add(scrollPane, BorderLayout.CENTER);

		//getContentPane().add(p, BorderLayout.CENTER);

		getContentPane().add(p);

		db = new Database();

		addVideo();
		showStatus();

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
			String header="";
			if(v.get(i).watch==0) header="[未]"; else header = "[済]";
			root.add(new DefaultMutableTreeNode(header+v.get(i).name));
		}
	}
	
	void showStatus(){
		ArrayList<Video> v = db.getVideo();
		int count=0;
		for (int i = 0; i < v.size(); i++) {
			if(v.get(i).watch==1) count++;
		}
		jl.setText(""+v.size()+"本中"+count+"本見ました！");
		
	}

	public void valueChanged(TreeSelectionEvent e) {
		try {
			TreePath path = tree.getSelectionPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			String file = node.toString().substring(3);
			System.out.println(file);
			// Runtime r = Runtime.getRuntime();

			// Process process = r.exec("C:\\Program Files
			// (x86)\\VideoLAN\\VLC\\vlc.exe "+node);
			
			ProcessBuilder pb = new ProcessBuilder("C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe", file);
			Process process = pb.start();
			int ret = process.waitFor();
			
			node.setUserObject(new DefaultMutableTreeNode("[済]"+file));
			db.setVideo(file,1);
			showStatus();
			tree.clearSelection();
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		

	}

}