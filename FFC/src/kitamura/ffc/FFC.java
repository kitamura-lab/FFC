package kitamura.ffc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ファイルをモバイルデバイスにコピーするプログラム<br>
 * <br>
 * 設定はFFCConfig.csvファイルに記述します．<br>
 * HOME: ソースフォルダのパス．（ここにポジションフォルダを置きます．）<br>
 * DEST: デスティネーションフォルダのパス．（FFCの置かれているフォルダからの相対パスです．）<br>
 * ポジション：0または1.（HOMEに置かれたポジションフォルダを1であればコピーする，0であればコピーしない．）<br>
 * <br>
 * 実行すると日付のついたフォルダをDESTに生成し，その中に選択されたポジションフォルダをコピーします．同じ名前のファイルが存在する場合はコピーしません．<br>
 * ウィンドウを閉じると「安全な取り外し」が行われます．<br>
 * HOMEにSYSTEMフォルダを置くと，その中のファイルがDESTにコピーされます．
 * 同じ名前のファイルが存在する場合は，タイムスタンプが新しければコピーされます．FFC.jarのバージョンアップに利用することができます．<br>
 * @author Yasuhiko Kitamura
 */
public class FFC extends JFrame implements WindowListener {

	private static final long serialVersionUID = 1L;

	static String version = "2.0"; // 2016/05/19
	final String logfile = "FFC.log";
	private Logger logger = null;

	JLabel label2, label3;
	String home = "C:\\Users\\kitamura\\Documents\\FIGHTERS";
	String dest = ".\\FIGHTERS1";

	class Folder {
		String name;
		int flag;
	}

	Folder folder[] = new Folder[100];

	/**
	 * @param args 使用しない
	 */
	public static void main(String[] args) {

		new FFC("FFC" + version);
	}

	FFC(String title) {
		/* ログファイルの初期化 */
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

		// フレームの初期化
		setTitle(title);
		setBounds(100, 100, 600, 100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		setVisible(true);

		JPanel p = new JPanel();
		JLabel label1 = new JLabel("Status: ");
		label2 = new JLabel();
		label3 = new JLabel();
		p.add(label1);
		p.add(label2);
		p.add(label3);

		Container contentPane = getContentPane();
		contentPane.add(p, BorderLayout.CENTER);

		// フォルダ情報の初期化
		for (int i = 0; i < folder.length; i++) {
			folder[i] = new Folder();
			folder[i].flag = 0;
		}

		// 設定ファイルの読込
		setting();

		// ビデオフォルダの生成
		File f = new File(dest);
		if (!f.exists())
			f.mkdir();
		f = new File(dest + "\\" + monthday());
		if (!f.exists())
			f.mkdir();

		// ビデオのコピー
		for (int i = 0; i < folder.length; i++) {
			if (folder[i].flag == 1)
				copyTransfer(home + "\\" + folder[i].name, dest + "\\" + monthday() + "\\" + folder[i].name);
		}

		// システムファイルのコピー
		copyTransfer(home + "\\SYSTEM", ".\\");

		label2.setText("END");
	}

	void setting() {
		try {
			// ファイルを読み込む
			FileReader fr = new FileReader("FFCConfig.csv");
			BufferedReader br = new BufferedReader(fr);

			// 読み込んだファイルを１行ずつ処理する
			String line;
			StringTokenizer token;

			int i = 0;
			while ((line = br.readLine()) != null) {
				// 区切り文字","で分割する
				token = new StringTokenizer(line, ",");
				String id = token.nextToken();
				if (id.equals("HOME")) {
					Pattern p = Pattern.compile("\\\\");
					Matcher m = p.matcher(token.nextToken());
					home = m.replaceAll("\\\\\\\\");
				} else if (id.equals("DEST")) {
					dest = ".\\" + token.nextToken();
				} else {
					folder[i].name = id;
					folder[i].flag = Integer.parseInt(token.nextToken());
					i++;
				}
			}
			// 終了処理
			br.close();

		} catch (Exception ex) {
			// 例外発生時処理
			ex.printStackTrace();
			logger.log(Level.SEVERE, "ERROR:", ex);
			label3.setText(ex.toString());

		}
	}

	String monthday() {
		String sMonth;
		String sDay;

		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);

		if (month < 10)
			sMonth = "0" + month;
		else
			sMonth = "" + month;
		if (day < 10)
			sDay = "0" + day;
		else
			sDay = "" + day;

		return sMonth + sDay;
	}

	void copyTransfer(String srcPath, String destPath) {

		copyTransfer(new File(srcPath), new File(destPath));

	}

	void copyTransfer(File src, File dest) {

		if (src.isDirectory()) {
			// ディレクトリがない場合、作成
			if (!dest.exists()) {
				dest.mkdir();
			}

			String[] files = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyTransfer(srcFile, destFile);
			}
		} else {
			// ファイルのコピー
			// ファイルが存在すればスキップ
			if (dest.exists()) {
				Long timeDest = dest.lastModified();
				Long timeSrc = src.lastModified();
				//System.out.println(src.getName() +":"+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(timeSrc) + ":"
				//		+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(timeDest));

				if (timeDest > timeSrc) {
					label2.setText("SKIP: " + src.getAbsolutePath().toString());
					//System.out.println("SKIP: " + src.getAbsolutePath().toString());
					return;
				}
			}
			try {
				FileInputStream fis = new FileInputStream(src);
				FileChannel srcChannel = fis.getChannel();

				FileOutputStream fos = new FileOutputStream(dest);
				FileChannel destChannel = fos.getChannel();
				srcChannel.transferTo(0, srcChannel.size(), destChannel);
				srcChannel.close();
				destChannel.close();
				fis.close();
				fos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.log(Level.SEVERE, "ERROR:", ex);
				label3.setText(ex.toString());
				;
			}
			label2.setText(src.getAbsolutePath().toString());
		}
	}

	public void windowClosing(WindowEvent e) {
		//System.out.println("BYE");
		try {
			// 安全な取り外しの実行
			Runtime.getRuntime().exec("UnplugDrive.exe");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.log(Level.SEVERE, "ERROR:", ex);
		}
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
