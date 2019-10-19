package com.coin.utils;

import java.util.Random;


public class StringUtils {
	  public static String getRandomStringByLength(int size) {
	        String str = "abcdefghijklmnopqrstuvwxyz0123456789";
	        Random random = new Random();
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < size; i++) {
	            int number = random.nextInt(str.length());
	            sb.append(str.charAt(number));
	        }
	        return sb.toString();
	    }
	    public static boolean isEmpty(String str)
	    {
	        return str == null || "".equals(str);
	    }
		public static final boolean isBlank(Object obj) {
			return (obj == null || "".equals(obj.toString()));
		}
}
