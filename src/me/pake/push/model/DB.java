package me.pake.push.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.pake.push.conf.DBConf;

final public class DB {
	
	private Connection conn = null;
	
	private static class SingleModel {
		private static DB instance = new DB();
	}
	
	private DB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.conn = DriverManager.getConnection("jdbc:mysql://" + DBConf.DB_HOST + ":" + DBConf.DB_PORT + "/" + DBConf.DB_NAME + "?user=" + DBConf.DB_USER + "&password=" + DBConf.DB_PASS + "&zeroDateTimeBehavior=convertToNull");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static DB getInstance() {
		return SingleModel.instance;
	}
	
	public Connection getConnection() {
		return this.conn;
	}
	
}
