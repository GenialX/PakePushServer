/**
 * Get Users To Push Email
 */
package me.pake.push.util.push;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.message.BackUsersForWishListMessage;
import me.pake.push.message.NearestUsersForWishListMessage;
import me.pake.push.model.DB;
import me.pake.push.util.Distance;

public class PushUser {
	
	
	
	/**
	 * 获取适合发送Email的愿望清单（创建）的用户数据
	 * 
	 * 1、构建SQL语句，拿到符合要求的用户数据。要求如下：
	 * 		(1)、email非空；
	 * 		(2)、存入到auto_loc的数据为autoLocActiveDayCount天内；
	 * 		(3)、符合邮件限制（email_receive表）的要求。
	 * 2、遍历（同时排除当前用户，非fromuserid），计算距离，将符合距离的用户放入数组中；
	 * 3、根据距离排序，返回数据。
	 * 
	 * 注意：目前usersCount无效
	 * 
	 * @param fromuserid 发起动作的用户ID
	 * @param lat 发起动作的用户的所在地理位置
	 * @param lon 同上
	 * @param distance 获取中的用户的最大限制距离
	 * @param usersCount 获取中的用户的最大限制数量
	 * @param autoLocActiveDayCount auto_loc表中的时间范围
	 * @param emailReceiveActiveHourCount email_receive表中的时间范围
	 * @param emailReceiveCount 规定时间范围内最大接收发送的次数
	 * @return ArrayList<PushWishListUserMessage>()
	 */
	public List<NearestUsersForWishListMessage> getNearestUsersForWishEmail(int fromuserid, double lat, double lon, double distance, 
								int usersCount, int autoLocActiveDayCount, 
								int emailReceiveActiveHourCount, int emailReceiveCount ) {
		List users = new ArrayList<NearestUsersForWishListMessage>();
		Connection conn = DB.getInstance().getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// databases operation...
		try {
			
			String sql = "SELECT auto_loc.k, auto_loc.Longitude, auto_loc.Latitude, user.email, "
					+ "email_receive.wcnt, email_receive.gcnt, email_receive.acnt "
					+ "FROM (auto_loc LEFT JOIN email_receive ON auto_loc.k=email_receive.k) "
					+ "LEFT JOIN user "
					+ "ON auto_loc.k=user.id "
					+ "WHERE user.email<>'' "
					+ "AND DATE_ADD(auto_loc.expires,INTERVAL ? DAY) > NOW() "
					+ "AND ( email_receive.createtime is null) "
					+ "OR DATE_ADD(email_receive.createtime,INTERVAL ? HOUR) < NOW() "
					+ "OR ( DATE_ADD(email_receive.createtime,INTERVAL ? HOUR) > NOW() AND email_receive.wcnt < ?) ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, autoLocActiveDayCount);
			pstmt.setInt(2, emailReceiveActiveHourCount);
			pstmt.setInt(3, emailReceiveActiveHourCount);
			pstmt.setInt(4, emailReceiveCount);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				// get rid of owner(fromuserid)
				if(fromuserid == rs.getInt("k")) continue;
				
				// get the distance 
				int userid 			= rs.getInt("k");
				double _lon 		= rs.getDouble("Longitude");
				double _lat 		= rs.getDouble("Latitude");
				double _distance 	= Distance.distance(lon, lat, _lon, _lat);
				String email 		= rs.getString("email");
				int acnt			= rs.getInt("acnt");
				int gcnt			= rs.getInt("gcnt");
				int wcnt			= rs.getInt("wcnt");
				
				if(_distance <= distance) {
					NearestUsersForWishListMessage u = new NearestUsersForWishListMessage();
					u.setUserid(userid);
					u.setDistance(_distance);
					u.setLon(_lon);
					u.setLat(_lat);
					u.setEmail(email);
					u.setAcnt(acnt);
					u.setGcnt(gcnt);
					u.setWcnt(wcnt);
					users.add(u);
				}
				
				// sort users distance var is smaller, the araylist index is smaller
				Collections.sort(users, new Comparator<NearestUsersForWishListMessage>() {
					public int compare(NearestUsersForWishListMessage o1, NearestUsersForWishListMessage o2) {
						return (int) (o1.getDistance() - o2.getDistance());
					}
				});
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs != null) {
					rs.close();
					rs = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return users;
	}
	
	/**
	 * 获取参与愿望清单回复的用户信息（排除发起动作的人）
	 * 
	 * @param fromuserid
	 * @param info_id
	 * @return List
	 */
	public List<BackUsersForWishListMessage> getBackUsersForWishEmail(int fromuserid, int info_id) {
		List users 				= new ArrayList<BackUsersForWishListMessage>();
		Connection conn 		= DB.getInstance().getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs			= null;
		try {
			String sql = "SELECT wishback.hfuser AS touserid, user.email "
					+ "FROM wishback "
					+ "LEFT JOIN user "
					+ "ON wishback.hfuser=user.id "
					+ "WHERE wishback.fbid=? " 
					+ "AND wishback.hfuser!=? "
					+ "AND wishback.hfuser!='admin' "
					+ "AND user.email<>'' "
					+ "GROUP BY wishback.hfuser "
					+ "UNION "
					+ "SELECT wishmsg.fbuser AS touserid, user.email "
					+ "FROM wishmsg "
					+ "LEFT JOIN user "
					+ "ON wishmsg.fbuser=user.id "
					+ "WHERE wishmsg.id=? "
					+ "AND wishmsg.fbuser!=? "
					+ "AND wishmsg.fbuser!='admin' "
					+ "AND user.email<>'' "
					+ "GROUP BY wishmsg.fbuser ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, info_id);
			pstmt.setInt(2, fromuserid);
			pstmt.setInt(3, info_id);
			pstmt.setInt(4, fromuserid);
//			System.out.println(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				BackUsersForWishListMessage user = new BackUsersForWishListMessage();
				int userid 		= rs.getInt("touserid");
				String email 	= rs.getString("email");
				user.setEmail(email);
				user.setUserid(userid);
				users.add(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try{
				if(rs != null) {
					rs.close();
					rs = null;
				}
				
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				
			} catch(SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return users;
	}
	
	/**
	 * 获取适合发送wechat的愿望清单（创建）的用户数据
	 * 
	 * 1、构建SQL语句，拿到符合要求的用户数据。要求如下：
	 * 		(1)、账户类型为WX,且openid非空；
	 * 		(2)、存入到auto_loc的数据为autoLocActiveDayCount天内；
	 * 		(3)、符合微信限制（wechat_receive表）的要求。
	 * 2、遍历（同时排除当前用户，非fromuserid），计算距离，将符合距离的用户放入数组中；
	 * 3、根据距离排序，返回数据。
	 * 
	 * 注意：目前usersCount无效
	 * 
	 * @param fromuserid 发起动作的用户ID
	 * @param lat 发起动作的用户的所在地理位置
	 * @param lon 同上
	 * @param distance 获取中的用户的最大限制距离
	 * @param usersCount 获取中的用户的最大限制数量
	 * @param autoLocActiveDayCount auto_loc表中的时间范围
	 * @param emailReceiveActiveHourCount email_receive表中的时间范围
	 * @param emailReceiveCount 规定时间范围内最大接收发送的次数
	 * @return ArrayList<PushWishListUserMessage>()
	 */
	public List<NearestUsersForWishListMessage> getNearestUsersForWishWechat(int fromuserid, double lat, double lon, double distance, 
			int usersCount, int autoLocActiveDayCount, 
			int emailReceiveActiveHourCount, int emailReceiveCount ) {
		List users = new ArrayList<NearestUsersForWishListMessage>();
		Connection conn = DB.getInstance().getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// databases operation...
		try {
			
			String sql = "SELECT auto_loc.k, auto_loc.Longitude, auto_loc.Latitude, user.openid, "
					+ "wechat_receive.wcnt, wechat_receive.gcnt, wechat_receive.acnt "
					+ "FROM (auto_loc LEFT JOIN wechat_receive ON auto_loc.k=wechat_receive.k) "
					+ "LEFT JOIN user "
					+ "ON auto_loc.k=user.id "
					+ "WHERE user.user_type='WX' "
					+ "AND user.openid<>'' "
					+ "AND DATE_ADD(auto_loc.expires,INTERVAL ? DAY) > NOW() "
					+ "AND ( wechat_receive.createtime is null) "
					+ "OR DATE_ADD(wechat_receive.createtime,INTERVAL ? HOUR) < NOW() "
					+ "OR ( DATE_ADD(wechat_receive.createtime,INTERVAL ? HOUR) > NOW() AND wechat_receive.wcnt < ?) ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, autoLocActiveDayCount);
			pstmt.setInt(2, emailReceiveActiveHourCount);
			pstmt.setInt(3, emailReceiveActiveHourCount);
			pstmt.setInt(4, emailReceiveCount);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				// get rid of owner(fromuserid)
				if(fromuserid == rs.getInt("k")) continue;
				
				// get the distance 
				int userid 			= rs.getInt("k");
				double _lon 		= rs.getDouble("Longitude");
				double _lat 		= rs.getDouble("Latitude");
				double _distance 	= Distance.distance(lon, lat, _lon, _lat);
				String openid 		= rs.getString("openid");
				int acnt			= rs.getInt("acnt");
				int gcnt			= rs.getInt("gcnt");
				int wcnt			= rs.getInt("wcnt");
				
				if(_distance <= distance) {
					NearestUsersForWishListMessage u = new NearestUsersForWishListMessage();
					u.setUserid(userid);
					u.setDistance(_distance);
					u.setLon(_lon);
					u.setLat(_lat);
					u.setOpenid(openid);
					u.setAcnt(acnt);
					u.setGcnt(gcnt);
					u.setWcnt(wcnt);
					users.add(u);
				}
				
				// sort users distance var is smaller, the araylist index is smaller
				Collections.sort(users, new Comparator<NearestUsersForWishListMessage>() {
					public int compare(NearestUsersForWishListMessage o1, NearestUsersForWishListMessage o2) {
						return (int) (o1.getDistance() - o2.getDistance());
					}
				});
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				if(rs != null) {
					rs.close();
					rs = null;
				}
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return users;
	}
	
	
	/**
	 * 获取参与愿望清单回复的用户信息（排除发起动作的人）
	 * 
	 * @param fromuserid
	 * @param info_id
	 * @return List
	 */
	public List<BackUsersForWishListMessage> getBackUsersForWishWechat(int fromuserid, int info_id) {
		List users 				= new ArrayList<BackUsersForWishListMessage>();
		Connection conn 		= DB.getInstance().getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs			= null;
		try {
			String sql = "SELECT wishback.hfuser AS touserid, user.openid "
					+ "FROM wishback "
					+ "LEFT JOIN user "
					+ "ON wishback.hfuser=user.id "
					+ "WHERE user.user_type='WX' "
					+ "AND wishback.fbid=? " 
					+ "AND wishback.hfuser!=? "
					+ "AND wishback.hfuser!='admin' "
					+ "AND user.openid<>'' "
					+ "GROUP BY wishback.hfuser "
					+ "UNION "
					+ "SELECT wishmsg.fbuser AS touserid, user.openid "
					+ "FROM wishmsg "
					+ "LEFT JOIN user "
					+ "ON wishmsg.fbuser=user.id "
					+ "WHERE user.user_type='WX' "
					+ "AND wishmsg.id=? "
					+ "AND wishmsg.fbuser!=? "
					+ "AND wishmsg.fbuser!='admin' "
					+ "AND user.openid<>'' "
					+ "GROUP BY wishmsg.fbuser ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, info_id);
			pstmt.setInt(2, fromuserid);
			pstmt.setInt(3, info_id);
			pstmt.setInt(4, fromuserid);
//			System.out.println(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				BackUsersForWishListMessage user = new BackUsersForWishListMessage();
				int userid 		= rs.getInt("touserid");
				String openid 	= rs.getString("openid");
				user.setOpenid(openid);
				user.setUserid(userid);
				users.add(user);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try{
				if(rs != null) {
					rs.close();
					rs = null;
				}
				
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				
			} catch(SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return users;
	}
	
	
	
}
