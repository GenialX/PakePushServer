package me.pake.push.util.group;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.conf.ServerConf;
import me.pake.push.conf.SiteConf;
import me.pake.push.conf.WechatConf;
import me.pake.push.lang.CN;
import me.pake.push.message.WishListWechatJsonMessage;
import me.pake.push.model.Model;
import me.pake.push.model.StandardGroupBill;
import me.pake.push.util.HttpGet;
import me.pake.push.util.HttpPost;
import me.pake.push.util.Log;
import me.pake.push.util.push.WechatPush;

public class GroupCrontab {

	private WechatPush WP = null;
	
	public GroupCrontab(WechatPush WP) {
		this.WP = WP;
//		this.test();
		// 处理订单数据.
		this.disposeBills();
		// 处理未生成订单的用户数据.
		this.disposeUsers();
	}


	private void test() {
		// TODO Auto-generated method stub
		Model T = new Model("standard_group_module");				
		String sql = "insert into standard_group_module (`sgb_id`,`date`,`have_people`,`people_count`,`trade_no`) values("+
				"1,2,3,4,'123'"
				+")";
		Log.record(sql, Log.INFO);
		int rs = T.add(sql);  
		Log.record(Integer.toString(rs), Log.INFO);
	}


	/**
	 * 处理有订单的数据.
	 * 
	 */
	private void disposeBills() {
		Log.record("[GROUP CRONTAB] " + "处理有订单的数据", Log.INFO);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		Model SGBI 						= new Model("standard_group_bill");
		List<Map<String, String>> sgbi 	= null;
		sgbi = SGBI.where("status = " + StandardGroupBill.STATUS_HAS_THE_TEAM +
						" or status = " + StandardGroupBill.STATUS_PAID)
						.limit("0,50")
						.select();
		try{
			Log.record("[GROUP CRONTAB] " + "一共获取" + sgbi.size() + "个订单数据", Log.INFO);
			for(int i = 0; i < sgbi.size(); i++) {
				Log.record("[GROUP CRONTAB] " + "正在处理第" + (i+1) + "个数据", Log.INFO);
				long start_time_timestamp = 0;
				long now_timestamp = new Date().getTime();
				try {
					date = sdf.parse(sgbi.get(i).get("start_time"));
					start_time_timestamp = date.getTime();
					long diffMillis = 1 * 60 * 60 * 1000; // 一个小时
					if(now_timestamp + diffMillis  >= start_time_timestamp) {
						// 离活动开始前不足一小时（超期）
						Log.record("[GROUP CRONTAB] " + "该订单数据超期，开始时间：" + sgbi.get(i).get("start_time") + ";账单ID：" + sgbi.get(i).get("id"), Log.INFO);
						this.disposeOverdueBill(sgbi.get(i));
					} else {
						// 离活动开始前还有大于一小时的时间（未超期）
						Log.record("[GROUP CRONTAB] " + "该订单数据未超期，开始时间：" + sgbi.get(i).get("start_time") + ";账单ID：" + sgbi.get(i).get("id"), Log.INFO);
						this.disposeNotOverdueBill(sgbi.get(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}  finally {
			if(sdf != null) {
				sdf = null;
			}
			if(date != null) {
				date = null;
			}
			if(SGBI != null) {
				SGBI = null;
			}
			if(sgbi != null) {
				sgbi = null;
			}
		}
	}
	
	/**
	 * 处理未超期的订单.
	 * 
	 * TODO 销毁对象
	 * @param map
	 */
	private void disposeNotOverdueBill(Map<String, String> bill) {
		Log.record("[GROUP CRONTAB] " + "正在处理未超期订单数据;订单ID：" + bill.get("id") + ";trade_no:" + bill.get("trade_no")
				+ ";create_time:" + bill.get("create_time") + ";status:" + bill.get("status"), Log.INFO);
		Calendar cal = Calendar.getInstance();
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int sgbu_id = Integer.parseInt(bill.get("sgbu_id")); // 项目ID
		String start_time = bill.get("start_time");
		Model SGM = new Model("standard_group_module");
		Model SGU = new Model("standard_group_users");
		Model SGBI = new Model("standard_group_bill");
		Model SGBU = new Model("standard_group_business");
		Map<String,String> sgbu = SGBU.where("id = " + sgbu_id).find();
		long start_time_timestamp = 0;
		try {
			date = sdf.parse(bill.get("start_time"));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		cal.setTime(date);
		start_time_timestamp = date.getTime();	
		int week = cal.get(Calendar.DAY_OF_WEEK);
		if(week == 1) week = 7;
		else week--;
		int newDate = week * 100 + date.getHours();
		int havePeople =  0;
		int peopleCount = Integer.parseInt(sgbu.get("group_type"));
		String trade_no = bill.get("trade_no");
		
		long diffMillis =   10 * 60 * 1000; // 十分钟
		int status = Integer.parseInt(bill.get("status"));
		Map<String,String> sameModule = SGM.where("date = " + newDate + " and sgb_id = " + Integer.parseInt(bill.get("sgbu_id")) ).find();
		int sameHavePeople = Integer.parseInt(sameModule.get("have_people"));
		String sameTradeNo = sameModule.get("trade_no");
		Log.record("[GROUP CRONTAB] 找到的同时间同主题的房间信息" + sameTradeNo, Log.INFO);
		
		if(status == StandardGroupBill.STATUS_HAS_THE_TEAM) {
			Log.record("[GROUP CRONTAB] " + "该订单处于支付状态", Log.INFO);
			// 订单处于支付状态.
			long create_time_timestamp = 0;
			long now_timestamp = new Date().getTime();

			try {
				date = sdf.parse(bill.get("create_time"));
				create_time_timestamp = date.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(create_time_timestamp + diffMillis < now_timestamp) {
				Log.record("[GROUP CRONTAB] " + "该订单超过十分钟内没有进行支付", Log.INFO);
			
				// 该订单已超过十分钟未全部支付
				/* 预处理 */
				// 当前订单中已支付的人数
				havePeople = SGU.where("trade_no = '" + trade_no + "' and is_paid = 1").count();
				
				if(havePeople + sameHavePeople > peopleCount) {
					Log.record("[GROUP CRONTAB]" + "当前订单中已支付人数与同主题时间的房间的报名人数之和大于总人数", Log.INFO);
					// 取消支付（更改订单、新增standard_group_module记录和删除未支付用户，更改已支付用户状态），空出座位（T出未支付的人），该活动重新出现在标准趴页面。
					
					/* 删除未支付用户（包含退出的） */
					Log.record("[GROUP CRONTAB] 删除未支付用户（包含退出的）：" + "trade_no = '" + trade_no+ "' and is_paid = 0", Log.INFO);
					SGU.where("trade_no = '" + trade_no+ "' and is_paid = 0").delete();
					
					/* 新增sgm */
					String sql = "insert into standard_group_module (`sgb_id`,`date`,`have_people`,`people_count`,`trade_no`) values("+
							sgbu_id + "," + newDate + "," + havePeople + "," + peopleCount + ",'" + trade_no +"'"
							+")";
					Log.record("[GROUP CRONTAB] 新增module SQL：" + sql, Log.INFO);
					SGM.add(sql);
					/* 更改已支付用户状态 */
					String sql1 = "update standard_group_users set sgbi_id = 0 where trade_no = '" + trade_no + "' ";
					Log.record("[GROUP CRONTAB] 更改用户SQL：" + sql1, Log.INFO);
					SGU.save(sql1);
					/* 更改订单状态 */
					sql = "update standard_group_bill set status = 5 where id = " + Integer.parseInt(bill.get("id"));
					Log.record("[GROUP CRONTAB]" + "更改订单status状态，设置为5。SQL:" + sql, Log.INFO);
					SGBI.save(sql);					
				} else if(havePeople + sameHavePeople < peopleCount) {
					Log.record("[GROUP CRONTAB]" + "当前订单中已支付人数与同主题时间的房间的报名人数之和小于总人数", Log.INFO);
					// 删除该订单下未支付的用户
					Log.record("[GROUP CRONTAB] 删除未支付用户（包含退出的）：" + "trade_no = '" + trade_no+ "' and is_paid = 0", Log.INFO);
					SGU.where("trade_no = '" + trade_no+ "' and is_paid = 0").delete();
					
					// 更改房间属性
					int sameModuleID = Integer.parseInt(sameModule.get("id"));
					String sql = "update standard_group_module set have_people = " + (havePeople+sameHavePeople) + " where id = " + sameModuleID;
					Log.record("[GROUP CRONTAB] 更改房间属性sql" + sql, Log.INFO);
					SGM.save(sql);
					
					// 更改订单状态
					sql = "update standard_group_bill set status = " + StandardGroupBill.STATUS_APPLYING + " , trade_no = '" + sameTradeNo + "' where id = " + Integer.parseInt(bill.get("id"));
					Log.record("[GROUP CRONTAB] 更改订单状态 sql： " + sql, Log.INFO);
					SGBI.save(sql);
					
					// 更改用户属性
					sql =  "update standard_group_users set sgbi_id = 0 , trade_no = '" + sameTradeNo + "' where trade_no = '" + trade_no + "' or trade_no = '" + sameTradeNo + "'";
					Log.record("[GROUP CRONTAB] 更改用户属性SQL:" + sql, Log.INFO);
					SGBU.save(sql);
					
					
				} else {
					Log.record("[GROUP CRONTAB] " + "当前订单中已支付人数与同主题时间的房间的报名人数之和等于总人数", Log.INFO);
					
					// 删除该订单下未支付的用户
					Log.record("[GROUP CRONTAB] 删除未支付用户（包含退出的）：" + "trade_no = '" + trade_no+ "' and is_paid = 0", Log.INFO);
					SGU.where("trade_no = '" + trade_no+ "' and is_paid = 0").delete();
					
					// 更改房间属性
					int sameModuleID = Integer.parseInt(sameModule.get("id"));
					String sql = "update standard_group_module set have_people = 0 , trade_no = '" + trade_no + "' where id = " + sameModuleID;
					Log.record("[GROUP CRONTAB] 更改房间属性sql" + sql, Log.INFO);
					SGM.save(sql);
					
					// 更改订单状态
					String nowTimeString = sdf.format(new Date());
					sql = "update standard_group_bill set status = 1 , create_time = '" + nowTimeString + "' , trade_no = '" + sameTradeNo + "' where id = " + Integer.parseInt(bill.get("id"));
					Log.record("[GROUP CRONTAB] 更改订单状态 sql： " + sql, Log.INFO);
					SGBI.save(sql);
					
					// 更改用户属性
					sql =  "update standard_group_users set sgbi_id = " + Integer.parseInt(bill.get("id")) + ", trade_no = '" + sameTradeNo + "' where trade_no = '" + sameTradeNo + "' or trade_no = '" + trade_no + "'";
					Log.record("[GROUP CRONTAB] 更改用户属性SQL:" + sql, Log.INFO);
					SGBU.save(sql);
				  
					// 推送
					int billID = Integer.parseInt(bill.get("id"));
					Log.record("[GROUP CRONTAB] 推送订单的ID"+billID, Log.INFO);
					HttpGet hg = new HttpGet();
					try {
						hg.get(ServerConf.HOST, ServerConf.GROUP_PAY_URI + billID, ServerConf.PORT);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
			} else {
				Log.record("[GROUP CRONTAB]" + "该订单没有超过十分钟内没有进行支付，不予处理", Log.INFO);
			}
			
		} else {
			Log.record("[GROUP CRONTAB]" + "该订单处于已支付完状态，不用处理.", Log.INFO);
		}
	}


	/**
	 * 处理超期订单.
	 * 
	 * @param map
	 */
	private void disposeOverdueBill(Map<String, String> bill) {
		Log.record("[GROUP CRONTAB] " + "正在处理超期订单数据;订单ID：" + bill.get("id") + ";trade_no:" + bill.get("trade_no")
			+ ";create_time:" + bill.get("create_time") + ";status:" + bill.get("status"), Log.INFO);
		String tradeNo = bill.get("trade_no");
		int billID = Integer.parseInt(bill.get("id"));
		int status = Integer.parseInt( bill.get("status"));
		if(status == StandardGroupBill.STATUS_PAID) {
			Log.record("[GROUP CRONTAB] " + "全部用户为已支付状态[订单status为2]", Log.INFO);
			// 用户全部为已支付状态
			this.notifyGoForPake(bill); 
		} else {
			Log.record("[GROUP CRONTAB] " + "有部分或全部用户处于未支付状态[订单status为1]", Log.INFO);
			// 有部分或全部用户处于未支付状态
			this.dismissGroup(bill);
		}
	}

	/**
	 * 遍历该订单的用户，根据用户支付状态微信通知用户“活动已解散”.
	 * 
	 * TODO 销毁对象
	 * 
	 * @param bill
	 */
	private void dismissGroup(Map<String, String> bill) {
		int billID = Integer.parseInt(bill.get("id"));
		Model SGU = new Model("standard_group_users");
		Model SGBI = new Model("standard_group_bill");
		Model U = new Model("user");
		// 得到该订单下的用户.
		List<Map<String,String>> sgus = SGU.where("sgbi_id = " + billID + " and is_paid = 1 and is_out = 0").select();
	 	Model SGBU									= new Model("standard_group_business");
	 	Map<String,String> sgbu						= SGBU.where("id = " + bill.get("sgbu_id")).find();	
	 	
		// 构造推送消息.
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		String url 	   = SiteConf.BASH_URL + "/group/Web/Group/index/";
		msg.setUrl(url);
		msg.setKeynote1Value(CN.GROUP);
		msg.setKeynote2Value(bill.get("start_time"));
		msg.setRemarkValue("\n点我查看更多活动吧！");
		Log.record("遍历该订单的用户，根据用户支付状态微信通知用户“活动已解散”，推送给" + sgus.size() + "人。", Log.INFO);

		// 遍历用户
		for(int i = 0 ; i < sgus.size(); i++) {
			Map<String, String> user = U.where("id = '" + sgus.get(i).get("uid") + "'").find();
			int isPaid = Integer.parseInt(sgus.get(i).get("is_paid"));
			if(isPaid == 1) {
				// 用户支付过.
				String groupNotifyContent = "您参与的整点趴“" + sgbu.get("title") + "”，因为报名人数未满，自动取消，活动费用已如数返还。感谢您的支持！您可选择参与其他活动。";
				msg.setFisrtValue(groupNotifyContent);
				/* 趴豆返还 */
				int oldPadou = Integer.parseInt(user.get("pdou"));
				int charge_padou = Integer.parseInt(sgbu.get("charge_padou"));
				int newPadou = oldPadou + charge_padou ;
				String sql = "update user set pdou = " + newPadou + " where id = " + Integer.parseInt(user.get("id"));
				Log.record("更改用户趴豆数[返还趴豆], SQL:" + sql, Log.INFO);
				U.save(sql);
			} else {
				// 用户未支付过.
				String groupNotifyContent = "您参与的整点趴“" + sgbu.get("title") + "”，还有一个小时就要开始，因为报名人数未满，自动取消，感谢您的支持！您可选择参与其他活动。";
				msg.setFisrtValue(groupNotifyContent);
			}
			// 推送.
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(user.get("openid"));
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.WP.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
					Log.record(response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 删除订单和用户数据记录
		Log.record("开始删除订单和用户数据.", Log.INFO);
		Log.record("删除订单的sql：id = " + billID ,Log.INFO);
		Log.record("删除用户的sql：sgbi_id = " + billID ,Log.INFO);
		SGBI.where("id = " + billID).delete();
		SGU.where("sgbi_id = " + billID).delete();
		
	}

	/**
	 * 微信通知用户，活动开始前不足一小时，赶紧出发吧.
	 * TODO 销毁对象
	 */
	private void notifyGoForPake(Map<String, String> bill) {
		int billID = Integer.parseInt(bill.get("id"));
		Model U = new Model("user");
		Model SGU = new Model("standard_group_users");
		Model SGBI = new Model("standard_group_bill");
		
		// 得到该订单下已付费的用户.
		List<Map<String,String>> sgus = SGU.where("sgbi_id = " + billID + " and is_paid = 1 and is_out = 0").select();
	 	Model SGBU									= new Model("standard_group_business");
	 	Map<String,String> sgbu						= SGBU.where("id = " + bill.get("sgbu_id")).find();	
	 	
	 	// 判断是否已经推送过
	 	int isPush = Integer.parseInt(bill.get("is_push"));
	 	if(isPush == 1) {
	 		Log.record("[GROUP CRONTAB] " + "改订单已经推送过消息", Log.INFO);
	 		return;
	 	} else {
	 		String sql = "update standard_group_bill set is_push = 1 where id = " + billID;
	 		Log.record("[GROUP CRONTAB] " + "改订单没有推送过消息。修改订单推送状态,SQL:" + sql, Log.INFO);
	 		SGBI.save(sql);
	 	}
		
		// 构造推送消息.
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		String url 	   = SiteConf.BASH_URL + "/group/Web/Pay/index/bill_id/" + billID;
		msg.setUrl(url);
		msg.setKeynote1Value(CN.GROUP);
		msg.setKeynote2Value(bill.get("start_time"));
		msg.setRemarkValue(CN.GROUP_PAID_LINK);
		String groupNotifyContent = "您报名的整点趴“" + sgbu.get("title") + "”还有不到一小时的时间，就要开始了，快到现场了吗？咱们不见不散喔！该活动的验证码为：" + bill.get("trade_no") + ",商家验证后即可消费。";
		msg.setFisrtValue(groupNotifyContent);
		Log.record("[GROUP CRONTAB] 开始启动“微信通知用户，活动开始前不足一小时，赶紧出发吧.”任务,一共通知" + sgus.size() + "人。", Log.INFO);
		for(int i = 0; i < sgus.size(); i++) {
			// 微信推送.
			Map<String, String> user = U.where("id = '" + sgus.get(i).get("uid") + "'").find();

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(user.get("openid"));
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.WP.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
					Log.record(response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * 处理未生成订单的用户数据.
	 * 
	 * @throws ParseException 
	 */
	private void disposeUsers()  {
		Log.record("[GROUP CRONTAB] " + "处理“未生成订单的用户数据”", Log.INFO);
		Model SGM = new Model("standard_group_module");
		Model SGU = new Model("standard_group_users");
		Model SGBI = new Model("standard_group_bill");
		Model SGBU = new Model("standard_group_business");		
		Calendar cal = Calendar.getInstance();
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 获取没有订单，且没有退出过的用户.
		List<Map<String,String>> users = SGU.where("sgbi_id = 0 and is_out = 0").limit("0,7").select();
		try {
			Log.record("[GROUP CRONTAB] " + "一共"+users.size()+"个用户", Log.INFO);
			for(int i = 0 ; i < users.size(); i++) {
				Log.record("[GROUP CRONTAB] " + "处理第" + (i+1) + "个用户", Log.INFO);
				String start_time = users.get(i).get("start_time");
				date = sdf.parse(start_time);
				long start_time_timestamp = date.getTime();
				long diffMillis = 1 * 60 * 60 * 1000;
				long now_timestamp = new Date().getTime();
				if(now_timestamp + diffMillis > start_time_timestamp) {
					// 改组报名已离活动开始前不足一小时时间（超期）
					try{
						this.disposeTheOverdueUser(users.get(i));
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					Log.record("[GROUP CRONTAB] " + "该用户没有超期", Log.INFO);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(SGM != null) {
				SGM = null;
			}
			if(SGU != null) {
				SGU = null;
			}
			if(SGBI != null) {
				SGBI = null;
			}
			if(SGBU != null) {
				SGBU = null;
			}
			if(cal != null) {
				cal = null;
			}
			if(date != null) {
				date = null;
			}
			if(sdf != null) {
				sdf = null;
			}
			if(users != null) {
				users = null;
			}
		}
		
	}


	/**
	 * 处理报名状态下超期的某个用户.
	 * 
	 * @param map
	 */
	private void disposeTheOverdueUser(Map<String, String> user) {
		Log.record("[GROUP CRONTAB] " + "处理报名状态下超期的某个用户。用户ID为：" + Integer.parseInt(user.get("uid")), Log.INFO);
		int uid = Integer.parseInt(user.get("uid"));
		Model SGM = new Model("standard_group_module");
		Model SGU = new Model("standard_group_users");
		Model SGBU = new Model("standard_group_business");
		Model SGBI = new Model("standard_group_bill");
		
		Model U = new Model("user");
		Map<String,String> userInfo = U.where("id = " + uid).find();
		String trade_no = user.get("trade_no");
		Map<String,String> sgm = SGM.where("trade_no = '" + trade_no +"'").find();
		int sgbu_id = 0;
		try{
			sgbu_id = Integer.parseInt(sgm.get("sgb_id"));
		} catch(Exception e) {
			e.printStackTrace();
			// 判断并处理无效的standard_group_users数据.
			int rs = SGM.where("trade_no = '" + trade_no +"'").count();
			if(rs < 1) {
				String sql = "id = " + Integer.parseInt(user.get("id"));
				Log.record("[GROUP CRONTAB] " + "处理[删除]无效的standard_group_users数据;SQL:" + sql, Log.INFO);
				SGU.where(sql).delete();
			}
			return ;
		}
		Map<String,String> sgbu = SGBU.where("id = " + sgbu_id).find();
		int peopleCount = Integer.parseInt(sgm.get("people_count"));
		int havePeople = Integer.parseInt(sgm.get("have_people"));
		Log.record("[GROUP CRONTAB] " + "trade_no:" + trade_no + ";sgbu_id:" + sgbu_id + ";sgu_id:" + Integer.parseInt(user.get("id")),Log.INFO);
		// 构造消息体.
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		String url 	   = SiteConf.BASH_URL + "/group/Web/Group/index";
		msg.setUrl(url);
		msg.setKeynote1Value(CN.GROUP);
		msg.setKeynote2Value(user.get("start_time"));
		msg.setRemarkValue("\n点我，查看更多活动！");
		String groupNotifyContent = null;
		
		if(Integer.parseInt(user.get("is_paid")) == 1) {
			Log.record("[GROUP CRONTAB] " + "该用户已经支付", Log.INFO);
			// 已支付的用户[由于曾经的已组队被解散，且支付了].
			groupNotifyContent = "您参与的整点趴“"+sgbu.get("title")+"”，因为报名人数未满，自动取消，活动费用已如数返还。感谢您的支持！";
			msg.setFisrtValue(groupNotifyContent);
			/* 趴豆返还 */
			int oldPadou = Integer.parseInt(userInfo.get("pdou"));
			int charge_padou = Integer.parseInt(sgbu.get("charge_padou"));
			int newPadou = oldPadou + charge_padou ;
			String sql = "update user set pdou = " + newPadou + " where id = " + uid;
			Log.record("[GROUP CRONTAB] " + "更改用户趴豆数[返还趴豆], SQL:" + sql, Log.INFO);
			U.save(sql);
		} else {
			// 未支付用户.
			Log.record("[GROUP CRONTAB] " + "该用户未支付过", Log.INFO);
			groupNotifyContent = "您参与的整点趴“"+sgbu.get("title")+"”，还有一个小时就要开始，因为报名人数未满，自动取消，感谢您的支持！您可选择参与其他活动。";
			msg.setFisrtValue(groupNotifyContent);
		}
		/* 删除用户 */
		String sql = "uid = " + uid + " and trade_no = '" + trade_no +"'";
		Log.record("[GROUP CRONTAB] 删除用户.SQL:" + sql, Log.INFO);
		SGU.where(sql).delete();
		/* 更改module属性 */
		if(havePeople != 0) {
			sql = "update standard_group_module set have_people = 0 where trade_no = '" + trade_no +"'";
			Log.record("[GROUP CRONTAB] 更改module属性.SQL:" + sql, Log.INFO);
			SGM.save(sql);
		}
		/* 删除相应的status为5的订单数据 */
		sql = "trade_no = '" + trade_no + "' and status = " + StandardGroupBill.STATUS_APPLYING;
		Log.record("[GROUP CRONTAB] 删除指定的订单。SQL:" + sql, Log.INFO);
		SGBI.where(sql).delete();

		// 微信推送.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		HttpPost hp = new HttpPost();
		msg.setTouser(userInfo.get("openid"));
		HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
		String response = null;
		try {
			String access_token = this.WP.getAccessToken();
			Log.record("[GROUP CRONTAB] 通知用户:" + groupNotifyContent, Log.INFO);
			if(!DeveloperConf.PUSH_DEBUG) {
				response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
				Log.record("[GROUP CRONTAB] " + response, Log.INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(SGM != null) {
				SGM = null;
			}
			if(SGU != null) {
				SGU = null;
			}
			if(SGBU != null) {
				SGBU = null;
			}
			if(U != null) {
				U = null;
			}
			if(userInfo != null) {
				userInfo = null;
			}
			if(sgm != null) {
				sgm = null;
			}
			if(sgbu != null) {
				sgbu = null;
			}
			if(msg != null) {
				msg = null;
			}
			if(hp != null) {
				hp = null;
			}
		}
	}
	
	
}