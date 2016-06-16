package kitamura.ffc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {
	Connection connection = null;

	public static void main(String[] args) {
		Database db = new Database();
		db.initialize();
		//db.putVideo("abc");
		db.getVideo();
		db.close();
	}

	Database() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:video.db");
			Statement statement = connection.createStatement();
			statement.executeUpdate("create table video(name, watch)");
		} catch (SQLException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void initialize(){
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("drop table if exists video");
			statement.executeUpdate("create table video(name, watch)");
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
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from video where name=\""+video+"\"");
			while (rs.next()) {
				System.out.println("DUPLICATED:"+rs.getString(1)+rs.getString(2));
				return;
			}
			
			statement.executeUpdate("insert into video values(\"" + video + "\",0)");
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
	
	void setVideo(String video, int watch){
		try{
			Statement statement = connection.createStatement();
			statement.executeUpdate("delete from video where name=\""+video+"\"");
			statement.executeUpdate("insert into video values(\"" + video + "\","+watch+")");
		}
		catch(Exception e){
			e.printStackTrace();
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
				v.name = rs.getString(1);
				v.watch = rs.getInt(2);
				video.add(v);
				System.out.println(rs.getString(1)+rs.getString(2));
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
