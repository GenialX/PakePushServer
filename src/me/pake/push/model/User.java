package me.pake.push.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
	private String tableName = "user";
	
	public User() {}

	public List<Map<String, String>> getUserByID(int id) {
		Connection conn = DB.getInstance().getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> user = new ArrayList<Map<String, String>>();
		Map<String,String> userMap = null;
		
		try {
			stmt = conn.prepareStatement("select * from " + this.tableName + " where id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				userMap = new HashMap<String,String>();
				for(int i = 1; i <= columnCount; i++) {
					userMap.put(rsmd.getColumnName(i), rs.getString(i));
				}
				user.add(userMap);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( rs != null ) {
					rs.close();rs = null;
				}
				if(stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return user;
	}
	
}
