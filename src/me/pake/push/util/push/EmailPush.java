/**
 * filter the params by URLFilter handle and send email
 *
 */
package me.pake.push.util.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import me.pake.push.message.BackUsersForWishListMessage;
import me.pake.push.message.NearestUsersForWishListMessage;
import me.pake.push.message.WishListWechatJsonMessage;
import me.pake.push.model.EmailReceive;
import me.pake.push.model.Model;
import me.pake.push.model.User;
import me.pake.push.model.WishMsg;
import me.pake.push.util.HttpPost;
import me.pake.push.util.Log;
import me.pake.push.util.SMTPSend;
import me.pake.push.util.URIFilter;

public class EmailPush extends PushAbstract{
	
	public EmailPush(URIFilter urlFilter) {
		super(urlFilter);
	}
	
	/**
	 * @version 0.1.0 add the {@value PushInterface#PUSH_TYPE_VIRTUAL_WISH_LIST} type. 21/2014/12
	 */
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
		case PushInterface.PUSH_TYPE_PROMOTE_EMAIL:
				this.puseTypePromoteEmail();
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
		
		List<NearestUsersForWishListMessage> users 	= pu.getNearestUsersForWishEmail(fromuserid, lat, lon, 10000.0, 1000, 30, ServerConf.RECEIVE_ACTIVE_HOUR_ACCOUNT, ServerConf.RECEIVE_ACCOUNT);
		
		// construct subject and content data
		String _subject = "";
		String _content = "";
		String url		= SiteConf.BASH_URL + "/wechat/index.php/index/wish/comment/" + info_id;
		// get from user into
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
		
		_subject = "亲，" + nickname + "在你附近发起了一个愿望哦~~";
		_content = _subject + "<br />"
				+ "<b>愿望内容</b>：" + wishMsgContent + "<br />"
				+ "<b>发布时间</b>：" + time + "<br />"
				+ "<b>愿望地址</b>：<a href='" + url + "' target='_blank'>点击查看详情</a>";
		
		// Send emails And update email receive table
		EmailReceive er = new EmailReceive();
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Accept creating wish list event, the users accepted email message account equeal: " + users.size());
		for(NearestUsersForWishListMessage userMessage : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("userid: " + userMessage.getUserid() + " email: " + userMessage.getEmail()  + " set wcnt to " + (userMessage.getWcnt() + 1) );
			if(!DeveloperConf.PUSH_DEBUG) {
				SMTPSend.send(userMessage.getEmail(), _subject, _content);
//				SMTPSend.send("genialx@qq.com", _subject, _content);
				er.setCnt(userMessage.getUserid(), EmailReceive.WCNT, ( userMessage.getWcnt() + 1 ) );
			}
		}
		
		// dispose
		if(er != null) {
			er = null;
		}
		
		if(pu != null) {
			pu = null;
		}
		
		if(users != null) {
			users.clear();
			users = null;
		}
		
		if(fromUser != null) {
			fromUser = null;
		}
		
		if(wishMsg != null) {
			wishMsg = null;
		}
		
		if(fromUserList != null) {
			fromUserList.clear();
			fromUserList = null;
		}
		
		if(wishMsgList != null) {
			wishMsgList.clear();
			wishMsgList = null;
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
		users       	= pu.getBackUsersForWishEmail(fromuserid, info_id);
		
		// construct email info
		String _subject = "";
		String _content = "";
		String url		= SiteConf.BASH_URL + "/wechat/index.php/index/wish/comment/" + info_id;
		
		_subject 		="亲,你的愿望清单里有人留言了，快去看看ta说了什么！";
		_content		= _subject +  "<br />"
						+ "<b>回复时间</b>：" + time + "<br />" 
						+ "<b>愿望地址</b>：<a href='" + url + "' target='_blank'>点击查看详情</a>";

if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Accept replying wish list event, the users accepted email message account equeal: " + users.size());
		for(BackUsersForWishListMessage user : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Email: " + user.getEmail());
			if(!DeveloperConf.PUSH_DEBUG) {
				SMTPSend.send(user.getEmail(), _subject, _content);
//				SMTPSend.send("genialx@qq.com", _subject, _content);
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
	 * Push email message while creating one virtual message.
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
		
		List<NearestUsersForWishListMessage> users 	= pu.getNearestUsersForWishEmail(fromuserid, lat, lon, 10000.0, 1000, 30, ServerConf.RECEIVE_ACTIVE_HOUR_ACCOUNT, ServerConf.RECEIVE_ACCOUNT);
		
		// construct subject and content data
		String _subject = "";
		String _content = "";
		String url		= SiteConf.BASH_URL + "/wechat/index.php/index/wish/comment/" + info_id;
		// get from user into
		List wishMsgList 		= null;
		WishMsg wishMsg 		= new WishMsg();
		wishMsgList 			= wishMsg.getWishMsgById(info_id);
		String wishMsgContent 	= "";
		String nickname			= username;
		
		if(wishMsgList.size() > 0) {
			Map<String, String> wmlm = (Map<String, String>) wishMsgList.get(0);
			wishMsgContent = wmlm.get("fbcontent");
		}
		
		_subject = "亲，" + nickname + "在你附近发起了一个愿望哦~~";
		_content = _subject + "<br />"
				+ "<b>愿望内容</b>：" + wishMsgContent + "<br />"
				+ "<b>发布时间</b>：" + time + "<br />"
				+ "<b>愿望地址</b>：<a href='" + url + "' target='_blank'>点击查看详情</a>";
		
		// Send emails And update email receive table
		EmailReceive er = new EmailReceive();
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("Accept creating virtual wish list event, the users accepted email message account equeal: " + users.size());
		for(NearestUsersForWishListMessage userMessage : users) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
if(DeveloperConf.CONSOLE_DEBUG) System.out.println("userid: " + userMessage.getUserid() + " email: " + userMessage.getEmail()  + " set wcnt to " + (userMessage.getWcnt() + 1) );
			if(!DeveloperConf.PUSH_DEBUG) {
				SMTPSend.send(userMessage.getEmail(), _subject, _content);
//				SMTPSend.send("genialx@qq.com", _subject, _content);
				er.setCnt(userMessage.getUserid(), EmailReceive.WCNT, ( userMessage.getWcnt() + 1 ) );
			}
		}
		
		// dispose
		if(er != null) {
			er = null;
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
	
	private void puseTypePromoteEmail() {
		
		/* Get Params */
		if(!this.filterTypePromoteEmailParams()) return;
		
		/* Get datas */
		int emailPushID								= Integer.parseInt(this.urlFilter.get("email_push_id"));
		Model EP			 						= new Model("email_push");
		Map<String,String> emailPush				= null;
		SimpleDateFormat formatter 					= new SimpleDateFormat("Y-M-d H:m:s");
	 	String now 									= formatter.format(new Date());
	 	String fileName								= "";
	 	
	 	emailPush = EP.where("id = " + emailPushID).find();
		if(emailPush.size() < 1) {
			Log.record("There is not email_push item according to the email_push_id " + emailPushID, Log.ERR);
			return ;
		}
		fileName = SiteConf.EMAIL_PUSH_FILE_PATH + "/" + emailPush.get("file");
		
		/* Push */
		String subject = emailPush.get("description");
		String content = emailPush.get("content");
		/* Read file */
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
Log.record("Read email push file: " + fileName , Log.INFO);
            reader = new BufferedReader(new FileReader(file));
            String email = null;
            int line = 1;
            while ((email = reader.readLine()) != null) {
            	Thread.sleep(3000);
            	if(!DeveloperConf.PUSH_DEBUG) {
            		Log.record("Sended email to " + email + ". email_push id: " + emailPushID + ". subject: " + subject , Log.INFO);
    				SMTPSend.send(email, subject, content);
    			}
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e1) {
                }
            }
        }
	    

				
	}
}
