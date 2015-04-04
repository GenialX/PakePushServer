package me.pake.push.message;

/**
 * Users, who are near to those created one wish message.
 *
 */
public class NearestUsersForWishListMessage implements Message {
	
	private int userid,
				wcnt,
				acnt,
				gcnt;
	public int getWcnt() {
		return wcnt;
	}

	public void setWcnt(int wcnt) {
		this.wcnt = wcnt;
	}

	public int getAcnt() {
		return acnt;
	}

	public void setAcnt(int acnt) {
		this.acnt = acnt;
	}

	public int getGcnt() {
		return gcnt;
	}

	public void setGcnt(int gcnt) {
		this.gcnt = gcnt;
	}

	private double 	lat,
					lon,
					distance;
	private String email,
				   openid;
	
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public int getType() {
		return Message.WISH_LIST;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
