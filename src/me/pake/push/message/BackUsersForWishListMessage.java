package me.pake.push.message;

/**
 * The users, who need pusing message to.
 * 
 * While some one wish message replied, the push message would send to these users.
 * 
 *
 */
public class BackUsersForWishListMessage implements Message {
	private int userid;
	
	private String email, 
				   openid;

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public int getType() {
		return Message.WISH_LIST;
	}
	
	
}
