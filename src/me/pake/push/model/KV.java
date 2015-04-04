package me.pake.push.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.crypto.Data;

import org.json.JSONException;
import org.json.JSONObject;

import me.pake.push.conf.WechatConf;
import me.pake.push.util.HttpGet;
import me.pake.push.util.WechatUtil;

/**
 * Database model for kv table.
 *
 */
public class KV {
	
	public final static String ACCESS_TOKEN = "access_token";
	public final static String APPID		  = "appid";
	public final static String APPSECRET    = "appsecret";
	
	private String tableName = "kv";
	private Connection conn = null;
	
	public KV() {
		this.conn = DB.getInstance().getConnection();
	}
	
	/**
	 * Get the value according to the key.
	 * 
	 * @param key it's the static string from this class.
	 * @return
	 */
	public String get(String key) {
		System.setProperty("user.timezone","Asia/Shanghai");
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String k = key;
		String v = "";
		String expires = "";
		int noexpire = 0;
		
		try {
			stmt = this.conn.prepareStatement("select * from " + this.tableName + " where k=?");
			stmt.setString(1, key);
			rs = stmt.executeQuery();
			
			if(rs.next()) {
				v = rs.getString("v");
				expires = rs.getString("expires");
				noexpire = rs.getInt("noexpire");
			} else {
				throw new Exception("There is not the k: " + key);
			}
			
			// If 'noexpire' field equal 1, return the "v" field
			if(noexpire == 1) {
				return v;
			} else {
				
				SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = simpleDataFormat.parse(expires);
				long expiresTime = date.getTime();
				long nowTime = new Date().getTime();
				
				if( nowTime < expiresTime ) {
					return v;
				} else {
					switch(key) {
						case KV.ACCESS_TOKEN: 
							String access_token = WechatUtil.getAccessToken();
							this.set(key, access_token);
							return access_token;
						default:
							return "";
					}
				}
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return v;
	}
	
	/**
	 * Update the value of the v field according to the key's value.
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void set(String key, String value) throws Exception {
		System.setProperty("user.timezone","Asia/Shanghai");
		int row = 0;
		PreparedStatement stmt = null;
		
		
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			long now = date.getTime() + 1000 * 1 * 60 * 60;
			String expires = simpleDateFormat.format(now);
			System.out.println(expires);
			stmt = this.conn.prepareStatement("update " + this.tableName + " set v=?, expires=? where k=?");
			stmt.setString(1, value);
			stmt.setString(2, expires);
			stmt.setString(3, key);
			row = stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(row <= 0) {
			throw new Exception("Update the value of key(" + key + ") failed, the value is" + value);
		}
		
	}

}
