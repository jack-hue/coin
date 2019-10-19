package com.coin.ardr.pojo;

public class Balance {
	/**
	 * 可用余额
	 */
private Double unconfirmedBalanceNQT;
/**
 *  保证余额，有小数点，略小于余额
 */
private Double guaranteedBalanceNQT;
/**
 * 有效余额，无小数点，等于余额
 */
private Double effectiveBalanceNXT;
/**
 *  锻造余额
 */
private Double forgedBalanceNQT;
/**
 * 余额
 */
private Double balanceNQT;

/**
 * API请求处理时间（以毫秒为单位）
 */
private Long requestProcessingTime;

public Double getUnconfirmedBalanceNQT() {
	return unconfirmedBalanceNQT;
}
public void setUnconfirmedBalanceNQT(Double unconfirmedBalanceNQT) {
	this.unconfirmedBalanceNQT = unconfirmedBalanceNQT;
}
public Double getGuaranteedBalanceNQT() {
	return guaranteedBalanceNQT;
}
public void setGuaranteedBalanceNQT(Double guaranteedBalanceNQT) {
	this.guaranteedBalanceNQT = guaranteedBalanceNQT;
}
public Double getEffectiveBalanceNXT() {
	return effectiveBalanceNXT;
}
public void setEffectiveBalanceNXT(Double effectiveBalanceNXT) {
	this.effectiveBalanceNXT = effectiveBalanceNXT;
}
public Double getForgedBalanceNQT() {
	return forgedBalanceNQT;
}
public void setForgedBalanceNQT(Double forgedBalanceNQT) {
	this.forgedBalanceNQT = forgedBalanceNQT;
}
public Double getBalanceNQT() {
	return balanceNQT;
}
public void setBalanceNQT(Double balanceNQT) {
	this.balanceNQT = balanceNQT;
}
public Long getRequestProcessingTime() {
	return requestProcessingTime;
}
public void setRequestProcessingTime(Long requestProcessingTime) {
	this.requestProcessingTime = requestProcessingTime;
}

}
