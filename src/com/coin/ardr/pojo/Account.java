package com.coin.ardr.pojo;

public class Account {

	private String accountRS;
	private String publicKey;
	private String requestProcessingTime;
	private String account;
	private String passWord;
	public String getAccountRS() {
		return accountRS;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public void setAccountRS(String accountRS) {
		this.accountRS = accountRS;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getRequestProcessingTime() {
		return requestProcessingTime;
	}
	public void setRequestProcessingTime(String requestProcessingTime) {
		this.requestProcessingTime = requestProcessingTime;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
}
