package com.coin.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coin.bit.pojo.Address;
import com.coin.bit.pojo.Transaction;
import com.coin.utils.HttpUtil;
import com.coin.utils.NumberUtils;
import com.google.common.collect.Lists;

public class UsdtService  {
	private Logger logger = Logger.getLogger(getClass());
	public static final int usdtPropertyid = 31;
	private final static String RESULT = "result";
//	omni_funded_send 方法，a向b转usdt    首先a必须有btc ，手续费先从a扣除，a中btc归零，不够的话在从feeaddress 中扣除不够的手续费。如果a够的话，a中btc也清零，多余的归到b中
	private final static String METHOD_SEND_TO_ADDRESS_FUNDED = "omni_funded_send";
	private final static String METHOD_SEND_TO_ADDRESS = "omni_send";
	private final static String METHOD_GET_TRANSACTION = "omni_gettransaction";
	private final static String METHOD_GET_BLOCK_COUNT = "getblockcount";
	private final static String METHOD_NEW_ADDRESS = "getnewaddress";
	private final static String METHOD_GET_BALANCE = "omni_getbalance";
	private final static String METHOD_WALLETPASSPHRASE = "walletpassphrase";
	private final static String METHOD_GET_LISTBLOCKTRANSACTIONS = "omni_listblocktransactions";
	private final static String DUMP_PRIVATE = "dumpprivkey";
	private static final String mainAddress = "";//手续费地址
    private final static String METHOD_GET_LISTUNSPENT = "listunspent";
    //正式网络usdt=31，测试网络可以用2
    private static final int propertyid = 31;
	private String url;
	private String user;
	private String password;
	 /**
     * usdt 离线签名
     *
     * @param privateKey：私钥
     * @param toAddress：接收地址
     * @param amount:转账金额
     * @return
     */
    public String rawSignAndSend(String fromAddress, String toAddress, String changeAddress, Long amount) throws Exception {
    	List<UTXO> utxos = new ArrayList<UTXO>();
    	List<UTXO> utxoss = new ArrayList<UTXO>();
    	if(mainAddress.equals(fromAddress)) {
    		utxos = this.getUnspents(fromAddress);
    		
    	}else {
    		utxos = this.getUnspents(fromAddress);
    		utxoss = this.getUnspents(toAddress);
    		for(int i=0;i<utxoss.size();i++) {
    			utxos.add(utxoss.get(i));
    		}
    	}
    	System.out.println(utxos);
    	// 获取手续费
    	Long fee = this.getOmniFee(utxos);
        //判断是主链试试测试链
//    	NetworkParameters networkParameters = isMainNet ? MainNetParams.get() : TestNet3Params.get();
    	NetworkParameters networkParameters =  MainNetParams.get();
    	//NetworkParameters networkParameters = TestNet3Params.get();
        org.bitcoinj.core.Transaction tran = new org.bitcoinj.core.Transaction(networkParameters);
         if(utxos==null||utxos.size()==0){
             throw new Exception("utxo为空");
         }
        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
        Long miniBtc = 546L;
        tran.addOutput(Coin.valueOf(miniBtc), org.bitcoinj.core.Address.fromString(networkParameters, toAddress));

        //构建usdt的输出脚本 注意这里的金额是要乘10的8次方
        String usdtHex = "6a146f6d6e69" + String.format("%016x", propertyid) + String.format("%016x", amount);
        tran.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));

        Long changeAmount = 0L;
        Long utxoAmount = 0L;
        List<UTXO> needUtxo = new ArrayList<>();
        //过滤掉多的uxto
        for (UTXO utxo : utxos) {
            if (utxoAmount > (fee + miniBtc)) {
                break;
            } else {
                needUtxo.add(utxo);
                utxoAmount += utxo.getValue().value;
            }
        }
        changeAmount = utxoAmount - (fee + miniBtc);
        //余额判断
        if (changeAmount < 0) {
            throw new Exception("utxo余额不足");
        }
        if (changeAmount > 0) {
            tran.addOutput(Coin.valueOf(changeAmount), org.bitcoinj.core.Address.fromString(networkParameters, changeAddress));
        }

        //先添加未签名的输入，也就是utxo
        for (UTXO utxo : needUtxo) {
            tran.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript()).setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
        }
        

        //下面就是签名
        for (int i = 0; i < needUtxo.size(); i++) {
        	//这里获取地址
        	String addr = needUtxo.get(i).getAddress();
        	String privateKeys = this.getPrivateAddress(addr);
        	
            ECKey ecKey = DumpedPrivateKey.fromBase58(networkParameters, privateKeys).getKey();
            TransactionInput transactionInput = tran.getInput(i);
            Script scriptPubKey = ScriptBuilder.createOutputScript(org.bitcoinj.core.Address.fromString(networkParameters, addr));
            Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, org.bitcoinj.core.Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
            TransactionSignature txSig = new TransactionSignature(ecSig, org.bitcoinj.core.Transaction.SigHash.ALL, false);
            transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
        }

        //这是签名之后的原始交易，直接去广播就行了
        String signedHex = Hex.toHexString(tran.bitcoinSerialize());
        logger.info("签名之后的原始交易:{}"+signedHex);
        //这是交易的hash
        String txHash = Hex.toHexString(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(tran.bitcoinSerialize()))));
        logger.info("fee:"+fee+",utxoAmount:"+utxoAmount+",changeAmount:"+changeAmount+",txHash:"+txHash);
        
        JSONObject json = doRequest("sendrawtransaction", signedHex);
		if (isError(json)) {
			logger.error("发送交易失败");
			return null;
		} else {
			String result = json.getString("result");
			logger.info("发送成功 hash:"+ result);
			return result;
		}
       
    }
    /**
     * 获取矿工费用
     * @param utxos
     * @return
     */
    public Long getOmniFee(List<UTXO> utxos) {
        Long miniBtc = 546L;
        Long feeRate = getFeeRate();
        Long utxoAmount = 0L;
        Long fee = 0L;
        Long utxoSize = 0L;
        for (UTXO output : utxos) {
            utxoSize++;
            if (utxoAmount > (fee + miniBtc)) {
                break;
            } else {
                utxoAmount += output.getValue().value;
                fee = (utxoSize * 148 + 34 * 2 + 10) * feeRate;
            }
        }
        return fee;
    }
    /**
     * 获取btc费率
     *
     * @return
     */
    public Long getFeeRate() {
        try {
            String httpGet1 = HttpUtil.get("https://bitcoinfees.earn.com/api/v1/fees/recommended");
            Map map = JSON.parseObject(httpGet1, Map.class);
            Long fastestFee = Long.valueOf(map.get("fastestFee").toString());
            return fastestFee;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
    /**
     * 广播交易
     * @param args
     */
    public String publishTx(String method,Object... params) {
    	   JSONObject param = new JSONObject();
           param.put("id",System.currentTimeMillis()+"");
           param.put("jsonrpc","2.0");
           param.put("method",method);
           if(params != null){
               param.put("params",params);
           }
           String creb = Base64.encodeBase64String((user+":"+password).getBytes());
           Map<String,String> headers = new HashMap<>(2);
           headers.put("Authorization","Basic "+creb);
           String resp = "";
           if (METHOD_GET_TRANSACTION.equals(method)){
               try{
                   resp = HttpUtil.jsonPost(url,headers,param.toJSONString());
               }catch (Exception e){
                   if (e instanceof IOException){
                       resp = "{}";
                   }
               }
           }else{
               resp = HttpUtil.jsonPost(url,headers,param.toJSONString());
           }
           return resp;
    }
    public List<UTXO> getUnspents(String... address) {
        List<UTXO> utxos = Lists.newArrayList();
     
        try {
       	   JSONObject jsonObject = doRequest(METHOD_GET_LISTUNSPENT, 0,99999999,address);
           JSONArray outputs = jsonObject.getJSONArray("result");
           if (outputs == null || outputs.size() == 0) {
               System.out.println("交易异常，余额不足");
           }
           for (int i = 0; i < outputs.size(); i++) {
           	JSONObject outputsMap = outputs.getJSONObject(i);
           	 String txid = outputsMap.get("txid").toString();
                String vout = outputsMap.get("vout").toString();
                String addr = outputsMap.get("address").toString();
                String script = outputsMap.get("scriptPubKey").toString();
                String amount = outputsMap.get("amount").toString();
                BigDecimal bigDecimal = new BigDecimal(amount);
                bigDecimal = bigDecimal.multiply(new BigDecimal(100000000));
               // String confirmations = outputsMap.get("confirmations").toString();
                UTXO utxo = new UTXO(Sha256Hash.wrap(txid), Long.valueOf(vout), Coin.valueOf(bigDecimal.longValue()),
                        0, false, new Script(Hex.decode(script)),addr);
                System.out.println(utxo.getAddress());
                utxos.add(utxo);
           }
           return utxos;
           } catch (Exception e) {
               logger.error("【BTC获取未消费列表】失败，", e);
               return null;
           }

    }
    /**
     *    
     * BTC获取私钥
     */
    public String getPrivateAddress(String address) {
    	 JSONObject json = doRequest(DUMP_PRIVATE,address);
         if(isError(json)){
        	 logger.error("获取USDT地址失败:{}"+json.get("error"));
             return "";
         }
         return json.getString(RESULT);
    	
    }
	public UsdtService(String url, String user, String password) {
		try {
			this.url = url;
			this.user = user;
			this.password = password;
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT链接获取失败！");
			e.printStackTrace();
		}
	}

	public JSONObject doRequest(String method, Object... params) {
		JSONObject param = new JSONObject();
		param.put("id", System.currentTimeMillis() + "");
		param.put("jsonrpc", "2.0");
		param.put("method", method);
		if (params != null) {
			param.put("params", params);
		}
		String creb = Base64.encodeBase64String((user + ":" + password).getBytes());
		Map<String, String> headers = new HashMap<>(2);
		headers.put("Authorization", "Basic " + creb);
		String resp = "";
		if (METHOD_GET_TRANSACTION.equals(method)) {
			try {
				resp = HttpUtil.jsonPost(url, headers, param.toJSONString());
			} catch (Exception e) {
				if (e instanceof IOException) {
					resp = "{}";
				}
			}
		} else {
			resp = HttpUtil.jsonPost(url, headers, param.toJSONString());
		}
		return JSON.parseObject(resp);
	}

	private boolean isError(JSONObject json) {
		if (json == null || (StringUtils.isNotEmpty(json.getString("error")) && json.get("error") != "null")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 方法描述：获取钱包地址 
	 * 
	 * @param label
	 * @return String
	 */
	public String getAddress(String label) {
		try {
			JSONObject json = doRequest(METHOD_NEW_ADDRESS);
			// if(isError(json)){
			// log.error("获取USDT地址失败:{}",json.get("error"));
			// return "";
			// }
			return json.getString(RESULT);
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT getAddress失败！");
			e.printStackTrace();
		}
		return null;
	}

	public long getblockcount() {
		JSONObject json = null;
		try {
			json = doRequest(METHOD_GET_BLOCK_COUNT);
			if (!isError(json)) {
				return json.getLong("result");
			} else {
				logger.error(json.toString());
				return 0;
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT getblockcount失败！");
			e.printStackTrace();
		}
		return 0;
	}

	public JSONObject getinfo() {
		try {
			return doRequest("omni_getinfo");
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT getinfo失败！");
			e.printStackTrace();
		}
		return null;
	}

	public BigDecimal getbalance(String address, int propertyid) {
		try {
			JSONObject json = doRequest(METHOD_GET_BALANCE, address, propertyid);
			if (isError(json)) {
				logger.error("获取USDT余额:{" + json.get("error") + "}");
				return new BigDecimal(0);
			}
			return NumberUtils.subNumber(json.getJSONObject(RESULT).getDouble("balance"), 8);
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT getbalance失败！");
			e.printStackTrace();
		}
		return null;
		
	}

	public BigDecimal getbalance(String address) {
		return getbalance(address, usdtPropertyid);
	}

	/**
	 * 
	 * 方法描述：提币 
	 * 
	 * @param fromAddress
	 * @param amount
	 * @return txid String
	 */
	public synchronized String send(String fromAddress, String toAddress, int propertyid, double amt) {
		try {
			if (vailedAddress(toAddress)) {
				JSONObject json = doRequest(METHOD_SEND_TO_ADDRESS, fromAddress, toAddress, propertyid, String.valueOf(amt));
				logger.error("==============USDT转账返回："+json);
				if (isError(json)) {
					logger.error("USDT 转帐给{" + toAddress + "} value:{" + amt + "}  失败 ：" + json.get("error"));
					return "";
				} else {
					logger.error("USDT 转币给{" + toAddress + "} value:{" + amt + "} 成功");
					return json.getString(RESULT);
				}
			} else {
				logger.error("USDT接受地址不正确");
				return "";
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-USDT getbalance失败！");
			e.printStackTrace();
		}
		return "";
	}
	public synchronized String sendFunded(String fromAddress, String toAddress, int propertyid, double amt,String feeaddress ) {
		try {
			if (vailedAddress(toAddress)) {
				JSONObject json = doRequest(METHOD_SEND_TO_ADDRESS_FUNDED, fromAddress, toAddress, propertyid, String.valueOf(amt),feeaddress );
				logger.error("==============USDT转账返回："+json);
				if (isError(json)) {
					logger.error("USDT 转帐给{" + toAddress + "} value:{" + amt + "}  失败 ：" + json.get("error"));
					return "";
				} else {
					logger.error("USDT 转币给{" + toAddress + "} value:{" + amt + "} 成功");
					return json.getString(RESULT);
				}
			} else {
				logger.error("USDT接受地址不正确");
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==============虚拟币-USDT sendFunded失败！");
			e.printStackTrace();
		}
		return "";
	}

	public String sendAndUnLock(String fromAddress, String walletPwd, String toAddress, int propertyid, double value) {
		walletpassphrase(walletPwd, 120);
		return send(fromAddress, toAddress, propertyid, value);
	}
	public String sendAndUnLock(String fromAddress, String walletPwd, String toAddress, double value) {
		walletpassphrase(walletPwd, 120);
		return send(fromAddress, toAddress, usdtPropertyid, value);
	}

	public String send(String fromAddress, String toAddress, double value) {
		return send(fromAddress, toAddress, usdtPropertyid, value);
	}
	public String sendFunded(String fromAddress, String toAddress, double value,String feeaddress) {
		return sendFunded(fromAddress, toAddress, usdtPropertyid, value,feeaddress);
	}
	public String sendFundedAndUnLock(String fromAddress, String walletPwd, String toAddress, double value,String feeaddress) {
		walletpassphrase(walletPwd, 120);
		return sendFunded(fromAddress, toAddress, usdtPropertyid, value,feeaddress);
	}
	public String sendFundedAndUnLock(String fromAddress, String walletPwd,int propertyid, String toAddress, double value,String feeaddress) {
		walletpassphrase(walletPwd, 120);
		return sendFunded(fromAddress, toAddress, propertyid, value,feeaddress);
	}
	/**
	 * usdt 离线签名
	 * @param privateKey
	 * @param changeAddress
	 * @param changeAmount
	 * @param toAddress
	 * @param outputs
	 * @param amount
	 * @return
	 */
//	public String sign(String privateKey, String changeAddress,Long changeAmount, String toAddress, List<Utxo> outputs,Long amount) {
//	        MainNetParams network = MainNetParams.get();
//	        Transaction tran = new Transaction(MainNetParams.get());
//	 
//	        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
//	        tran.addOutput(Coin.valueOf(546L), Address.fromBase58(network, toAddress));
//	 
//	        //构建usdt的输出脚本 注意这里的金额是要乘10的8次方
//	        String usdtHex = "6a146f6d6e69" + String.format("%016x", 31) + String.format("%016x", amount);
//	        tran.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));
//	 
//	        //如果有找零就添加找零
//	        if (changeAmount.compareTo(0L) > 0) {
//	            tran.addOutput(Coin.valueOf(changeAmount), Address.fromBase58(network, changeAddress));
//	        }
//	 
//	        //先添加未签名的输入，也就是utxo
//	        for (Utxo output : outputs) {
//	            tran.addInput(Sha256Hash.wrap(output.getTxHash()), output.getVout(), new Script(HexUtil.decodeHex(output.getScriptPubKey()))).setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
//	        }
//	 
//	        //下面就是签名
//	        for (int i = 0; i < outputs.size(); i++) {
//	            Utxo output = outputs.get(i);
//	            ECKey ecKey = DumpedPrivateKey.fromBase58(network, privateKey).getKey();
//	            TransactionInput transactionInput = tran.getInput(i);
//	            Script scriptPubKey = ScriptBuilder.createOutputScript(Address.fromBase58(network, output.getAddress()));
//	            Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
//	            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
//	            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
//	            transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
//	        }
//	        //这是签名之后的原始交易，直接去广播就行了
//	        String signedHex = HexUtil.encodeHexStr(tran.bitcoinSerialize());
//	        //这是交易的hash
//	        String txHash = HexUtil.encodeHexStr(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(tran.bitcoinSerialize()))));
//	        return signedHex;
//	}
	/**
	 * 验证地址的有效性
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public boolean vailedAddress(String address) {
		JSONObject json = doRequest("validateaddress", address);
		if (isError(json)) {
			logger.error("USDT验证地址失败:" + json.get("error"));
			return false;
		} else {
			return json.getJSONObject(RESULT).getBoolean("isvalid");
		}
	}

	public boolean parseBlock(int index) {
		// doRequest("omni_listblocktransactions",279007);
		// {"result":["63d7e22de0cf4c0b7fd60b4b2c9f4b4b781f7fdb8be4bcaed870a8b407b90cf1","6fb25ab84189d136b95d7f733b0659fa5fbd63f476fb1bca340fb4f93de6c912","d54213046d8be80c44258230dd3689da11fdcda5b167f7d10c4f169bd23d1c01"],"id":"1521454868826"}
		JSONObject jsonBlock = doRequest(METHOD_GET_LISTBLOCKTRANSACTIONS, index);
		if (isError(jsonBlock)) {
			logger.error("访问USDT出错");
			return false;
		}
		JSONArray jsonArrayTx = jsonBlock.getJSONArray(RESULT);
		if (jsonArrayTx == null || jsonArrayTx.size() == 0) {
			// 没有交易
			return true;
		}
		Iterator<Object> iteratorTxs = jsonArrayTx.iterator();
		while (iteratorTxs.hasNext()) {
			String txid = (String) iteratorTxs.next();
			parseTx(txid, null);
		}
		return true;
	}

	public void parseTx(String txid, List<?> userList) {
		/**
		 * {"result":{"amount":"50.00000000","divisible":true,"fee":"0.00000257",
		 * "txid":"f76d51044f156e6ed84c11e6531db1d6d70799196522c07bd2a8870a21f90220","ismine":true,
		 * "type":"Simple
		 * Send","confirmations":565,"version":0,"sendingaddress":"mh8tV2mfDa6yHK76t68N3paoGdSmangJDi",
		 * "valid":true,"blockhash":"000000000000014cdef6ee8a095b58755efebf913b1ab13bb23adaa33b6f7b05",
		 * "blocktime":1523528971,"positioninblock":189,"referenceaddress":"mg5yVUSwGNEJNhYKfyETV2udWok6Q4pgLx",
		 * "block":1292526,"propertyid":2,"type_int":0},"id":"1523860978684"}
		 */
		JSONObject jsonTransaction = doRequest(METHOD_GET_TRANSACTION, txid);
		if (isError(jsonTransaction)) {
			logger.error("处理USDT tx出错");
			return;
		}
		JSONObject jsonTResult = jsonTransaction.getJSONObject(RESULT);
		if (!jsonTResult.getBoolean("valid")) {
			logger.info("不是有效数据");
			return;
		}
		int propertyidResult = jsonTResult.getIntValue("propertyid");
		if (propertyidResult != usdtPropertyid) {
			logger.info("非USDT数据");
			return;
		}
		double value = jsonTResult.getDouble("amount");
		if (value > 0) {
			String address = jsonTResult.getString("referenceaddress");
		}
	}

	public int getConfirmations(String txid) {
		JSONObject jsonTransaction = doRequest(METHOD_GET_TRANSACTION, txid);
		if (isError(jsonTransaction)) {
			return 0;
		}
		JSONObject jsonTResult = jsonTransaction.getJSONObject(RESULT);
		if (!jsonTResult.getBoolean("valid")) {
			return 0;
		}
		int propertyidResult = jsonTResult.getIntValue("propertyid");
		if (propertyidResult != usdtPropertyid) {
			return 0;
		}
		return jsonTResult.getIntValue("confirmations");
	}



	public boolean walletpassphrase(String walletPwd, long times) {
		try {
			 doRequest(METHOD_WALLETPASSPHRASE, walletPwd,times);
			 return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==============虚拟币-USDT walletpassphrase失败！");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * usdt 离线签名，目前暂不可用
	 * @param privateKey
	 * @param changeAddress
	 * @param changeAmount
	 * @param toAddress
	 * @param outputs
	 * @param amount
	 * @return
	 */
	/**
	@Deprecated
	public String sign(String privateKey, String changeAddress,Long changeAmount, String toAddress, List<UTXO> outputs,Long amount) {
	        MainNetParams network = MainNetParams.get();
	        org.bitcoinj.core.Transaction tran = new org.bitcoinj.core.Transaction(MainNetParams.get());

	        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
	        tran.addOutput(Coin.valueOf(546L), org.bitcoinj.core.Address.fromBase58(network, toAddress));

	        //构建usdt的输出脚本 注意这里的金额是要乘10的8次方
	        String usdtHex = "6a146f6d6e69" + String.format("%016x", 31) + String.format("%016x", amount);
	        tran.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));

	        //如果有找零就添加找零
	        if (changeAmount.compareTo(0L) > 0) {
	            tran.addOutput(Coin.valueOf(changeAmount), org.bitcoinj.core.Address.fromBase58(network, changeAddress));
	        }

	        //先添加未签名的输入，也就是utxo
	        for (UTXO output : outputs) {
	            tran.addInput(Sha256Hash.wrap(output.getTxHash()), output.getVout(), new Script(HexUtils.decodeHex(output.getScriptPubKey()))).setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
	        }

	        //下面就是签名
	        for (int i = 0; i < outputs.size(); i++) {
	            UTXO output = outputs.get(i);
	            ECKey ecKey = DumpedPrivateKey.fromBase58(network, privateKey).getKey();
	            TransactionInput transactionInput = tran.getInput(i);
	            Script scriptPubKey = ScriptBuilder.createOutputScript(org.bitcoinj.core.Address.fromBase58(network, output.getAddress()));
	            Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
	            ECKey.ECDSASignature ecSig = ecKey.sign(hash);
	            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
	            transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
	        }

	        //这是签名之后的原始交易，直接去广播就行了
	        String signedHex = HexUtils.encodeHexStr(tran.bitcoinSerialize());
	        //这是交易的hash
	        String txHash = HexUtils.encodeHexStr(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(tran.bitcoinSerialize()))));
	        return signedHex;
	}*/
}
