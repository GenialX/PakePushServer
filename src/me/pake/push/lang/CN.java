package me.pake.push.lang;

import me.pake.push.conf.GroupPayConf;

/**
 * Language libraries for Chinese.
 * 
 * @author GenialX
 * @since 0.2.0
 *
 */
public interface CN {
	//==============//
	/* Group[整点趴] */
	//==============//
	/* Pay */
	public static String GROUP			 = "标准趴";
	
	public static String GROUP_PAY_TITLE = "哈哈，你报名的整点趴已经组队成功。请点击此处确认付费。"+GroupPayConf.PAY_TIME+"分钟内未确认视为自动退出哦！";
	public static String GROUP_PAY_LINK  = "\n小伙伴们喊你付款呢！点我点我~";
	
	/* Paid　*/
	public static String GROUP_PAID_TITLE = "亲，您参加的标准趴已经付款成功喽！";
	public static String GROUP_PAID_LINK  = "\n小伙伴们，准备出发吧！查看活动详情，点我点我~";
	
	/* RedNews CMS LikeActivity */
	public static String REDNEWS_LIKE       	  = "点赞抢招聘门票";
	public static String REDNEWS_LIKE_TITLE_MY    = "亲，您参加的招聘抢票活动已积够赞数喽。一强盗一枚热乎乎的招聘门票！";
	public static String REDNEWS_LIKE_TITLE_OTHER = "亲，由于您的大力支持，您的好友已经成功抢到门票喽！";
	public static String REDNEWS_LIKE_LINK        = "\n查看详情，点我点我~";
}
