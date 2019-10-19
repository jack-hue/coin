package com.coin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coin.bit.pojo.Account;
import com.coin.bit.pojo.Address;
import com.coin.bit.pojo.Transactions;
import com.coin.bit.pojo.UnSpentUtxo;
import com.coin.utils.HttpUtil;
import com.coin.utils.NumberUtils;

 
public class BtcService {
	private Logger logger = Logger.getLogger(getClass());
    private final static String RESULT = "result";
    private final static String METHOD_SEND_TO_ADDRESS = "sendtoaddress";
    private final static String METHOD_GET_TRANSACTION = "gettransaction";
    private final static String METHOD_LIST_TRANSACTIONS = "listtransactions";
    private final static String METHOD_GET_BLOCK_COUNT = "getblockcount";
    private final static String METHOD_NEW_ADDRESS = "getnewaddress";
    private final static String METHOD_GET_BALANCE = "getbalance";
    private final static String METHOD_WALLET_PASSPHRASE = "walletpassphrase";
    private final static String METHOD_WALLET_LOCK = "walletlock";
    
	private String url;
	private String username;
	private String password;
	public BtcService(String url,String username,String password){
		try {
			this.url=url;
			this.username=username;
			this.password=password;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**  
	 * 离线签名
	    * @Title: signTransaction
	    * @param @param privKey 私钥
	    * @param @param recevieAddr 收款地址
	    * @param @param formAddr 发送地址
	    * @param @param amount 金额
	    * @param @param fee 手续费(自定义 或者 默认)
	    * @param @param unUtxos 未交易的utxo
	    * @param @return    参数  
	    * @return char[]    返回类型  
	    * @throws  
	    */  
	public  String signTransaction(String privKey, String recevieAddr, String formAddr,
																		  long amount, long fee, 
																		  List<UnSpentUtxo> unUtxos) {
//		NetworkParameters params= TestNet3Params.get();
		NetworkParameters params= MainNetParams.get();
		if(!unUtxos.isEmpty() && null != unUtxos){
			List<UTXO> utxos = new ArrayList<UTXO>();
			// String to a private key
			DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privKey);
			ECKey key = dumpedPrivateKey.getKey();
			// 接收地址
			org.bitcoinj.core.Address receiveAddress = org.bitcoinj.core.Address.fromString(params, recevieAddr);
			// 构建交易
			org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(params);
			tx.addOutput(Coin.valueOf(amount), receiveAddress); // 转出
			// 如果需要找零 消费列表总金额 - 已经转账的金额 - 手续费
			long value = unUtxos.stream().mapToLong(UnSpentUtxo::getValue).sum();
			org.bitcoinj.core.Address toAddress = org.bitcoinj.core.Address.fromString(params, formAddr);
			long leave  = value - amount - fee;
			if(leave > 0){
				tx.addOutput(Coin.valueOf(leave), toAddress);
			}
			// utxos is an array of inputs from my wallet
			for (UnSpentUtxo unUtxo : unUtxos) {
				utxos.add(new UTXO(Sha256Hash.wrap(unUtxo.getHash()),
								unUtxo.getTxN(),
								Coin.valueOf(unUtxo.getValue()), 
								unUtxo.getHeight(), 
								false,
								new Script(Utils.HEX.decode(unUtxo.getScript())),
								unUtxo.getAddress()));
			}
			for (UTXO utxo : utxos) {
				TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
				// YOU HAVE TO CHANGE THIS
				tx.addSignedInput(outPoint, utxo.getScript(), key, org.bitcoinj.core.Transaction.SigHash.ALL, true);
			}
			Context context = new Context(params);
			tx.getConfidence().setSource(org.bitcoinj.core.TransactionConfidence.Source.NETWORK);
			tx.setPurpose(org.bitcoinj.core.Transaction.Purpose.USER_PAYMENT);
			
			logger.info("=== [BTC] sign success,hash is :"+tx.getHashAsString());
			return new String(Hex.encodeHex(tx.bitcoinSerialize()));
		}
		return null;
	}
    private JSONObject doRequest(String method,Object... params){
        JSONObject param = new JSONObject();
        param.put("id",System.currentTimeMillis()+"");
        param.put("jsonrpc","2.0");
        param.put("method",method);
        if(params != null){
            param.put("params",params);
        }
        String creb = Base64.encodeBase64String((username+":"+password).getBytes());
        Map<String,String> headers = new HashMap<>(2);
        headers.put("Authorization","Basic "+creb);
        return JSON.parseObject(HttpUtil.jsonPost(url,headers,param.toJSONString()));
    }
    private boolean isError(JSONObject json){
        if( json == null || (StringUtils.isNotEmpty(json.getString("error")) && json.get("error") != "null")){
            return true;
        }
        return false;
    }
	public  String getAddress(String label) {
		try {
//			JSONObject json = doRequest(METHOD_NEW_ADDRESS,label);
			  JSONObject json = doRequest(METHOD_NEW_ADDRESS);
		        if(isError(json)){
		        	logger.error("获取BTC地址失败:"+json.get("error"));
		            return "";
		        }
		        return json.getString(RESULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  long getblockcount(){
		 JSONObject json = null;
		try {
		      json = doRequest(METHOD_GET_BLOCK_COUNT);
	            if(!isError(json)){
	                return json.getLong("result");
	            }else{
	                logger.error(json.toString());
	                return 0;
	            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	public  JSONObject getblockchaininfo() {
		try {
			return  doRequest("getblockchaininfo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Deprecated
	public  JSONObject getInfo() {
		try {
			return  doRequest("getInfo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  JSONObject getnetworkinfo() {
		try {
			return  doRequest("getnetworkinfo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  JSONObject getwalletinfo() {
		try {
			return  doRequest("getwalletinfo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  BigDecimal getbalance() {
		try {
			 JSONObject json = doRequest(METHOD_GET_BALANCE);
		        if(isError(json)){
		            logger.error("获取BTC余额:"+json.get("error"));
		            return new BigDecimal(0);
		        }
			return NumberUtils.roundBigDecimal(new BigDecimal(json.getDouble(RESULT)), 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    public boolean vailedAddress(String address) {
        JSONObject json  = doRequest("validateaddress",address);
        if(isError(json)){
            logger.error("BTC验证地址失败:"+json.get("error"));
            return false;
        }else{
            return json.getJSONObject(RESULT).getBoolean("isvalid");
        }
    }
	public  List<Address> listreceivedbyaddress(long l, boolean flag) {
		try {
			 JSONObject json  = doRequest("listreceivedbyaddress",l,flag);
		        if(isError(json)){
		            logger.error("BTC验证地址失败:"+json.get("error"));
		            return null;
		        }else{
		        	return JSONArray.parseArray(json.getString("result"), Address.class);
		        }
		        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  List<Account> listreceivedbyaccount(long l, boolean flag) {
		try {
			 JSONObject json  = doRequest("listreceivedbyaccount",l,flag);
		        if(isError(json)){
		            logger.error("BTC验证地址失败:"+json.get("error"));
		            return null;
		        }else{
		        	return JSONArray.parseArray(json.getString("result"), Account.class);
		        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 建议使用离线签名
	*@param pwd
	*@param address
	*@param amt
	*@return
	 */
	@Deprecated
	public synchronized  String sendtoaddress(String pwd,String address,double amt) {
		try {
//			if(vailedAddress(address)){
				boolean flag=walletpassphrase(pwd, 10);
				if(flag){
					JSONObject json = doRequest(METHOD_SEND_TO_ADDRESS,address,amt);
					if(isError(json)){
						logger.error("BTC 转帐给{"+address+"} value:{"+amt+"}  失败 ："+json.get("error"));
						return "";
					}else{
						logger.error("BTC 转币给{"+address+"} value:{"+amt+"} 成功");
						return json.getString(RESULT);
					}
				}else{
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public  List<Transactions> getRecord(String account,int pageLimit,int pageIndex){
		try {
			
			 JSONObject json = doRequest(METHOD_LIST_TRANSACTIONS,account,pageLimit,pageIndex);
	        if(isError(json)){
	        	logger.error("获取BTC地址失败:"+json.get("error"));
	            return null;
	        }
	        return JSONArray.parseArray(json.getString("result"), Transactions.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：根据txid获取交易信息
	 * 编写人：auth
	 * 编写时间：2017年8月18日
	 * @param txid
	 * @return
	 * Transaction
	 */
	@Deprecated
	public  Transactions getTransaction(String txid){
		try {
			 JSONObject json = doRequest(METHOD_GET_TRANSACTION, txid);
			   if(isError(json)) {
		           logger.error("处理BTC tx出错");
		           return null;
		       }
			   
			return JSON.parseObject(json.getString("result"), Transactions.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取确认次数
	 * 编写人：auth
	 * 编写时间：2017年8月18日
	 * @param txid
	 * @return
	 * long
	 */
	public  Long getConfirmations(String txid){
		try {
			JSONObject json = doRequest(METHOD_GET_TRANSACTION, txid);
			   if(isError(json)) {
		           logger.error("处理BTC tx出错");
		           return null;
		       }
			   
			return json.getJSONObject(RESULT).getLong("confirmations");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public  boolean walletlock(){
		try {
			JSONObject obj=doRequest(METHOD_WALLET_LOCK);
			if(isError(obj)){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public  boolean walletpassphrase(String pwd,long times){
		try {
			JSONObject obj=doRequest(METHOD_WALLET_PASSPHRASE, pwd,times);
			if(isError(obj)){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
