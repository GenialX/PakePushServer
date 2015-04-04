package me.pake.push.conf;

/**
 * Developer configuration 
 *
 */
public final class DeveloperConf {

	/**
	 * push debug switch.
	 * 
	 * If true, it would not push message, such as WeChat, email and so on.
	 * while some one creating one wish message, it would not push message to the nearest users.
	 */
	public static boolean PUSH_DEBUG = false;

	//=======//
	//==LOG==//
	//=======//
	
	public static boolean CONSOLE_EMERG = true;
	
	public static boolean CONSOLE_ALERT = true;
	
	public static boolean CONSOLE_CRIT = true;
	
	public static boolean CONSOLE_ERR  = true;
	
	public static boolean CONSOLE_WARN  = true;
	
	public static boolean CONSOLE_NOTICE = true;

	/**
	 * Console info debug switch.
	 * 
	 * If true, it would echo the necessary running info on console.
	 */
	public static boolean CONSOLE_INFO = true;
	
	/**
	 * Console debug switch.
	 * 
	 * If true, it would echo the necessary running  debug messages on console.
	 */
	public static boolean CONSOLE_DEBUG = true;
	
	public static boolean CONSOLE_SQL    = true;
	
	
}
