package com.coin.neo;

import com.coin.neo.Core.Scripts.Program;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Byte Handle Helper
 * 
 */
public class Helper {
    /**
     * 以十进制格式显示byte 数组，中间以空格分隔。
     * @param bs
     * @return 以十进制格式显示byte 数组，中间以空格分隔。
     */
	public static String getByteStr(byte[] bs)  {
    	StringBuilder sb = new StringBuilder();
    	for(byte b: bs) {
    		sb.append(" ").append(Byte.toUnsignedInt(b));
    	}
    	return sb.substring(1);
    }
	
    public static byte[] reverse(byte[] v) {
        byte[] result = new byte[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = v[v.length - i - 1];
        }
        return result;
    }
    
    public static byte[] hexToBytes(String value) {
        if (value == null || value.length() == 0) {
            return new byte[0];
        }
        if (value.length() % 2 == 1) {
            throw new IllegalArgumentException();
        }
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
    
    public static String toHexString(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            int v = Byte.toUnsignedInt(b);
            sb.append(Integer.toHexString(v >>> 4));
            sb.append(Integer.toHexString(v & 0x0f));
        }
        return sb.toString();
    }

    public static String reverse(String value) {
    	return toHexString(reverse(hexToBytes(value)));
    }

    public static byte[] removePrevZero(byte[] bt) {
		if(bt.length == 33 && bt[0] == 0) {
			return Arrays.copyOfRange(bt, 1, 33);
		}
		return bt;
	}

    /**
     * @deprecated 未被使用
     */
	public static String now() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
	}
    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    /**
     * @deprecated 未被使用
     */
    public static String getCodeHash(String codeHexStr){
        UInt160 code = Program.toScriptHash(Helper.hexToBytes(codeHexStr));
        String codeHash = Helper.toHexString(code.toArray());
        return codeHash;
    }
}
