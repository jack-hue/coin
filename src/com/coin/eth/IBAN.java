package com.coin.eth;

import java.math.BigInteger;
import java.text.Format;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.Integer.parseInt;

/**
 * 
 * All rights Reserved, Designed By 郑州好聚点科技有限公司
 * @Description:根据官方规则生成iban及付款二维码
 * @author: auth    
 * @date:   2018年7月4日 下午2:28:43   
 * @Copyright: 2018 http://www.hjd123.com Inc. All rights reserved.
 */
public class IBAN {

	/**
	 * 根据官方支持的IBAN规则生成二维码 目前支持的有imtoken kcash
	 * 参考url
	 * https://github.com/ethereum/web3.js/blob/develop/lib/web3/iban.js
	 * 可以防止地址错误（有两位校验和）
	 */
	public static void main(String[] args) {
		getIBAN();
	}

	public static void getIBAN() {
		String address = "0xaaae432f77a74a33c5e5b47612dabef44a905de6".toLowerCase();
		System.out.println(address);
		address = address.substring(2);
		BigInteger value = new BigInteger(address, 16);
		StringBuilder bban = new StringBuilder(value.toString(36).toUpperCase());
		while (bban.length() < 15 * 2) {
			bban.insert(0, '0');
		}
		System.out.println("bban " + bban);
		String iban = "XE00" + bban;

		iban = iban.substring(4) + iban.substring(0, 4);
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < iban.length(); i++) {
			char chr = iban.charAt(i);
			if (chr >= 'A' && chr <= 'Z') {
				int temp = chr - 'A' + 10;
				code.append(String.valueOf(temp));
			} else {
				code.append(String.valueOf((chr - '0')));
			}
		}
//		System.out.println(code);
		String remainder = code.toString();
		String block;
		while (remainder.length() > 2) {
			int endPoint = remainder.length() >= 9 ? 9 : remainder.length();
			block = remainder.substring(0, endPoint);
			remainder = parseInt(block, 10) % 97 + remainder.substring(block.length());
//			System.out.println(remainder);
		}

		int checkNum = parseInt(remainder, 10) % 97;
		String checkDigit = ("0" + (98 - checkNum));
		checkDigit = checkDigit.substring(checkDigit.length() - 2);
//		System.out.println(checkDigit);
		String IBAN = "XE" + checkDigit + bban;
		String qrCodeString = "iban:" + IBAN + "?token=ETH&amount=5";
		System.out.println("IBAN " + IBAN);
		System.out.println("验证 " + validateIBAN(IBAN));
		System.out.println("qrcode " + qrCodeString);
		decodeQRString(qrCodeString);
	}

	public static boolean validateIBAN(String iban) {
		int len = iban.length();
		if (len < 4 || !iban.matches("[0-9A-Z]+"))
			return false;

		iban = iban.substring(4) + iban.substring(0, 4);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++)
			sb.append(Character.digit(iban.charAt(i), 36));

		BigInteger bigInt = new BigInteger(sb.toString());

		return bigInt.mod(BigInteger.valueOf(97)).intValue() == 1;
	}

	public static void decodeQRString(String result) {
		int ibanEndpoint = result.indexOf("?");
		String iban = result.substring(5, ibanEndpoint < 0 ? result.length() : ibanEndpoint);
		String address = IBAN2Address(iban);
		String query = result.substring(ibanEndpoint + 1, result.length());
		String[] params = query.split("&");
		String token = null;
		String amount = null;
		for (String param : params) {
			if (param.startsWith("token=")) {
				token = param.substring(6);
				continue;
			}
			if (param.startsWith("amount=")) {
				amount = param.substring(7);
			}
		}
		System.out.println("decodeQRString");
		System.out.println("address " + address);
		System.out.println("token " + token);
		System.out.println("amount " + amount);
	}

	public static String IBAN2Address(String iban) {
		String base36 = iban.substring(4);
		StringBuilder base16 = new StringBuilder(new BigInteger(base36, 36).toString(16));
		while (base16.length() < 20) {
			base16.insert(0, "0");
		}
		return "0x" + base16.toString().toLowerCase();
	}
}
