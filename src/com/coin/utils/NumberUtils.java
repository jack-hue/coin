package com.coin.utils;

import java.math.BigDecimal;

public class NumberUtils {
	public static BigDecimal roundBigDecimal(BigDecimal bigDecimal, int scale) {
		if (bigDecimal == null) {
			bigDecimal = BigDecimal.valueOf(0);
			return BigDecimal.valueOf(0);
		}
		return bigDecimal.divide(new BigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP);
	}
	public static BigDecimal subNumber(double num, int scale) {
		return BigDecimal.valueOf(num).setScale(scale, BigDecimal.ROUND_DOWN);
	}
}
