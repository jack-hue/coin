package com.coin.bit.pojo;

import java.io.Serializable;

public class UnSpentUtxo  implements Serializable{
private static final long serialVersionUID = -7417428486644921613L;
	
	private String hash;//交易hash
	private long txN; //
	private long value;//金额
	private int height;//区块高度
	private String script;//hex
	private String address;//钱包地址
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public long getTxN() {
		return txN;
	}
	public void setTxN(long txN) {
		this.txN = txN;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
