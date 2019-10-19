package com.coin.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.coin.utils.BigDecimalUtil;
import com.coin.utils.DateUtils;
import com.coin.utils.HttpUtil;

public class XrpService  {

	private Logger logger = Logger.getLogger(getClass());
	private String getUrl = "https://data.ripple.com";
	private String postUrl = "https://s1.ripple.com:51234";
	private String address;
	private String password;

	private static final String gasFee = "100";
	private static final String COIN_XRP = "XRP";

	private final static String RESULT = "result";
	private final static String SUCCESS = "success";
	private final static String TES_SUCCESS = "tesSUCCESS";

	private final static String METHOD_GET_TRANSACTION = "/v2/accounts/{0}/transactions";
	private final static String METHOD_GET_BALANCE = "/v2/accounts/{0}/balances";
	private final static String METHOD_POST_SIGN = "sign";
	private final static String METHOD_POST_INDEX = "ledger_current";
	private final static String METHOD_POST_ACCOUNT_INFO = "account_info";
	private final static String METHOD_POST_SUBMIT = "submit";

	public XrpService(String address, String password) {
		try {
			this.address = address;
			this.password = password;
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币链接获取失败！");
			e.printStackTrace();
		}
	}

	public XrpService(String address, String password, String url) {
		try {
			this.address = address;
			this.password = password;
			this.getUrl = url;
			this.postUrl = url;
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币链接获取失败！");
			e.printStackTrace();
		}
	}

	public String getAddress() {
		return address;
	}

	/**
	 * 发送交易
	 * 
	 * @param address
	 * @param value
	 * @return
	 */
	public synchronized String send(String toAddress, double value,String tag) {
		try {
			String txBlob = this.sign(toAddress, value,tag);
			if (StringUtils.isEmpty(txBlob)) {
				logger.error("签名失败:{" + toAddress + "}");
				return null;
			}
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("tx_blob", txBlob);
			// 签名
			JSONObject json = doRequest(METHOD_POST_SUBMIT, params);
			if (!isError(json)) {
				logger.error("============瑞波币XRP转账返回！json="+json);
				JSONObject result = json.getJSONObject(RESULT);
				if (result != null) {
					if (TES_SUCCESS.equals(result.getString("engine_result"))) {
						String hash = result.getJSONObject("tx_json").getString("hash");
						if (!StringUtils.isEmpty(hash)) {
							logger.error("转账成功：toAddress:{" + toAddress + "},value:{" + value + "},hash:{" + hash + "}");
							return hash;
						} else {
							logger.error(
									"转账失败：toAddress:{" + toAddress + "},value:{" + value + "},hash:{" + hash + "}");
						}
					}
				}
			}else{
				logger.error("============瑞波币XRP转账失败！json="+json);
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币send失败！");
			e.printStackTrace();
		}
		return null;
	}
	public synchronized String sendOnLine(String toAddress, double value,String tag) {
		try {
			String txBlob = this.signOnLine(toAddress, value,tag);
			if (StringUtils.isEmpty(txBlob)) {
				logger.error("签名失败:{" + toAddress + "}");
				return null;
			}
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("tx_blob", txBlob);
			// 签名
			JSONObject json = doRequest(METHOD_POST_SUBMIT, params);
			if (!isError(json)) {
				logger.error("============瑞波币XRP转账返回！json="+json);
				JSONObject result = json.getJSONObject(RESULT);
				if (result != null) {
					if (TES_SUCCESS.equals(result.getString("engine_result"))) {
						String hash = result.getJSONObject("tx_json").getString("hash");
						if (!StringUtils.isEmpty(hash)) {
							logger.error("转账成功：toAddress:{" + toAddress + "},value:{" + value + "},hash:{" + hash + "}");
							return hash;
						} else {
							logger.error(
									"转账失败：toAddress:{" + toAddress + "},value:{" + value + "},hash:{" + hash + "}");
						}
					}
				}
			}else{
				logger.error("==============瑞波币XRP转账失败！json="+json);
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币send失败！");
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public String signOnLine(String toAddress, Double value,String tag) {
		try {
			// 瑞波币余额存储加六位长度
			value = BigDecimalUtil.mul(value, 1000000);
			Integer vInteger = BigDecimal.valueOf(value).intValue();
			JSONObject txJson = new JSONObject();
			txJson.put("Account", address);
			txJson.put("Amount", vInteger.toString());
			txJson.put("Destination", toAddress);// 标签
			txJson.put("TransactionType", "Payment");
			txJson.put("DestinationTag", tag);
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("secret", password);
			params.put("tx_json", txJson);
			params.put("offline", false);
			// 签名
			JSONObject json = doRequest(METHOD_POST_SIGN, params);
			if (!isError(json)) {
				logger.error("==============XRP签名返回："+json);
				JSONObject result = json.getJSONObject(RESULT);
				if (result != null) {
					if (SUCCESS.equals(result.getString("status"))) {
						return result.getString("tx_blob");
					}
				}
			}else{
				logger.error("瑞波币XRP签名失败！json="+json);
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币signOnLine失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 签名
	 * 
	 * @param address
	 * @param value
	 * @return tx_blob
	 */
	public String sign(String toAddress, Double value,String tag) {
		try {
			value = BigDecimalUtil.mul(value, 1000000);
			Integer vInteger = BigDecimal.valueOf(value).intValue();
			Map<String, String> map = getAccountSequenceAndLedgerCurrentIndex();
			Payment payment = new Payment();
			payment.as(AccountID.Account, address);
			payment.as(AccountID.Destination, toAddress);
			payment.as(UInt32.DestinationTag, tag);
			payment.as(Amount.Amount, vInteger.toString());
			payment.as(UInt32.Sequence, map.get("accountSequence"));
			payment.as(UInt32.LastLedgerSequence, map.get("ledgerCurrentIndex") + 4);
			payment.as(Amount.Fee, gasFee);
			SignedTransaction signed = payment.sign(password);
			if (signed != null) {
				return signed.tx_blob;
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币sign失败！");
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> getAccountSequenceAndLedgerCurrentIndex() {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("account", address);
			params.put("strict", "true");
			params.put("ledger_index", "current");
			params.put("queue", "true");
			JSONObject re = doRequest(METHOD_POST_ACCOUNT_INFO, params);
			if (re != null) {
				JSONObject result = re.getJSONObject("result");
				if (SUCCESS.equals(result.getString("status"))) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("accountSequence", result.getJSONObject("account_data").getString("Sequence"));
					map.put("ledgerCurrentIndex", result.getString("ledger_current_index"));
					return map;
				}
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币getAccountSequenceAndLedgerCurrentIndex失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取用户交易序列号
	 * 
	 * @return
	 */
	public long getAccountSequence() {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("account", address);
			params.put("strict", "true");
			params.put("ledger_index", "current");
			params.put("queue", "true");
			JSONObject re = doRequest(METHOD_POST_ACCOUNT_INFO, params);
			if (re != null) {
				JSONObject result = re.getJSONObject("result");
				if (SUCCESS.equals(result.getString("status"))) {
					return result.getJSONObject("account_data").getLongValue("Sequence");
				}
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币getAccountSequence失败！");
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * 获取最新序列
	 * 
	 * @return
	 */
	public long getLedgerIndex() {
		try {
			JSONObject re = doRequest(METHOD_POST_INDEX);
			if (re != null) {
				JSONObject result = re.getJSONObject("result");
				if (SUCCESS.equals(result.getString("status"))) {
					return result.getLongValue("ledger_current_index");
				}
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币getLedgerIndex失败！");
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * XRP查询余额
	 * 
	 * @return
	 */
	public double getBalance() {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("currency", COIN_XRP);
			String re = HttpUtil.jsonGet(getUrl + MessageFormat.format(METHOD_GET_BALANCE, address), params);
			logger.info("获取XRP余额:{" + re + "}");
			if (!StringUtils.isEmpty(re)) {
				JSONObject json = JSON.parseObject(re);
				if (SUCCESS.equals(json.getString(RESULT))) {
					JSONArray array = json.getJSONArray("balances");
					if (array != null && array.size() > 0) {
						// 总余额
						double balance = array.getJSONObject(0).getDoubleValue("value");
						if (balance >= 20) {
							// 可用余额 xrp会冻结20个币
							return BigDecimalUtil.sub(balance, 20);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币getBalance失败！");
			e.printStackTrace();
		}
		return 0.00;
	}

	public String getTransaction(Date dateGMT, String limit) throws ParseException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("start", DateUtils.formatDate(dateGMT, "yyyy-MM-dd'T'HH:mm:ss"));
			params.put("result", "tesSUCCESS");
			params.put("type", "Payment");
			params.put("limit", limit);
			return HttpUtil.jsonGet(getUrl + MessageFormat.format(METHOD_GET_TRANSACTION, address), params);
			// {"result":"success","count":1,"transactions":[{"hash":"86859CBEFB870C941727E291BB13312BC3D6042D8DA08BF1DEDE6BB55F21B8EC","ledger_index":42268358,"date":"2018-10-16T06:49:30+00:00","tx":{"TransactionType":"Payment","Flags":2147483648,"Sequence":37996,"Amount":"29840000","Fee":"1000","SigningPubKey":"0279154FA62D1F28B6CB1F0264663F19EF3A1718D15363CAC1B47A9C6987231F94","TxnSignature":"304502210089F6526134F335C01C65D3E82284C06E22598A4EAD912604BF4FEAA42D5ACCE402203C044F32D399CEA6F4A16DD6DCBB339A7D0CF76E52C1AB090330A67EDFBABBE4","Account":"rH8yTBLm2SYgcL5HQGDznuNxMiy5ayYcmK","Destination":"raHb1tR1BcNB92nEqxiTuKsDap46szkJG2"},"meta":{"TransactionIndex":1,"AffectedNodes":[{"CreatedNode":{"LedgerEntryType":"AccountRoot","LedgerIndex":"56419B3209DA988033F6F0BD921806B4821A73EDF8C338FB5F06C785E527E967","NewFields":{"Sequence":1,"Balance":"29840000","Account":"raHb1tR1BcNB92nEqxiTuKsDap46szkJG2"}}},{"ModifiedNode":{"LedgerEntryType":"AccountRoot","PreviousTxnLgrSeq":42268355,"PreviousTxnID":"63D5A3121F2706099835D9A3A9E5DA574070A4E08FC70956DB1CC196DE6CDEFC","LedgerIndex":"7D2FC14FA2B344F41BB8A6E10B319301EA1C11705AE159B3E2FA82D9F3079E4C","PreviousFields":{"Sequence":37996,"Balance":"2051629511468"},"FinalFields":{"Flags":131072,"Sequence":37997,"OwnerCount":0,"Balance":"2051599670468","Account":"rH8yTBLm2SYgcL5HQGDznuNxMiy5ayYcmK"}}}],"TransactionResult":"tesSUCCESS","delivered_amount":"29840000"}}]}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币getBalance失败！");
			e.printStackTrace();
		}
		return null;
	}

	public Long parseTransaction(String startTm) throws ParseException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			if (!StringUtils.isEmpty(startTm)) {
				Date d = new Date(BigDecimalUtil.longAdd(Long.parseLong(startTm), 1000L));
				params.put("start", DateUtils.formatDate(d, "yyyy-MM-dd'T'HH:mm:ss"));
			}
			params.put("result", "tesSUCCESS");
			params.put("type", "Payment");
			params.put("limit", "1");
			String re = HttpUtil.jsonGet(getUrl + MessageFormat.format(METHOD_GET_TRANSACTION, address), params);
			if (!StringUtils.isEmpty(re)) {
				JSONObject json = JSON.parseObject(re);
				if (SUCCESS.equals(json.getString(RESULT))) {
					// marker = json.getString("marker");
					JSONArray transactions = json.getJSONArray("transactions");
					if (transactions != null && transactions.size() > 0) {
						for (Object object : transactions) {
							JSONObject transaction = (JSONObject) object;
							String hash = transaction.getString("hash");
							String dateString = transaction.getString("date");
							Date date = DateUtils.parseDate(dateString, "yyyy-MM-dd'T'HH:mm:ss");
							JSONObject tx = transaction.getJSONObject("tx");
							String destinationTag = tx.getString("DestinationTag");
							if (StringUtils.isEmpty(destinationTag)) {
								logger.info("非用户充值记录");
								return date.getTime();
							}
							String to = tx.getString("Destination");
							if (!address.equals(to)) {
								logger.info("非用户充值记录,地址不一致");
								return date.getTime();
							}
							// 校验用户是否存在
							/*
							 * UserEntity user =
							 * userService.getUserById(Integer.parseInt(
							 * destinationTag)); if (user == null) {
							 * logger.info("用户不存在：{}",destinationTag); return
							 * date.getTime(); }
							 */
							double amount = tx.getDoubleValue("Amount");
							if (amount > 0) {
								amount = BigDecimalUtil.div(amount, 1000000, 6);
							} else {
								logger.error("交易金额异常：{" + amount + "}");
								return date.getTime();
							}
							// 添加充值记录
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-瑞波币parseTransaction失败！");
			e.printStackTrace();
		}
		return null;
	}

	private boolean isError(JSONObject json) {
		if (json == null || (!StringUtils.isEmpty(json.getString("error")) && json.get("error") != "null")) {
			return true;
		}
		return false;
	}

	private JSONObject doRequest(String method, Object... params) {
		JSONObject param = new JSONObject();
		param.put("id", System.currentTimeMillis() + "");
		param.put("jsonrpc", "2.0");
		param.put("method", method);
		if (params != null) {
			param.put("params", params);
		}
		String creb = Base64.encodeBase64String((address + ":" + password).getBytes());
		Map<String, String> headers = new HashMap<>(2);
		headers.put("Authorization", "Basic " + creb);
		String resp = "";
		try {
			resp = HttpUtil.jsonPost(postUrl, headers, param.toJSONString());
		} catch (Exception e) {
			logger.info(e.getMessage());
			if (e instanceof IOException) {
				resp = "{}";
			}
		}
		return JSON.parseObject(resp);
	}

}
