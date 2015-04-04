package me.pake.push.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.pake.push.util.Log;

public class Model {
	
	private String order 			= "id desc";
	private String where 			= "1";
	private String limit 			= "0,10";
	private String field			= "*";
	private Connection conn  		= null;
	private PreparedStatement pstmt = null;
	private Statement stmt			= null;
	private ResultSet rs          	= null;
	
	protected String tableName		= "";
	
	public Model() {
		this.conn = DB.getInstance().getConnection();
	}
	
	public Model(String tableName) {
		this.conn = DB.getInstance().getConnection();
		this.tableName = tableName;
	}
	
	public List<Map<String, String>> select() {
		List<Map<String, String>> result 	= new ArrayList<Map<String, String>>();
		Map<String,String> map 				= null;
		try {
			this.pstmt 	= this.conn.prepareStatement("select " + this.field + " from " + this.tableName + " where " + this.where + " order by " + this.order + " limit " + this.limit);
			this.rs 	= this.pstmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				map = new HashMap<String,String>();
				for(int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnName(i), this.rs.getString(i));
				}
				result.add(map);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( rs != null ) {
					rs.close();rs = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public Map<String, String> find() {
		Map<String,String> result 				= new HashMap<String,String>();
		
		try {
			this.pstmt 	= this.conn.prepareStatement("select " + this.field + " from " + this.tableName + " where " + this.where + " order by " + this.order + " limit " + this.limit);
			this.rs 	= this.pstmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				for(int i = 1; i <= columnCount; i++) {
					result.put(rsmd.getColumnName(i), this.rs.getString(i));
				}
				break;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( rs != null ) {
					rs.close();rs = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public int add(String sql) {
		int insertID = 0;
		try {
			this.stmt = this.conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			this.stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = this.stmt.getGeneratedKeys(); 
		    if (rs.next()) {
	           insertID = rs.getInt(1); 
	       }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return insertID;
	}
	
	public boolean save(String sql) {	
		int rs = 0;
		try {
			this.pstmt 	= this.conn.prepareStatement(sql);
			rs			= this.pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(rs > 0) return true;
		return false;
	}
	
	public boolean delete() {
		List<Map<String, String>> result 	= new ArrayList<Map<String, String>>();
		Map<String,String> map 				= null;
		boolean rs = false;
		try {
			this.pstmt 	= this.conn.prepareStatement("delete from " + this.tableName + " where " + this.where );
			int _rs = this.pstmt.executeUpdate();
			if(_rs > 0) rs = true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return rs;
	}
	
	public int count() {
		List<Map<String, String>> result 	= new ArrayList<Map<String, String>>();
		Map<String,String> map 				= null;
		int count = 0;
		try {
			this.pstmt 	= this.conn.prepareStatement("select count(*) from " + this.tableName + " where " + this.where );
			this.rs 	= this.pstmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			
			while(rs.next()) {
				for(int i = 1; i <= columnCount; i++) {
					count = this.rs.getInt(i);
				}
				break;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if( rs != null ) {
					rs.close();rs = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return count;
	}
	
	public Model order(String order) {
		this.order = order;
		return this;
	}
	
	public Model where(String where) {
		this.where = where;
		return this;
	}
	
	public Model limit(String limit) {
		this.limit = limit;
		return this;
	}

}
