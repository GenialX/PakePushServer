package me.pake.push.conf;

/**
 * WeChat configuration
 * 
 */
public class WechatConf {
	
	/**
	 * The specific WeChat MP API host.
	 */
	public static String HOST			= "api.weixin.qq.com";
	
	/**
	 * The WeChat MP API URI for "template message" push.
	 */
	public static String TPL_MSG_URI	= "/cgi-bin/message/template/send";
	
	/**
	 * The WeChat MP API URI for get "access_token".
	 */
	public static String ACCESS_TOKEN_URI = "/cgi-bin/token";


}
