package me.pake.push.action;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.util.Log;
import me.pake.push.util.URIFilter;
import me.pake.push.util.push.EmailPush;
import me.pake.push.util.push.PushInterface;
import me.pake.push.util.push.WechatPush;

/**
 * Notice specific push action to push messages
 *
 */
public class PushWorker implements Runnable, PushInterface{
	
	/**
	 * URI from the HTTP request line, which would be analyzed by the URLFilter object, 
	 * according the analyzed parameters to tell program how to work.
	 */
	private String uri;
	
	private URIFilter uriFilter;
	
	/**
	 * Tell the program which push method should be used,
	 * such as email push, WeChat push and so on,
	 * and start push.
	 * 
	 * @param uri the URI from HTTP request line
	 */
	public PushWorker(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Tell program which method should be chose to push message.
	 */
	@Override
	public void run() {
		// Initial URI parameters
		this.uriFilter = new URIFilter(this.uri);
		
		// Switch the API action name such as emailPushAPI, wechatPushAPI and so on...
		switch(this.uriFilter.getActionName()) {
			case PushInterface.EmailPushAPI:
					EmailPush ep = new EmailPush(this.uriFilter);
					ep.push();
					if(ep != null){
						ep = null;
					}
				;break;
			case PushInterface.WechatPushAPI:
					WechatPush wp = new WechatPush(this.uriFilter);
					wp.push();
					if(wp != null) {
						wp = null;
					}
				;break;
			default:
				Log.record("Invalid GET Request: " + this.uri, Log.INFO);
				;break;
		}
	
		// Dispose
		if(this.uriFilter != null) {
			this.uriFilter = null;
		}
	}
	
}
