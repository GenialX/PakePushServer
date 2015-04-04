package me.pake.push.util.push;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.util.Log;
import me.pake.push.util.URIFilter;

public abstract class PushAbstract {

	protected URIFilter urlFilter = null;
	
	public PushAbstract(URIFilter urlFilter) {
		this.urlFilter = urlFilter;
	}
	
	protected boolean filterPushType() {
		if(!this.urlFilter.isset("push_type")) {
if(DeveloperConf.CONSOLE_INFO) System.out.println("Push type is null or unset");
			return false;
		}
		return true;
	}
	
	// filter params for create wish msg
	protected boolean filterTypeWishListParams() {
		if(!this.urlFilter.isset("lat")) {
			return false;
		}
		
		if(!this.urlFilter.isset("lon")) {
			return false;
		}
		
		if(!this.urlFilter.isset("info_id")) {
			return false;
		}

		if(!this.urlFilter.isset("time")) {
			return false;
		}

		if(!this.urlFilter.isset("fromuserid")) {
			return false;
		}
		
		return true;
	}
	
	protected boolean filterTypeWishBackParam() {
		if(!this.urlFilter.isset("frouserid")) {
			return false;
		}
		if(!this.urlFilter.isset("info_id")) {
			return false;
		}
		if(!this.urlFilter.isset("time")) {
			return false;
		}
		
		return true;
	}
	
	protected boolean filterTypeVirtualWishListParams() {
		if(!this.urlFilter.isset("lat")) {
			return false;
		}
		
		if(!this.urlFilter.isset("lon")) {
			return false;
		}
		
		if(!this.urlFilter.isset("info_id")) {
			return false;
		}
		
		if(!this.urlFilter.isset("time")) {
			return false;
		}
		
		if(!this.urlFilter.isset("username")) {
			return false;
		}
		
		return true;
	}
	
	protected boolean filterTypeGroupPayParams() {
		if(!this.urlFilter.isset("bill_id")) {
			return false;
		}
		return true;
	}
	
	protected boolean filterTypeGroupPaidParams() {
		if(!this.urlFilter.isset("bill_id")) {
			return false;
		}
		return true;
	}
	
	protected boolean filterTypePromoteEmailParams() {
		if(!this.urlFilter.isset("email_push_id")) {
			return false;
		}
		return true;
	}
	
	protected boolean filterTypeLikeActivityPushParams() {
		if(!this.urlFilter.isset("like_activity_id")) {
			return false;
		}
		return true;
	}
	
	// abstract methods
	/**
	 * push message according to the push type
	 */
	public abstract void push();
	
}
