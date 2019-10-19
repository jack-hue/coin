package com.coin.bit.pojo;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

public class Transactions {
	   private String blockhash;
		private long blockindex;
		private long blocktime;
	private boolean abandoned;
	private String account;
	private String address;
	private BigDecimal amount;
	private String bip125Replaceable;
	private String category;
	private long confirmations;
	private BigDecimal fee;
	private String label;
	private long time;
	private long timereceived;
	private boolean trusted;
	private String txid;
	private long vout;
	private List<String> walletconflicts;
	public boolean isAbandoned() {
		return abandoned;
	}
	public String getBlockhash() {
		return blockhash;
	}
	public void setBlockhash(String blockhash) {
		this.blockhash = blockhash;
	}
	public long getBlockindex() {
		return blockindex;
	}
	public void setBlockindex(long blockindex) {
		this.blockindex = blockindex;
	}
	public long getBlocktime() {
		return blocktime;
	}
	public void setBlocktime(long blocktime) {
		this.blocktime = blocktime;
	}
	public void setAbandoned(boolean abandoned) {
		this.abandoned = abandoned;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getBip125Replaceable() {
		return bip125Replaceable;
	}
	public void setBip125Replaceable(String bip125Replaceable) {
		this.bip125Replaceable = bip125Replaceable;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getConfirmations() {
		return confirmations;
	}
	public void setConfirmations(long confirmations) {
		this.confirmations = confirmations;
	}
	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getTimereceived() {
		return timereceived;
	}
	public void setTimereceived(long timereceived) {
		this.timereceived = timereceived;
	}
	public boolean isTrusted() {
		return trusted;
	}
	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public long getVout() {
		return vout;
	}
	public void setVout(long vout) {
		this.vout = vout;
	}
	public List<String> getWalletconflicts() {
		return walletconflicts;
	}
	public void setWalletconflicts(List<String> walletconflicts) {
		this.walletconflicts = walletconflicts;
	}
	

    
    
}
