package me.pake.push.model;

public class StandardGroupBill {
	
	public static int  STATUS_HAS_THE_TEAM 	= 1; // 已组队（生成订单默认状态）
	public static int  STATUS_PAID 			= 2; // 已付费
	public static int  STATUS_CONSUNMED 	= 3; // 已消费
	public static int  STATUS_WITHDRAWN 	= 4; // 已结算（体现）
	public static int  STATUS_APPLYING 		= 5; // 报名中（组队后，有人退出）

	public StandardGroupBill() {}

}
