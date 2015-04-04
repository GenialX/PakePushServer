package me.pake.push.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * this is the JSON message for WeChat push,
 * while some one created or replied one wish list.
 * 
 */
public class WishListWechatJsonMessage implements Message{

	// configuration
	private String defaultColor			= "#191970";
	
	private String touser				= ""; // WeChat OPENID eg:oJY0Fj1mZyycS7MwMMDYDeFUqKWQ
	private String templateId			= "ZDHOX5D0_6PqdKXnsj2Fw6RxnX_jt5bcfDPvYf8bVvI";
	private String url					= "";
	private String color	 			= "#FF0000";
	
	private String fisrtValue			= null;
	private String firstColor			= defaultColor;	
	private String keynote1Value		= null;
	private String keynote1Color		= defaultColor;	
	private String keynote2Value		= null;
	private String keynote2Color		= defaultColor;	
	private String remarkValue			= null;
	private String remarkColor			= defaultColor;
	
	private JSONObject jsonObj 			= new JSONObject();
	
	private JSONObject dataObj	  	 	= new JSONObject();
	private JSONObject firstObj  	 	= new JSONObject();
	private JSONObject keynote1Obj  	= new JSONObject();
	private JSONObject keynote2Obj  	= new JSONObject();
	private JSONObject remarkObj 	 	= new JSONObject();
	
	
	
	public WishListWechatJsonMessage() {}

	@Override
	public int getType() {
		return Message.WISH_LIST_WECHAT_JSON;
	}
	
	// Get JSON string for posting message
	public String getJSON() {
		String json = null;
		try {

			this.firstObj.put("value", this.fisrtValue);
			this.firstObj.put("color", this.firstColor);
			
			this.keynote1Obj.put("value", this.keynote1Value);
			this.keynote1Obj.put("color", this.keynote1Color);
			
			this.keynote2Obj.put("value", this.keynote2Value);
			this.keynote2Obj.put("color", this.keynote2Color);
			
			this.remarkObj.put("value", this.remarkValue);
			this.remarkObj.put("color", this.remarkColor);
			
			this.dataObj.put("first", this.firstObj);
			this.dataObj.put("keynote1", this.keynote1Obj);
			this.dataObj.put("keynote2", this.keynote2Obj);
			this.dataObj.put("remark", this.remarkObj);
			
			this.jsonObj.put("touser", this.touser);
			this.jsonObj.put("template_id", this.templateId);
			this.jsonObj.put("url", this.url);
			this.jsonObj.put("topcolor", this.color);
			this.jsonObj.put("data", this.dataObj);
			
			json = this.jsonObj.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return json;
	}

	public String getTouser() {
		return touser;
	}

	public void setTouser(String touser) {
		this.touser = touser;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getFisrtValue() {
		return fisrtValue;
	}

	public void setFisrtValue(String fisrtValue) {
		this.fisrtValue = fisrtValue;
	}

	public String getFirstColor() {
		return firstColor;
	}

	public void setFirstColor(String firstColor) {
		this.firstColor = firstColor;
	}

	public String getKeynote1Value() {
		return keynote1Value;
	}

	public void setKeynote1Value(String keynote1Value) {
		this.keynote1Value = keynote1Value;
	}

	public String getKeynote1Color() {
		return keynote1Color;
	}

	public void setKeynote1Color(String keynote1Color) {
		this.keynote1Color = keynote1Color;
	}

	public String getKeynote2Value() {
		return keynote2Value;
	}

	public void setKeynote2Value(String keynote2Value) {
		this.keynote2Value = keynote2Value;
	}

	public String getKeynote2Color() {
		return keynote2Color;
	}

	public void setKeynote2Color(String keynote2Color) {
		this.keynote2Color = keynote2Color;
	}

	public String getRemarkValue() {
		return remarkValue;
	}

	public void setRemarkValue(String remarkValue) {
		this.remarkValue = remarkValue;
	}

	public String getRemarkColor() {
		return remarkColor;
	}

	public void setRemarkColor(String remarkColor) {
		this.remarkColor = remarkColor;
	}

}
