package com.coin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class HttpUtil {
	public static  Logger log = Logger.getLogger(HttpUtil.class);
	 private static final String CHARSET = "UTF-8";
	    private static final String HTTP_POST = "POST";
	    private static final String HTTP_GET = "GET";

	    private static final String HTTP_PUT = "PUT";

	    /**
	     * Send GET request
	     */
	    public static String get(String url, Map<String, String> queryParas, Map<String, String> headers) {
	        HttpURLConnection conn = null;
	        try {
	            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), HTTP_GET, headers);
	            conn.connect();
	            return readResponseString(conn);
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
	    }

	    public static String get(String url, Map<String, String> queryParas) {
	        return get(url, queryParas, null);
	    }

	    public static String get(String url) {
	        return get(url, null, null);
	    }

	    public static String jsonGet(String url,Map<String,String> params){
	        Map<String,String> headers = new HashMap<>();
	        headers.put("Content-Type","application/json");
	        return get(url,params,headers);
	    }


	    /**
	     * Send POST request
	     */
	    public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
	        HttpURLConnection conn = null;
	        try {
	            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), HTTP_POST, headers);
	            conn.connect();
	            OutputStream out = conn.getOutputStream();
	            out.write(data.getBytes(CHARSET));
	            out.flush();
	            out.close();
	            return readResponseString(conn);
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
	    }

	    public static String post(String url, Map<String, String> queryParas, String data) {
	        return post(url, queryParas, data, null);
	    }

	    public static String post(String url, String data, Map<String, String> headers) {
	        return post(url, null, data, headers);
	    }

	    public static String post(String url, String data) {
	        return post(url, null, data, null);
	    }

	    public static String jsonPost(String url,String data){
	        Map<String,String> headers = new HashMap<>();
	        headers.put("Content-Type","application/json");
	        return post(url,null,data,headers);
	    }

	    public static String jsonPost(String url,Map<String,String>headers,String data){
	        if(headers == null){
	            headers = new HashMap<>();
	        }
	        headers.put("Content-Type","application/json");
	        return post(url,null,data,headers);
	    }

	    /**
	     * Send POST request
	     */
	    public static String put(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
	        HttpURLConnection conn = null;
	        try {
	            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), HTTP_PUT, headers);
	            conn.connect();
	            OutputStream out = conn.getOutputStream();
	            out.write(data.getBytes(CHARSET));
	            out.flush();
	            out.close();
	            return readResponseString(conn);
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
	    }



	    public static String jsonPut(String url,String data){
	        Map<String,String> headers = new HashMap<>();
	        headers.put("Content-Type","application/json");
	        return put(url,null,data,headers);
	    }


	    /**
	     * https 域名校验
	     */
	    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
	        @Override
	        public boolean verify(String hostname, SSLSession session) {
	            return true;
	        }
	    }

	    /**
	     * https 证书管理
	     */
	    private static class TrustAnyTrustManager implements X509TrustManager {
	        @Override
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	        @Override
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        }
	        @Override
	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        }
	    }

	    private static SSLSocketFactory initSSLSocketFactory() {
	        try {
	            TrustManager[] tm = {new TrustAnyTrustManager()};
	            SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
	            sslContext.init(null, tm, new java.security.SecureRandom());
	            return sslContext.getSocketFactory();
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }

	    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
	    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new TrustAnyHostnameVerifier();

	    private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws Exception {
	        URL _url = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
	        if (conn instanceof HttpsURLConnection) {
	            ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
	            ((HttpsURLConnection)conn).setHostnameVerifier(trustAnyHostnameVerifier);
	        }
	        conn.setRequestMethod(method);
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setConnectTimeout(30000);
	        conn.setReadTimeout(30000);
	        conn.setUseCaches(false); // Post 请求不能使用缓存
	        if(headers != null){
	            String contentType = headers.get("Content-Type");
	            if(StringUtils.isNotEmpty(contentType)){
	                conn.setRequestProperty("Content-Type",contentType);
	            }else{
	                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
	            }
	        }
	        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
	        if (headers != null && !headers.isEmpty())
	            for (Map.Entry<String, String> entry : headers.entrySet())
	                conn.setRequestProperty(entry.getKey(), entry.getValue());

	        return conn;
	    }

	    private static String readResponseString(HttpURLConnection conn) {
	        StringBuilder sb = new StringBuilder();
	        InputStream inputStream = null;
	        try {
	            inputStream = conn.getInputStream();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
	            String line = null;
	            while ((line = reader.readLine()) != null){
	                sb.append(line).append("\n");
	            }
	            return sb.toString();
	        }
	        catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        finally {
	            if (inputStream != null) {
	                try {
	                    inputStream.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

	    /**
	     * Build queryString of the url
	     */
	    private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
	        if (queryParas == null || queryParas.isEmpty())
	            return url;

	        StringBuilder sb = new StringBuilder(url);
	        boolean isFirst;
	        if (url.indexOf("?") == -1) {
	            isFirst = true;
	            sb.append("?");
	        }
	        else {
	            isFirst = false;
	        }

	        for (Map.Entry<String, String> entry : queryParas.entrySet()) {
	            if (isFirst) isFirst = false;
	            else sb.append("&");

	            String key = entry.getKey();
	            String value = entry.getValue();
	            if (!StringUtils.isEmpty(value)){
	                try {value = URLEncoder.encode(value, CHARSET);} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
	                sb.append(key).append("=").append(value);
	            }
	        }
	        return sb.toString();
	    }
	 public static String httpGet(String url) throws HttpException, IOException{
		 String retVal="";
		 GetMethod getMethod=null;
		 try {
			 org.apache.commons.httpclient.HttpClient httpClient=new org.apache.commons.httpclient.HttpClient();
			  getMethod=new GetMethod(url);
			 int statusCode=httpClient.executeMethod(getMethod);
			 retVal=getMethod.getResponseBodyAsString();
			 if(statusCode!=200){
				 retVal="-1"; 
			 }
		} finally{
			getMethod.releaseConnection();
		}
		return retVal;
	 }
	 public static String httpPost(String url,List<org.apache.commons.httpclient.NameValuePair> list) throws HttpException, IOException{
		 String retVal="";
		 PostMethod postMethod=null;
		 try {
			 org.apache.commons.httpclient.HttpClient httpClient=new org.apache.commons.httpclient.HttpClient();
			  postMethod=new PostMethod(url);
			  if(list!=null){
				  org.apache.commons.httpclient.NameValuePair[] params=new org.apache.commons.httpclient.NameValuePair[list.size()];
					for(int i=0;i<list.size();i++)
					{
						params[i]=list.get(i);
					}
				    postMethod.addParameters( (org.apache.commons.httpclient.NameValuePair[]) params);
			  }
			 int statusCode=httpClient.executeMethod(postMethod);
			 retVal=postMethod.getResponseBodyAsString();
			 if(statusCode!=200){
				 retVal="-1"; 
			 }
		} finally{
			postMethod.releaseConnection();
		}
			return retVal;
	 }
		
}
