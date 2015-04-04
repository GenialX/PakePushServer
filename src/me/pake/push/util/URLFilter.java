/**
 * URL参数解析
 */
package me.pake.push.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.Iterator;

public class URLFilter {
	
	// the url need filtering
	private String url;
	
	// the url map
	private Map<String, String> urlMap;
	
	// the action(api) name
	private String actionName;
	
	public URLFilter(String _url) {
		this.url = _url;
		this.getParams();
	}
	
	// initial the params from the url[private string]
	private void getParams() {
		if(this.url.indexOf("?") > 0) { // whether is not contains get params
			
			this.actionName = this.url.substring(1, this.url.indexOf("?")); // get action(api) name
			
			String paramString = this.url.substring(this.url.indexOf("?") + 1, this.url.length()); // get params string eg:k=value&k=value
			
			if(paramString.indexOf("&") > 0) {
				String[] paramStringSplit = paramString.split("&");
				this.urlMap = new HashMap<String, String>(paramStringSplit.length);
				if(paramStringSplit.length > 0) {
					for(String pss : paramStringSplit) {
						if(pss.indexOf("=") > 0) {
							String[] urlParam = pss.split("=");
							if(urlParam.length > 1) {
								this.urlMap.put(urlParam[0], urlParam[1]);
//								System.out.println("key:" + urlParam[0] + ", " + "value:" + urlParam[1]);
							}
						}
					}
				}
			} else {
				this.urlMap = new HashMap<String, String>(1);
				if(paramString.indexOf("=") > 0) {
					String[] urlParam = paramString.split("=");
					if(urlParam.length > 1) {
						this.urlMap.put(urlParam[0], urlParam[1]);
//						System.out.println("key:" + urlParam[0] + ", " + "value:" + urlParam[1]);
					}
				}
			}
		}
	}
	
	// get the GET params
	public String get(String key) {
		try {
			if(this.urlMap != null) {
				if(this.urlMap.containsKey(key)) {
					return URLDecoder.decode(this.urlMap.get(key), "UTF-8");
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String getActionName() {
		if(this.actionName != null) {
			return this.actionName;
		}
		return "";
	}
	
	// if the get param is set and not ""
	public boolean isset(String key) {
		if(this.urlMap != null) {
			if(this.urlMap.containsKey(key)) {
				if(!this.urlMap.get(key).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	
}
