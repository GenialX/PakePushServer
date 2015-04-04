package me.pake.push.conf;

/**
 * Server configuration.
 *
 */
public class ServerConf {

	/**
	 * Server's user_agent: {@value}
	 */
	public static String USER_AGENT 	= "PakePushServer/0.0.1 imgenialx@gmail.com";
	
	
	/**
	 * This is the limit to the active hour count of the push messages pushed to users.
	 * 
	 */
	public static int RECEIVE_ACTIVE_HOUR_ACCOUNT = 1;
	
	/**
	 * This is the limit to the messages pushed count at the range of the {@value #RECEIVE_ACTIVE_HOUR_COUNT}.
	 * 
	 */
	public static int RECEIVE_ACCOUNT = 2;
	
	/**
	 * The specific WeChat MP API host.
	 */
	public static String HOST			= "wechat.pake.me";
	
	/**
	 * The port.
	 * 
	 */
	public static int PORT 				= 1720;
	
	/**
	 * The WeChat MP API URI for "template message" push.
	 */
	public static String GROUP_PAY_URI	= "/wechatPushAPI?push_type=18&bill_id=";

	
}
