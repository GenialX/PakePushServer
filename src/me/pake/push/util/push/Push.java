package me.pake.push.util.push;

public interface Push {
	public static String EmailPushAPI = "emailPushAPI";
	public static String WechatPushAPI = "wechatPushAPI";
	
	// push type 
	// same with php server
	
	public static int PUSH_TYPE_WISH_LIST = 0x0001; // 愿望清单
	
	public static int PUSH_TYPE_WISH_BACK = 0x0002; // 愿望清单回复
	
	public static int PUSH_TYPE_FRIEND_PLAN = 0x0003; // 伙伴计划
	
	public static int PUSH_TYPE_ENTRY_PLAN = 0x0004; //加入伙伴计划
	
	public static int PUSH_TYPE_PARTY = 0x0005; // 聚会
	
	public static int PUSH_TYPE_ENTRY_PARTY=0x0006; //参与聚会
	
	public static int PUSH_TYPE_CHECK_IN = 0x0007; // 聚会签到
	
	public static int PUSH_TYPE_DAILY_NOTICE = 0x0008; //每日提醒
	
	
	
}
