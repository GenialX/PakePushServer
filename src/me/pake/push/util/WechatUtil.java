package me.pake.push.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import me.pake.push.conf.WechatConf;
import me.pake.push.model.KV;

import org.json.JSONException;
import org.json.JSONObject;

/** 
 * 公众平台通用接口工具类 
 *  
 * @author liuyq 
 * @date 2013-08-09 
 */
public class WechatUtil {
	
	public final static String REQUEST_GET = "GET";
	public final static String REQUEST_POST = "POST";
  
    /** 
     * 发起HTTPS请求并获取结果 
     *  
     * @param requestUrl 请求地址 
     * @param requestMethod 请求方式（GET、POST） 
     * @param outputStr 提交的数据 
     * @return JSONObject(通过JSONObject.get(key)的方式获取JSON对象的属性值) 
     */  
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {  
        JSONObject jsonObject = null;  
        StringBuffer buffer = new StringBuffer();  
        try {  
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
            TrustManager[] tm = { new PushX509TrustManager() };  
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
            sslContext.init(null, tm, new java.security.SecureRandom());  
            // 从上述SSLContext对象中得到SSLSocketFactory对象  
            SSLSocketFactory ssf = sslContext.getSocketFactory();  
  
            URL url = new URL(requestUrl);  
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
            httpUrlConn.setSSLSocketFactory(ssf);  
  
            httpUrlConn.setDoOutput(true);  
            httpUrlConn.setDoInput(true);  
            httpUrlConn.setUseCaches(false);  
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod(requestMethod);  
  
            if ("GET".equalsIgnoreCase(requestMethod))  
                httpUrlConn.connect();  
  
            // 当有数据需要提交时  
            if (null != outputStr) {  
                OutputStream outputStream = httpUrlConn.getOutputStream();  
                // 注意编码格式，防止中文乱码  
                outputStream.write(outputStr.getBytes("UTF-8"));  
                outputStream.close();  
            }  
  
            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();  
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
  
            String str = null;  
            while ((str = bufferedReader.readLine()) != null) {  
                buffer.append(str);  
            }  
            bufferedReader.close();  
            inputStreamReader.close();  
            // 释放资源  
            inputStream.close();  
            inputStream = null;  
            httpUrlConn.disconnect();  
            jsonObject = new JSONObject(buffer.toString());
        } catch (ConnectException ce) {  
        	ce.printStackTrace();
        } catch (Exception e) {  
            e.printStackTrace();
        }  
        return jsonObject;  
    }  
    
    /**
     * Get "access_token" from wechat server.
     * 
     * While the "access_token" in database is invalid, this method may be used.
     * 
     * @return access_token value
     */
	public static String getAccessToken() {
		KV kv = new KV();
		String APPID = kv.get(KV.APPID);
		String APPSECRET = kv.get(KV.APPSECRET);
		String URL = "https://" + WechatConf.HOST + WechatConf.ACCESS_TOKEN_URI + "?grant_type=client_credential&appid=" + APPID + "&secret=" + APPSECRET;
		String access_token = "";
		JSONObject jsonObj = null;
		jsonObj = WechatUtil.httpRequest(URL, WechatUtil.REQUEST_GET, null);
		try {
			access_token = jsonObj.getString("access_token");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return access_token;
	}
	
}  