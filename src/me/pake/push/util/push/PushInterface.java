package me.pake.push.util.push;

/**
 * @version 0.6.0
 *
 */
public interface PushInterface {
	
	/**
	 * @since 0.0.1
	 */
	public static String EmailPushAPI = "emailPushAPI";
	
	/**
	 * @since 0.0.1
	 */
	public static String WechatPushAPI = "wechatPushAPI";
	
	// push type 
	// same with php server
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_WISH_LIST = 0x0001; // 愿望清单
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_WISH_BACK = 0x0002; // 愿望清单回复
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_FRIEND_PLAN = 0x0003; // 伙伴计划
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_ENTRY_PLAN = 0x0004; //加入伙伴计划
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_PARTY = 0x0005; // 聚会
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_ENTRY_PARTY=0x0006; //参与聚会
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_CHECK_IN = 0x0007; // 聚会签到
	
	/**
	 * @since 0.0.1
	 */
	public static int PUSH_TYPE_DAILY_NOTICE = 0x0008; //每日提醒
	
	/**
	 * @since 0.1.0
	 * 
	 */
	public static int PUSH_TYPE_VIRTUAL_WISH_LIST = 0x0009; // 虚拟愿望清单创建
	

	/**
	 * For group, to notice the user to pay for it.
	 * 
	 * @since 0.3.0
	 */
	public static int PUSH_TYPE_GROUP_PAY = 0x0012; 
	
	/**
	 * For promotion, by sending email.
	 *
	 * @since 0.4.0
	 *
	 */
	public static int PUSH_TYPE_PROMOTE_EMAIL = 0x0013;
	
	/**
	 * The current group has been paid by all users.
	 * 
	 * @since 0.4.0
	 */
	public static int PUSH_TYPE_GROUP_PAID = 0x0014;
	
	
	/**
	 * While the like_count field in the `like_activity` table up to thirty.
	 * 
	 * @since 0.5.0
	 */
	public static int PUSH_TYPE_LIKE_ACTIVITY_PUSH = 0x0015;
	
	/**
	 * Auto dispose the group data.
	 * 
	 * @since 0.6.0
	 */
	public static int PUSH_TYPE_GROUP_CRONTAB = 0x0016;
	
	/**
	 * @since 0.7.0
	 */
	public static int PUSH_TYPE_RD_LIKE_ACTIVITY_PUSH = 0x0017;
	
}
