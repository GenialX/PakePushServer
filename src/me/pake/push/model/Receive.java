package me.pake.push.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.conf.ServerConf;

abstract class Receive implements ReceiveInterface{
	
	protected String tableName = "";
	
	public Receive() { }
	
	/**
	 * update the cnt field
	 * 
	 * @version 0.0.2 Set cnt to 0, while it is bigger than the specific count
	 * 
	 * @param userid
	 * @param type ReceiveInterface INT
	 * @param cnt
	 * @return
	 * 
	 */
	public boolean setCnt(int userid, String type, int cnt) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = DB.getInstance().getConnection();
			pstmt = conn.prepareStatement("UPDATE " + this.tableName + " SET " + type + "=?, createtime=? WHERE k=?");
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String time = df.format(new Date());
			if(cnt > ServerConf.RECEIVE_ACCOUNT ) {
				cnt = 0;
			}
			pstmt.setInt(1, cnt);
			pstmt.setString(2, time);
			pstmt.setInt(3, userid);
			if(pstmt.executeUpdate() != 0) {
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Update " + this.tableName + " successfully, set " + type + " to " + cnt);
				return true;
			} else {
				// create one item
				if(pstmt != null) {
					pstmt = null;
				}
				pstmt = conn.prepareStatement("INSERT INTO " + this.tableName + " (k, wcnt, acnt, gcnt,createtime) VALUES (?,?,?,?,?)");
				pstmt.setInt(1, userid);
				if(type == Receive.WCNT) {
					pstmt.setInt(2, cnt);
				} else {
					pstmt.setInt(2, 0);
				}
				if(type == Receive.ACNT) {
					pstmt.setInt(3, cnt);
				} else {
					pstmt.setInt(3, 0);
				}				
				if(type == Receive.GCNT) {
					pstmt.setInt(4, cnt);
				} else {
					pstmt.setInt(4, 0);
				}
				pstmt.setString(5, time);
				if(pstmt.executeUpdate() != 0) {
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Insert " + this.tableName + " successfully, initial " + type + " to " + cnt);
					if(pstmt != null) {
						pstmt.close();
						pstmt = null;
					}
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
}
