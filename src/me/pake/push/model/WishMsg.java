package me.pake.push.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishMsg {
	private String tableName = "wishmsg";
	
	public List getWishMsgById(int id) {
		Connection conn = DB.getInstance().getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map> wishMsg = new ArrayList<Map>();
		Map<String,String> wishMsgMap = null;
		
		try {
			stmt = conn.prepareStatement("select * from wishmsg where id=?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				wishMsgMap = new HashMap<String,String>();
				for(int i = 1; i <= columnCount; i++) {
					wishMsgMap.put(rsmd.getColumnName(i), rs.getString(i));
				}
				wishMsg.add(wishMsgMap);
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
		
		return wishMsg;
	}
}
