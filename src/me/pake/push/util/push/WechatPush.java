/**
 * filter params by URLFilter handler and send wechat message
 * 
 */
package me.pake.push.util.push;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.conf.ServerConf;
import me.pake.push.conf.SiteConf;
import me.pake.push.conf.WechatConf;
import me.pake.push.lang.CN;
import me.pake.push.message.BackUsersForWishListMessage;
import me.pake.push.message.NearestUsersForWishListMessage;
import me.pake.push.message.WishListWechatJsonMessage;
import me.pake.push.model.KV;
import me.pake.push.model.Model;
import me.pake.push.model.User;
import me.pake.push.model.WechatReceive;
import me.pake.push.model.WishMsg;
import me.pake.push.util.*;
import me.pake.push.util.group.GroupCrontab;


public class WechatPush extends PushAbstract{
	
	/**
	 * The current access_token value.
	 */
	private String access_token = null;

	public WechatPush(URIFilter urlFilter) {
		super(urlFilter);
	}

	public void push() {
		// filter params
		if(!this.filterPushType()) {
			return;
		}
		
		switch(Integer.valueOf(this.urlFilter.get("push_type"))) {
		case PushInterface.PUSH_TYPE_WISH_LIST:
				this.pushTypeWishList();
			break;
		case PushInterface.PUSH_TYPE_WISH_BACK:
				this.pushTypeWishBack();
			break;
		case PushInterface.PUSH_TYPE_VIRTUAL_WISH_LIST:
				this.pushTypeVirtualWishList();
			break;
		case PushInterface.PUSH_TYPE_GROUP_PAY:
				this.pushTypeGroupPay();
			break;
		case PushInterface.PUSH_TYPE_GROUP_PAID:
				this.pushTypeGroupPaid();
			break;
		case PushInterface.PUSH_TYPE_LIKE_ACTIVITY_PUSH:
				this.pushTypeLikeActivityPush();
			break;
		case PushInterface.PUSH_TYPE_GROUP_CRONTAB:
				new GroupCrontab(this);
			break;
		default:
			Log.record("Invalid push type: " + this.urlFilter.get("push_type"), Log.INFO);
			break;
		}
	}
	
	// private methods


	// run the method while some one creating one wishmsg
	/**
	 * @version 0.0.3 
	 * Modify the bug while the user's nickname is null.
	 */
	private void pushTypeWishList() {
		// valid params
		if(!this.filterTypeWishListParams()) return;
		
		// get users
		PushUser pu		 	= new PushUser();
		int fromuserid 		= Integer.parseInt(this.urlFilter.get("fromuserid"));
		double lat 			= Double.parseDouble(this.urlFilter.get("lat"));
		double lon 			= Double.parseDouble(this.urlFilter.get("lon"));
		int info_id 		= Integer.parseInt(this.urlFilter.get("info_id"));
		String time 		= this.urlFilter.get("time");
		
		List<NearestUsersForWishListMessage> users 	= pu.getNearestUsersForWishWechat(fromuserid, lat, lon, 10000.0, 1000, 30, ServerConf.RECEIVE_ACTIVE_HOUR_ACCOUNT, ServerConf.RECEIVE_ACCOUNT);
		
		// construct subject and content data
		
		// get from user and msg
		List fromUserList 		= null;
		List wishMsgList 		= null;
		User fromUser 			= new User();
		WishMsg wishMsg 		= new WishMsg();
		fromUserList 			= fromUser.getUserByID(fromuserid);
		wishMsgList 			= wishMsg.getWishMsgById(info_id);
		String wishMsgContent 	= "";
		String nickname			= "";
		
		if(fromUserList.size() > 0) {
			Map<String,String> fulm = (Map<String, String>) fromUserList.get(0);
			nickname = fulm.get("nickname");
			if(nickname == "" || nickname == null) nickname = "有人";
		}
		
		if(wishMsgList.size() > 0) {
			Map<String, String> wmlm = (Map<String, String>) wishMsgList.get(0);
			wishMsgContent = wmlm.get("fbcontent");
		}
		
		String url 	   = SiteConf.BASH_URL + "/wechat/index.php/weixin/wishlist/wishsel/" + lat + "/" + lon + "/" + info_id;
		
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setUrl(url);
		msg.setFisrtValue("亲，" + nickname + "在你附近发起了一个愿望:" + wishMsgContent);
		msg.setKeynote1Value("愿望清单");
		msg.setKeynote2Value(time);
		msg.setRemarkValue("\n感兴趣，就点击进去回复吧!");
		
		// Send emails And update email receive table
		Log.record("Accept creating wish list event, the users accepted wechat message account equal: " + users.size(), Log.INFO);
		// push wechat msg...	
		WechatReceive wr = new WechatReceive();
		for(NearestUsersForWishListMessage userMessage : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(userMessage.getOpenid());
//			msg.setTouser("oJY0Fj1mZyycS7MwMMDYDeFUqKWQ");
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
					JSONObject rJsonObj = new JSONObject(response);
//					Log.record("response:" + response + " userid:" + userMessage.getUserid() + " set wcnt to " + (userMessage.getWcnt() + 1), Log.INFO);
					wr.setCnt(userMessage.getUserid(), WechatReceive.WCNT, ( userMessage.getWcnt() + 1 ) );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// dispose
		if(pu != null) {
			pu = null;
		}
		
		if(users != null) {
			users.clear();
			users = null;
		}
		
	}
	
	// run the method while some one reply the specific wishmsg
	private void pushTypeWishBack() {
		// valid params
		if(this.filterTypeWishBackParam()) return;
		
		// get params
		int fromuserid 									= Integer.parseInt(this.urlFilter.get("fromuserid"));
		int info_id	   									= Integer.parseInt(this.urlFilter.get("info_id"));
		String time										= this.urlFilter.get("time");
		List<BackUsersForWishListMessage> users 		= null;
		
		// get users
		PushUser pu 	= new PushUser();
		users       	= pu.getBackUsersForWishWechat(fromuserid, info_id);
		
		// construct email info
		
		// get from user and msg
		List wishMsgList 		= null;
		WishMsg wishMsg 		= new WishMsg();
		wishMsgList 			= wishMsg.getWishMsgById(info_id);
		String _lat 			= null;
		String _lon				= null;
		
		if(wishMsgList.size() > 0) {
			Map<String, String> wmlm = (Map<String, String>) wishMsgList.get(0);
			_lat = wmlm.get("fbclat");
			_lon = wmlm.get("fbclon");
		}
		
		String url = SiteConf.BASH_URL + "/wechat/index.php/weixin/wishlist/wishsel/" + _lat + "/" + _lon + "/" + info_id;
		
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setUrl(url);
		msg.setFisrtValue("亲，您的愿望清单里有人留言了，快去看看ta说了什么！");
		msg.setKeynote1Value("愿望清单");
		msg.setKeynote2Value(time);
		msg.setRemarkValue("\n点击此处，查看详情!");
		
		Log.record("Accept replying wish list event, the users accepted wechat message account equeal: " + users.size(), Log.INFO);
		for(BackUsersForWishListMessage userMessage : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(userMessage.getOpenid());
//			msg.setTouser("oJY0Fj1mZyycS7MwMMDYDeFUqKWQ");
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
					JSONObject rJsonObj = new JSONObject(response);
//					Log.record("response: " + response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// dispose 
		if(users != null) {
			users = null;
		}
		
		if(pu != null) {
			pu = null;
		}
	}
	
	/**
	 * Push WeChat message while creating one virtual one wish list.
	 * @version 0.0.1 21/2014/12
	 */
	private void pushTypeVirtualWishList() {
		// valid params
		if(!this.filterTypeVirtualWishListParams()) return;
		
		// get users
		PushUser pu		 	= new PushUser();
		String username		= this.urlFilter.get("username");
		int fromuserid		= 0;
		double lat 			= Double.parseDouble(this.urlFilter.get("lat"));
		double lon 			= Double.parseDouble(this.urlFilter.get("lon"));
		int info_id 		= Integer.parseInt(this.urlFilter.get("info_id"));
		String time 		= this.urlFilter.get("time");
		
		List<NearestUsersForWishListMessage> users 	= pu.getNearestUsersForWishWechat(fromuserid, lat, lon, 10000.0, 1000, 30, ServerConf.RECEIVE_ACTIVE_HOUR_ACCOUNT, ServerConf.RECEIVE_ACCOUNT);
		
		// construct subject and content data
		
		// get from user and msg
		List wishMsgList 		= null;
		WishMsg wishMsg 		= new WishMsg();
		wishMsgList 			= wishMsg.getWishMsgById(info_id);
		String wishMsgContent 	= "";
		String nickname			= username;
		
		if(wishMsgList.size() > 0) {
			Map<String, String> wmlm = (Map<String, String>) wishMsgList.get(0);
			wishMsgContent = wmlm.get("fbcontent");
		}
		
		String url 	   = SiteConf.BASH_URL + "/wechat/index.php/weixin/wishlist/wishsel/" + lat + "/" + lon + "/" + info_id;
		
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setUrl(url);
		msg.setFisrtValue("亲，" + nickname + "在你附近发起了一个愿望:" + wishMsgContent);
		msg.setKeynote1Value("愿望清单");
		msg.setKeynote2Value(time);
		msg.setRemarkValue("\n感兴趣，就点击进去回复吧!");
		
		// Send emails And update email receive table
		Log.record("Accept creating wish list event, the users accepted wechat message account equeal: " + users.size(), Log.INFO);
		// push wechat msg...
		WechatReceive wr = new WechatReceive();
		for(NearestUsersForWishListMessage userMessage : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(userMessage.getOpenid());
//			msg.setTouser("oJY0Fj1mZyycS7MwMMDYDeFUqKWQ");
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
					JSONObject rJsonObj = new JSONObject(response);
//					Log.record("response:" + response + " userid:" + userMessage.getUserid() + " set wcnt to " + (userMessage.getWcnt() + 1) , Log.INFO);
					wr.setCnt(userMessage.getUserid(), WechatReceive.WCNT, ( userMessage.getWcnt() + 1 ) );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// dispose
		
		if(wr != null) {
			wr = null;
		}
		
		if(pu != null) {
			pu = null;
		}
		
		if(users != null) {
			users.clear();
			users = null;
		}
		
		if(wishMsg != null) {
			wishMsg = null;
		}
		
		if(wishMsgList != null) {
			wishMsgList.clear();
			wishMsgList = null;
		}
		
	}
	
	/**
	 * Push type group pay.
	 * 
	 */
	private void pushTypeGroupPay(){
		/* Get Params */
		if(!this.filterTypeGroupPayParams()) return;

		Log.record("接收到通知支付的消息，订单号:" + this.urlFilter.get("bill_id"), Log.INFO);
		/* Get datas */
		int billID 									= Integer.parseInt(this.urlFilter.get("bill_id"));
		Model SGB			 						= new Model("standard_group_bill");
		Map<String,String> bill 					= null;
		Model SGU  									= new Model("standard_group_users");
		List<Map<String, String>> users       		= null;
		SimpleDateFormat formatter 					= new SimpleDateFormat("Y-M-d H:m:s");
	 	String now 									= formatter.format(new Date());
	 	
		bill = SGB.where("id = " + billID).find();
		if(bill.size() < 1) {
			Log.record("There is not bill item according to the bill_id " + billID, Log.ERR);
			return ;
		}
		users = SGU.where("trade_no = '" + bill.get("trade_no") + "' and is_paid = 0 and is_out = 0 ").select();
		
		String url 	   = SiteConf.BASH_URL + "/group/Web/Pay/index/bill_id/" + billID;
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setUrl(url);
		msg.setFisrtValue(CN.GROUP_PAY_TITLE);
		msg.setKeynote1Value(CN.GROUP);
		msg.setKeynote2Value(bill.get("start_time"));
		msg.setRemarkValue(CN.GROUP_PAY_LINK);
		
		/* push */
		for(int i = 0; i < users.size(); i++) {
			
			Model UM = new Model("user");
			Map<String, String> user = UM.where("id = '" + users.get(i).get("uid") + "'").find();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(user.get("openid"));
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
//					Log.record(response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			/* Dispose the database */
			//...
		}
		
	}

	/**
	 * Push messages to users, after the bill has been paid by all users.
	 * 
	 * @author genialx
	 * 
	 */
	private void pushTypeGroupPaid() {
		/* Get Params */
		if(!this.filterTypeGroupPayParams()) return;
		
		/* Get datas */
		int billID 									= Integer.parseInt(this.urlFilter.get("bill_id"));
		Model SGB			 						= new Model("standard_group_bill");
		Map<String,String> bill 					= null;
		Model SGU  									= new Model("standard_group_users");
		List<Map<String, String>> users       		= null;
		SimpleDateFormat formatter 					= new SimpleDateFormat("Y-M-d H:m:s");
	 	String now 									= formatter.format(new Date());
	 	Model SGBU									= new Model("standard_group_business");
	 	Map<String,String> sgbu						= null;		
	 	Model Admin									= new Model("admin");


		Log.record("接收到通知支付完成的消息，订单号:" + this.urlFilter.get("bill_id"), Log.INFO);
		bill = SGB.where("id = " + billID).find();
		if(bill.size() < 1) {
			Log.record("There is not bill item according to the bill_id " + billID, Log.ERR);
			return ;
		}
		users = SGU.where("trade_no = '" + bill.get("trade_no") + "' and is_out = 0 and is_paid = 1").select();
		sgbu  = SGBU.where("id = " + bill.get("sgbu_id")).find();
		
		String url 	   = SiteConf.BASH_URL + "/group/Web/Pay/index/bill_id/" + billID;
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setUrl(url);
		msg.setKeynote1Value(CN.GROUP);
		msg.setKeynote2Value(bill.get("start_time"));
		msg.setRemarkValue(CN.GROUP_PAID_LINK);
		String groupPaidTitle = "您报名的整点趴“" + sgbu.get("title") + "”全部人员支付完毕，将于" + bill.get("start_time") + "在" + sgbu.get("location") + "进行，赶紧梳妆打扮准备出发吧。该活动的验证码为：" + bill.get("trade_no") + ",商家验证后即可消费。";
		msg.setFisrtValue(groupPaidTitle);
		
		/* push to users*/
		for(int i = 0; i < users.size(); i++) {
			
			Model UM = new Model("user");
			Map<String, String> user = UM.where("id = '" + users.get(i).get("uid") + "'").find();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpPost hp = new HttpPost();
			msg.setTouser(user.get("openid"));
			HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			String response = null;
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
//					Log.record(response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		/* push to business */
		int adminID = Integer.parseInt(bill.get("admin_id"));
		Map<String, String> admin = Admin.where("id = " + adminID).find();
		String openID = admin.get("openid");
		groupPaidTitle = "恭喜，一波小伙伴组队成功，已经准备出发，将于" + bill.get("start_time") + "到店消费，准备好迎接他们吧！嘿嘿。";
		msg.setFisrtValue(groupPaidTitle);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		HttpPost hp = new HttpPost();
		msg.setTouser(openID);
		HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
		String response = null;
		try {
			String access_token = this.getAccessToken();
			if(!DeveloperConf.PUSH_DEBUG) {
				response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
//				Log.record(response, Log.INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Push wechat msg to users while the like_count up to limit count, for the rednews CMS.
	 * 
	 * @return void
	 */
	private void pushTypeLikeActivityPush() {
		/* Get Params */
		if(!this.filterTypeLikeActivityPushParams()) return;
		
		/* Get Datas */
		int likeActivityID							= Integer.parseInt(this.urlFilter.get("like_activity_id"));
		Model LR									= new Model("like_record");
		List<Map<String, String>> lrs			 	= null;
		Model U										= new Model("user");
		Map<String, String> user					= null;
		Model LA									= new Model("like_activity");
		Map<String, String> la						= LA.where("id = '" + likeActivityID + "'").find();
		int ownerUid								= Integer.parseInt(la.get("uid"));
		int recruitID								= Integer.parseInt(la.get("post_id"));
		Model P										= new Model("post");
		Map<String, String> post					= null;
		post										= P.where("id = '" + recruitID + "'").find();
		String info									= post.get("attr_fields");
		JSONObject infoJSON							= null;
		String startTime							= null;
		
		try {
			infoJSON 								= new JSONObject(info);
			startTime								= infoJSON.getString("start_time");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		lrs	= LR.where("like_activity_id = '" + likeActivityID + "'").select();
		WishListWechatJsonMessage msg = new WishListWechatJsonMessage();
		msg.setKeynote1Value(CN.REDNEWS_LIKE);
		msg.setRemarkValue(CN.REDNEWS_LIKE_LINK);
		msg.setKeynote2Value(startTime);
		
		/* push */
		String url 	   = SiteConf.BASH_URL + "/rednews/Web/LikeActivity/my/uid/" + ownerUid + "/";
		msg.setUrl(url);
		msg.setFisrtValue(CN.REDNEWS_LIKE_TITLE_MY);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		HttpPost hp = new HttpPost();
		user = U.where("id = '" + ownerUid + "'").find();
		msg.setTouser(user.get("openid"));
		HttpEntity requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
		String response = null;
		Log.record("Receiving the pushtype " + PushInterface.PUSH_TYPE_LIKE_ACTIVITY_PUSH + ", to user id: " + ownerUid + " title :" + msg.getFisrtValue() + "link :" + msg.getUrl(), Log.INFO);
		try {
			String access_token = this.getAccessToken();
			if(!DeveloperConf.PUSH_DEBUG) {
				response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
//				Log.record(response, Log.INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < lrs.size(); i++) {

			user = U.where("id = '" + Integer.parseInt(lrs.get(i).get("uid")) + "'").find();

			/* Dispose the TPL variables */			
			int currentUid = Integer.parseInt(lrs.get(i).get("uid"));
			url 	   = SiteConf.BASH_URL + "/rednews/Web/LikeActivity/other/uid/" + ownerUid + "/";
			msg.setUrl(url);
			msg.setFisrtValue(CN.REDNEWS_LIKE_TITLE_OTHER);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			hp = new HttpPost();
			msg.setTouser(user.get("openid"));
			requestBodies = new StringEntity(msg.getJSON(), Consts.UTF_8);
			response = null;
			Log.record("Receiving the pushtype " + PushInterface.PUSH_TYPE_LIKE_ACTIVITY_PUSH + ", to user id: " + ownerUid + " title :" + msg.getFisrtValue() + "link :" + msg.getUrl(), Log.INFO);
			try {
				String access_token = this.getAccessToken();
				if(!DeveloperConf.PUSH_DEBUG) {
					response = hp.post(WechatConf.HOST, WechatConf.TPL_MSG_URI + "?access_token=" + access_token, requestBodies);
//					Log.record(response, Log.INFO);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Get valid access token.
	 * 
	 * @return access token
	 */
	public String getAccessToken() throws Exception{
		if(this.access_token == null) {
			this.access_token = new KV().get(KV.ACCESS_TOKEN);
		}
		if(this.access_token == "") {
			throw new Exception("ACCESS_TOKEN is invalid.");
		}
		return this.access_token;
	}
	
}
