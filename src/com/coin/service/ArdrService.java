package com.coin.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.coin.ardr.pojo.Account;
import com.coin.ardr.pojo.Balance;
import com.coin.ardr.pojo.Shufflings;
import com.coin.utils.HttpUtil;
import com.coin.utils.StringUtils;

public class ArdrService  {
	private Logger logger = Logger.getLogger(getClass());
	public static final int RATE = 100000000;
	private final String CREATE_ACCOUNT = "nxt?requestType=getAccountId&secretPhrase=";
	private final String GET_ACCOUNT = "nxt?requestType=getAccount&account=ACCOUNTNUMBER&chain=CHAIN&random=";
	private final String PUBLICKEY = "nxt?requestType=getAccountPublicKey&account=ACCOUNTNUMBER&chain=CHAIN&random=";
	private final String GET_GUARANTEED_BALANCE = "nxt?requestType=getGuaranteedBalance&account=ACCOUNTNUMBER&numberOfConfirmations=CONFIRMNUMBER";
	private final String GET_BALANCE = "nxt?requestType=getBalance&account=ACCOUNTNUMBER&chain=CHAIN";
	private final String GET_ACCOUNT_PHASED_TRANSACTION_COUNT = "nxt?requestType=getAccountPhasedTransactionCount&account=ACCOUNTNUMBER&chain=CHAIN";
	private final String GET_ALLSHUFFLINGS = "nxt?requestType=getAllShufflings";
	private final String GET_TIME = "nxt?requestType=getTime&chain=CHAIN&random=";
	private final String SEND_MONEY = "nxt?requestType=sendMoney";
	private final String GET_FEE = "nxt?requestType=sendMoney";
	private final String GET_BLOCKCHAIN_TRANSACTIONS = "nxt?requestType=getBlockchainTransactions&account=ACCOUNTNUMBER&firstIndex=FIRSTINDEX&lastIndex=LASTINDEX&numberOfConfirmations=NUMBEROFCONFIRMATIONS&chain=CHAIN";
	private final String GET_CONFIRMATIONS = "nxt?requestType=getTransaction&transaction=TRANSACTION&fullHash=FULLHASH&chain=CHAIN";
	private final String BROADCAST_TRANSACTION = "nxt?requestType=broadcastTransaction";
	private final String GET_TRANSACTIONBYTES = "nxt?requestType=getTransactionBytes&transaction=TRANSACTION&fullHash=FULLHASH&chain=CHAIN";

	private String url;


	public ArdrService(String url) {
		this.url = url;
	}

	public static void main(String[] args) {
		ArdrService ardrFactory = new ArdrService( "http://127.0.0.1:26876/");
		String txid = ardrFactory.sendMoney(
				"point break perfect suppose look minute help dawn three shore sympathy taught",
				"ARDOR-K26W-FTNQ-8DTF-7DLDP", 0.8, "1", "1");
		System.out.println(txid);
	}

	public Account createAccount() {
		Account account = null;
		try {
			String pwd = StringUtils.getRandomStringByLength(32);
			String retVal = HttpUtil.httpGet(url + CREATE_ACCOUNT + pwd);
			Gson gson = new Gson();
			account = gson.fromJson(retVal, Account.class);
			account.setPassWord(pwd);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return account;
	}

	// GET_ACCOUNT="/nxt?requestType=getAccount&account=ACCOUNTNUMBER&chain=CHAIN&random=";
	public Account getAccountByAccountNumber(String accountNumber, String chain) {
		Account account = null;
		try {
			if (StringUtils.isEmpty(chain)) {
				chain = "1";
			}
			String urlAccount = GET_ACCOUNT.replace("ACCOUNTNUMBER", accountNumber).replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount + Math.random());
			Gson gson = new Gson();
			account = gson.fromJson(retVal, Account.class);
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getAccountByAccountNumber失败！");
			e.printStackTrace();
		}
		return account;
	}

	public String getPublicKey(String accountNumber, String chain) {
		String str="";
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = PUBLICKEY.replace("ACCOUNTNUMBER", accountNumber).replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount + Math.random());
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("publicKey");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getPublicKey失败！");
			e.printStackTrace();
		}
		return str;
	}

	public String getGuaranteedBalance(String accountNumber, int confirmNumber) {
		String str="";
		try {
			String urlAccount = GET_GUARANTEED_BALANCE.replace("ACCOUNTNUMBER", accountNumber).replace("CONFIRMNUMBER",
					String.valueOf(confirmNumber));
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			BigDecimal balance = jsonObject.getBigDecimal("guaranteedBalanceNQT");
			str = (balance.divide(BigDecimal.valueOf(RATE))).toString();
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getGuaranteedBalance失败！");
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 
	 * 方法描述：获取余额 编写人：auth 编写时间：2018年3月19日
	 * 
	 * @param url
	 * @param accountNumber
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 *             Balance
	 */
	// GET_BALANCE="/nxt?requestType=getBalance&account=ACCOUNTNUMBER&chain=CHAIN";
	public Balance getBalance(String accountNumber, String chain) {
		Balance balance=new Balance();
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_BALANCE.replace("ACCOUNTNUMBER", accountNumber).replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			BigDecimal unconfirmedBalanceNQT = jsonObject.getBigDecimal("unconfirmedBalanceNQT");
			if (unconfirmedBalanceNQT != null) {
				balance.setUnconfirmedBalanceNQT(
						(unconfirmedBalanceNQT.divide(BigDecimal.valueOf(RATE))).doubleValue());
			}
			BigDecimal guaranteedBalanceNQT = jsonObject.getBigDecimal("guaranteedBalanceNQT");
			if (guaranteedBalanceNQT != null) {
				balance.setGuaranteedBalanceNQT((guaranteedBalanceNQT.divide(BigDecimal.valueOf(RATE))).doubleValue());
			}
			BigDecimal effectiveBalanceNXT = jsonObject.getBigDecimal("effectiveBalanceNXT");
			if (effectiveBalanceNXT != null) {
				balance.setEffectiveBalanceNXT((effectiveBalanceNXT.divide(BigDecimal.valueOf(RATE))).doubleValue());
			}
			BigDecimal forgedBalanceNQT = jsonObject.getBigDecimal("forgedBalanceNQT");
			if (forgedBalanceNQT != null) {
				balance.setForgedBalanceNQT((forgedBalanceNQT.divide(BigDecimal.valueOf(RATE))).doubleValue());
			}
			BigDecimal balanceNQT = jsonObject.getBigDecimal("balanceNQT");
			if (balanceNQT != null) {
				balance.setBalanceNQT((balanceNQT.divide(BigDecimal.valueOf(RATE))).doubleValue());
			}

		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getBalance失败！");
			e.printStackTrace();
		}
		return balance;
	}

	/**
	 * 
	 * 方法描述：检索有关所有洗牌的信息 编写人：auth 编写时间：2018年3月19日
	 * 
	 * @param url
	 * @param accountNumber
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 *             Balance
	 */
	// GET_ALLSHUFFLINGS="/nxt?requestType=getAllShufflings";
	@Deprecated
	public Shufflings getAllShufflings(String cxt) {
		Shufflings shufflings=null;
		try {
			String urlAccount = GET_ALLSHUFFLINGS;
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			String shufflingsStr = jsonObject.getString("shufflings");
			shufflingsStr = shufflingsStr.replace("[", "").replace("]", "");
			Gson gson = new Gson();
			shufflings = gson.fromJson(shufflingsStr, Shufflings.class);

		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getAllShufflings失败！");
			e.printStackTrace();
		}
		return shufflings;
	}

	/**
	 * 
	 * 方法描述：获取账户分期交易计数 编写人：auth 编写时间：2018年3月19日
	 * 
	 * @param url
	 * @param accountNumber
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 *             Balance
	 */
	// GET_ACCOUNT_PHASED_TRANSACTION_COUNT="/nxt?requestType=getAccountPhasedTransactionCount&account=ACCOUNTNUMBER";
	@Deprecated
	public String getAccountPhasedTransactionCount(String accountNumber, String chain) {
		String str="";
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_ACCOUNT_PHASED_TRANSACTION_COUNT.replace("ACCOUNTNUMBER", accountNumber)
					.replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("numberOfPhasedTransactions");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getAccountPhasedTransactionCount失败！");
			e.printStackTrace();
		}
		return str;
	}

	public String getTime(String chain) {
		String str=null;
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_TIME.replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount + Math.random());
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("time");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getTime失败！");
			e.printStackTrace();
		}
		return str;
	}

	public String getGuaranteedBalance(String accountNumber) {
		try {
			return getGuaranteedBalance(accountNumber, 1440);
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getGuaranteedBalance失败！");
			e.printStackTrace();
		}
		return null;
	}

	public synchronized String sendMoney(String pwd, String accountRS, double amount, String chain) {
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			DecimalFormat decimalFormat = new DecimalFormat("#");
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			NameValuePair p1 = new NameValuePair("secretPhrase", pwd);
			list.add(p1);
			NameValuePair p2 = new NameValuePair("recipient", accountRS);
			list.add(p2);
			NameValuePair p3 = new NameValuePair("calculateFee", "true");
			list.add(p3);
			NameValuePair p4 = new NameValuePair("amountNQT", decimalFormat.format(amount * RATE));
			list.add(p4);
			NameValuePair p5 = new NameValuePair("chain", chain);
			list.add(p5);
			String retVal = HttpUtil.httpPost((url + GET_FEE), list);
			logger.error("==============阿朵币转账返回："+retVal);
			JSONObject jsonRetVal = JSONObject.parseObject(retVal);
			JSONObject json = jsonRetVal.getJSONObject("transactionJSON");
			String fee = json.getString("minimumFeeFQT");
			return sendMoney(pwd, accountRS, amount, fee, chain);
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr sendMoney失败！");
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public synchronized String sendMoney(String pwd, String accountRS, double amount, String fee, String chain) {
		String txid = null;
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			DecimalFormat decimalFormat = new DecimalFormat("#");
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			NameValuePair p1 = new NameValuePair("secretPhrase", pwd);
			list.add(p1);
			NameValuePair p2 = new NameValuePair("recipient", accountRS);
			list.add(p2);
			NameValuePair p3 = new NameValuePair("amountNQT", decimalFormat.format(amount * RATE));
			list.add(p3);
			NameValuePair p4 = new NameValuePair("feeNQT", fee);
			list.add(p4);
			NameValuePair p5 = new NameValuePair("chain", chain);
			list.add(p5);

			String retVal = HttpUtil.httpPost((url + SEND_MONEY), list);
			logger.error("==============阿朵币转账返回："+retVal);
			JSONObject jsonRetVal = JSONObject.parseObject(retVal);
			JSONObject json = jsonRetVal.getJSONObject("transactionJSON");
			txid = json.getString("transaction");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr sendMoney失败！");
			e.printStackTrace();
		}
		return txid;
	}

	public String getConfirmations(String transaction, String fullHash, String chain) {
		String str=null;
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_CONFIRMATIONS.replace("TRANSACTION", transaction).replace("FULLHASH", fullHash)
					.replace("CHAIN", chain);
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("confirmations");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getConfirmations失败！");
			e.printStackTrace();
		}
		return str;
	}
	public String broadcastTransaction(String transactionBytes) {
		String str=null;
		try {
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			NameValuePair p1 = new NameValuePair("transactionBytes", transactionBytes);
			list.add(p1);
			String retVal = HttpUtil.httpPost((url + BROADCAST_TRANSACTION), list);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("fullHash");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr broadcastTransaction失败！");
			e.printStackTrace();
		}
		return str;
	}

	public String getTransactionBytes(String transaction, String fullHash, String chain) {
		String str=null;
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_TRANSACTIONBYTES.replace("TRANSACTION", transaction).replace("FULLHASH", fullHash)
					.replace("CHAIN", chain);
			;
			String retVal = HttpUtil.httpGet(url + urlAccount);
			JSONObject jsonObject = JSONObject.parseObject(retVal);
			str = jsonObject.getString("transactionBytes");
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getTransactionBytes失败！");
			e.printStackTrace();
		}
		return str;
	}

	public String getBlockchainTransactions(String accountNumber, String chain) {
		return getBlockchainTransactions(accountNumber, null, null, null, chain);
	}
	public String getBlockchainTransactions(String accountNumber, String firstIndex, String lastIndex,
			String numberOfConfirmations, String chain) {
		String retVal = null;
		try {
			if (StringUtils.isBlank(chain)) {
				chain = "1";
			}
			String urlAccount = GET_BLOCKCHAIN_TRANSACTIONS.replace("ACCOUNTNUMBER", accountNumber).replace("CHAIN",
					chain);
			if (StringUtils.isBlank(firstIndex)) {
				urlAccount = urlAccount.replace("&firstIndex=FIRSTINDEX", "");
			} else {
				urlAccount = urlAccount.replace("FIRSTINDEX", firstIndex);
			}

			if (StringUtils.isBlank(lastIndex)) {
				urlAccount = urlAccount.replace("&lastIndex=LASTINDEX", "");
			} else {
				urlAccount = urlAccount.replace("LASTINDEX", lastIndex);
			}
			if (StringUtils.isBlank(numberOfConfirmations)) {
				urlAccount = urlAccount.replace("&numberOfConfirmations=NUMBEROFCONFIRMATIONS", "");
			} else {
				urlAccount = urlAccount.replace("NUMBEROFCONFIRMATIONS", numberOfConfirmations);
			}
			retVal = HttpUtil.httpGet(url + urlAccount);
		} catch (Exception e) {
			logger.error("==============虚拟币-Ardr getBlockchainTransactions失败！");
			e.printStackTrace();
		}
		return retVal;
	}

}
