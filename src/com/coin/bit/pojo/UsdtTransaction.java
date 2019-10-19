package com.coin.bit.pojo;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

public class UsdtTransaction {
	private BigDecimal amount;
	private String ecosystem;
	private String propertyname;
	private String data;
	private boolean divisible;
	private BigDecimal fee;
	private String propertytype;
	private String txid;
	private boolean ismine;
	private String type;
	private long confirmations;
	private String version;
	private String url;
	private String sendingaddress;
	private boolean valid;
	private String blockhash;
	private long blocktime;
	private long positioninblock;
	private long block;
	private String category;
//	private Category category;
	private String subcategory;
	private String propertyid;
	private Integer type_int;
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getEcosystem() {
		return ecosystem;
	}
	public void setEcosystem(String ecosystem) {
		this.ecosystem = ecosystem;
	}
	public String getPropertyname() {
		return propertyname;
	}
	public void setPropertyname(String propertyname) {
		this.propertyname = propertyname;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public boolean isDivisible() {
		return divisible;
	}
	public void setDivisible(boolean divisible) {
		this.divisible = divisible;
	}
	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	public String getPropertytype() {
		return propertytype;
	}
	public void setPropertytype(String propertytype) {
		this.propertytype = propertytype;
	}
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public boolean isIsmine() {
		return ismine;
	}
	public void setIsmine(boolean ismine) {
		this.ismine = ismine;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getConfirmations() {
		return confirmations;
	}
	public void setConfirmations(long confirmations) {
		this.confirmations = confirmations;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSendingaddress() {
		return sendingaddress;
	}
	public void setSendingaddress(String sendingaddress) {
		this.sendingaddress = sendingaddress;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getBlockhash() {
		return blockhash;
	}
	public void setBlockhash(String blockhash) {
		this.blockhash = blockhash;
	}
	public long getBlocktime() {
		return blocktime;
	}
	public void setBlocktime(long blocktime) {
		this.blocktime = blocktime;
	}
	public long getPositioninblock() {
		return positioninblock;
	}
	public void setPositioninblock(long positioninblock) {
		this.positioninblock = positioninblock;
	}
	public long getBlock() {
		return block;
	}
	public void setBlock(long block) {
		this.block = block;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSubcategory() {
		return subcategory;
	}
	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}
	public String getPropertyid() {
		return propertyid;
	}
	public void setPropertyid(String propertyid) {
		this.propertyid = propertyid;
	}
	public Integer getType_int() {
		return type_int;
	}
	public void setType_int(Integer type_int) {
		this.type_int = type_int;
	}
	

	

    
    
}
