package me.pake.push.conf;

/**
 * SMTP server configuration.
 * 
 */
public final class EmailConf {
	
	/**
	 * SMTP server host.
	 */
	public static String 	HOST 	= "127.0.0.1";
	
	/**
	 * SMTP authentication switch.
	 */
	public static boolean 	AUTH 	= false;
	
	/**
	 * SMTP authentication user's name.
	 */
	public static String 	USER 	= "";

	/**
	 * SMTP authentication user's password.
	 */
	public static String 	PASS 	= "";
	
	/**
	 * SMTP debug switch.
	 */
	public static boolean 	DEBUG 	= false;
	
	/**
	 * SMTP from address.
	 */
	public static String 	FROM 	= "marster@pake.me";
}
