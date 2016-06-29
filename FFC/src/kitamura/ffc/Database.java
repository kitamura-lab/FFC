package kitamura.ffc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {
	Connection connection = null;

	public static void main(String[] args) {
		Database db = new Database();
		db.initialize();
		db.getVideo();
		db.close();
	}

	Database() {
		Statement statement = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:video.db");
			statement = connection.createStatement();
			statement.executeUpdate("create table if not exists video(date, time, name, watch)");
			statement.executeUpdate("create table if not exists watchtime(date,time, watchtime)");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void initialize() {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("drop table if exists video");
			statement.executeUpdate("create table video(date, time, name, watch)");
			statement.executeUpdate("drop table if exists watchtime");
			statement.executeUpdate("create table watchtime(date,time, watchtime)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void putVideo(String video) {
		if (video.indexOf(".MP4") < 0)
			return;
		PreparedStatement ps = null;
		try {			
			ps = connection.prepareStatement("select * from video where name=?");
			ps.setString(1, video);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// System.out.println("DUPLICATED:"+rs.getString(1)+rs.getString(2));
				return;
			}
			ps = connection.prepareStatement("insert into video values(date('now'),time('now'),?,0)");
			ps.setString(1, video);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void setVideo(String video, int watch) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("update video set watch=? where name=?");
			ps.setInt(1, watch);
			ps.setString(2, video);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	int getWatchTime() {
		Statement statement = null;
		int time = 0;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select sum(watchtime) from watchtime where date >= date('now', '-7 days')");
			while (rs.next()) {
				//System.out.println("TIME:"+rs.getString(1));				
				time = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return time;
	}

	void putWatchTime(int watchtime) {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("insert into watchtime values(date('now'),time('now'),?)");
			ps.setInt(1, watchtime);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	ArrayList<Video> getVideo() {
		ArrayList<Video> video = new ArrayList<Video>();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			String sql = "select * from video";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				Video v = new Video();
				v.name = rs.getString(3);
				v.watch = rs.getInt(4);
				video.add(v);
				// System.out.println(rs.getString(3)+rs.getInt(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return video;
	}
}
