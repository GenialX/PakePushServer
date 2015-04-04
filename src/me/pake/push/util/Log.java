package me.pake.push.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.pake.push.conf.DeveloperConf;

/**
 * Log.
 * 
 * @author GenialX
 *
 */
public class Log implements LogInterface{

	public static boolean record(String message, String level) {
		SimpleDateFormat formatter = new SimpleDateFormat("Y-M-d H:m:s:SSS");
	 	String now = formatter.format(new Date());
		if(Log.isGo(level) == false) return false;
		System.out.println("[Level] "+ level + " [Time] " + now + " [Message] " + message);
		return true;
	}
	
	/**
	 * Whether or not the current level message is allowed to be logged in console.
	 * 
	 * @param level
	 * @return boolean
	 */
	private static boolean isGo(String level) {
		switch(level) {
		case Log.EMERG:
			return DeveloperConf.CONSOLE_EMERG;
		case Log.ALERT:
			return DeveloperConf.CONSOLE_ALERT;
		case Log.CRIT:
			return DeveloperConf.CONSOLE_CRIT;
		case Log.ERR:
			return DeveloperConf.CONSOLE_ERR;
		case Log.WARN:
			return DeveloperConf.CONSOLE_WARN;
		case Log.NOTICE:
			return DeveloperConf.CONSOLE_NOTICE;
		case Log.INFO:
			return DeveloperConf.CONSOLE_INFO;
		case Log.DEBUG:
			return DeveloperConf.CONSOLE_DEBUG;
		case Log.SQL:
			return DeveloperConf.CONSOLE_SQL;
		default:
			return false;
		}
	}

}
