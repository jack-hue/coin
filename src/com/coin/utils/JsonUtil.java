package com.coin.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * JSON数据解析操作类
 */
public final class JsonUtil {

	/**
	 * 从一个JSON 对象字符格式中得到一个java对象，形如： {"id" : idValue, "name" : nameValue,
	 * "aBean" : {"aBeanId" : aBeanIdValue, ...}}
	 * 
	 * @param
	 * @param clazz
	 * @return
	 */
	public static<T> T getDTO(String jsonString, Class<T> clazz) {
			return JSONObject.parseObject(jsonString,clazz);
	}

	public static String getSting(Object obj){
		return JSONObject.toJSONString(obj);
	}
    	

}
