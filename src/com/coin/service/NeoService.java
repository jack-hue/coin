package com.coin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.coin.neo.model.NeoAsset;
import com.coin.utils.HttpUtil;
public class NeoService  {
	private Logger logger = Logger.getLogger(getClass());
	public static final String NEO_ASSET_ID="0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b";
	public static final String NEOGAS_ASSET_ID="0x602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7";
	private static final String GET_ACCOUNT_STATE="getaccountstate";
	private static final String GET_ASSET_STATE="getassetstate";
	private static final String GET_BALANCE="getbalance";
	private static final String GET_BLOCK_COUNT="getblockcount";
	private static final String GET_BLOCK="getblock";
	private static final String RESULT="result";
	private static final String GET_NEW_ADDRESS ="getnewaddress";
	private static final String GET_RAW_TRANSACTION ="getrawtransaction";
	private static final String LIST_ADDRESS ="listaddress";
	private static final String SEND_FROM ="sendfrom";
	private static final String SEND_TO_ADDRESS  ="sendtoaddress";
	private static final String VALIDATE_ADDRESS  ="validateaddress";
	private String url;
	private NeoService() {

	}

	public NeoService(String url) {
		this.url = url;
		
	}
    private JSONObject doRequest(String method,Object... params){
        JSONObject param = new JSONObject();
        param.put("id",System.currentTimeMillis()+"");
        param.put("jsonrpc","2.0");
        param.put("method",method);
        if(params != null){
            param.put("params",params);
        }
        return JSON.parseObject(HttpUtil.jsonPost(url,null,param.toJSONString()));
    }
	private boolean isError(JSONObject json) {
		if (json == null || (!StringUtils.isEmpty(json.getString("error")) && json.get("error") != "null")) {
			return true;
		}
		return false;
	}
	/**
	 * 根据账户地址，查询账户资产信息。
	 * @param addr
	 * @return
	 */
	public JSONObject getAccountState(String addr) {
		try {
			  JSONObject json = doRequest(GET_ACCOUNT_STATE,addr);
		        if(isError(json)){
		        	logger.error("getaccountstate失败:"+json.toString());
		            return null;
		        }
		        return json;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getaccountstate失败！");
			e.printStackTrace();
		}
		return null;
	}
	public String getAccountStateBalances(String addr) {
		try {
			JSONObject json = doRequest(GET_ACCOUNT_STATE,addr);
			if(isError(json)){
				logger.error("getaccountstateBalances失败:"+json.toString());
				return null;
			}
			return json.getJSONObject(RESULT).getString("balances");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getaccountstateBalances失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取给定地址中的账户余额
	 * @param addr
	 * @return
	 */
	public double getAccountStateNEOBalances(String addr) {
		try {
			List<NeoAsset> list=JSON.parseArray(getAccountStateBalances(addr), NeoAsset.class);
			double value=0;
			for (NeoAsset neoAsset : list) {
				if(NEO_ASSET_ID.equals(neoAsset.getAsset())){
					value=neoAsset.getValue();
					break;
				}
			}
			return value;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getaccountstateNEO失败！");
			e.printStackTrace();
		}
		return 0;
	}
	public double getAccountStateNEOGASBalances(String addr) {
		try {
			List<NeoAsset> list=JSON.parseArray(getAccountStateBalances(addr), NeoAsset.class);
			double value=0;
			for (NeoAsset neoAsset : list) {
				if(NEOGAS_ASSET_ID.equals(neoAsset.getAsset())){
					value=neoAsset.getValue();
					break;
				}
			}
			return value;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getaccountstateNEOGAS失败！");
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * 根据指定的资产编号，查询资产信息。
	 * @param addr
	 * @return
	 */
	public JSONObject getAssetState(String assetId) {
		try {
			  JSONObject json = doRequest(GET_ASSET_STATE,assetId);
		        if(isError(json)){
		        	logger.error("getassetstate 失败:"+json.toString());
		            return null;
		        }
		        return json;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getassetstate失败！");
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject getbalanceJson(String assetId) {
		try {
			JSONObject json = doRequest(GET_BALANCE,assetId);
			if(isError(json)){
				logger.error("getbalanceJson 失败:"+json.toString());
				return null;
			}
			return json;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getbalanceJson失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据指定的资产编号，返回钱包中对应资产的余额信息
	 * @param assetId
	 * @return
	 */
	public double getbalance(String assetId) {
		try {
			JSONObject json = getbalanceJson(assetId);
			if(isError(json)){
				logger.error("getbalance 失败:"+json.toString());
				return 0;
			}
			return json.getJSONObject(RESULT).getDoubleValue("balance");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getbalance失败！");
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * 获取主链中区块的数量。
	 * @return
	 */
	public long getblockcount() {
		try {
			JSONObject json = doRequest(GET_BLOCK_COUNT);
			if(isError(json)){
				logger.error("getbalanceJson 失败:"+json.toString());
				return 0;
			}
			return json.getLongValue(RESULT);
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getbalanceJson失败！");
			e.printStackTrace();
		}
		return 0;
	}
	public JSONObject getBlock(long num) {
		try {
			JSONObject json = doRequest(GET_BLOCK,num,1);
			if(isError(json)){
				logger.error("getblock 失败:"+json.toString());
				return null;
			}
			return json.getJSONObject(RESULT);
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getblock失败！");
			e.printStackTrace();
		}
		return null;
	}
	public String getNewAddress() {
		try {
			JSONObject json = doRequest(GET_NEW_ADDRESS);
			if(isError(json)){
				logger.error("getNewAddress 失败:"+json.toString());
				return null;
			}
			return json.getString(RESULT);
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getNewAddress失败！");
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject getRawTransaction(String txid) {
		try {
			JSONObject json = doRequest(GET_RAW_TRANSACTION,txid,1);
			if(isError(json)){
				logger.error("getNewAddress 失败:"+json.toString());
				return null;
			}
//			{"id":"1542167035523","jsonrpc":"2.0","result":{"attributes":[],"blockhash":"0xb2a06177ae1fcbe71c31e9a8f028cba6b9316334e52da6903c34f6eefd52fa45","blocktime":1542164822,"confirmations":53,"net_fee":"0","nonce":1194627964,"scripts":[],"size":10,"sys_fee":"0","txid":"0x55af7cc2d743d4bf078a60db6ba18f00fd4cd0d0d49965ca08aa603c0ecb60cd","type":"MinerTransaction","version":0,"vin":[],"vout":[]}}
			return json;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getNewAddress失败！");
			e.printStackTrace();
		}
		return null;
	}
	public JSONObject listaddress() {
		try {
			JSONObject json = doRequest(LIST_ADDRESS);
			if(isError(json)){
				logger.error("listaddress 失败:"+json.toString());
				return null;
			}
//			{"id":"1542167035523","jsonrpc":"2.0","result":{"attributes":[],"blockhash":"0xb2a06177ae1fcbe71c31e9a8f028cba6b9316334e52da6903c34f6eefd52fa45","blocktime":1542164822,"confirmations":53,"net_fee":"0","nonce":1194627964,"scripts":[],"size":10,"sys_fee":"0","txid":"0x55af7cc2d743d4bf078a60db6ba18f00fd4cd0d0d49965ca08aa603c0ecb60cd","type":"MinerTransaction","version":0,"vin":[],"vout":[]}}
			return json;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getNewAddress失败！");
			e.printStackTrace();
		}
		return null;
	}
	public List<String> listaddressStr() {
		try {
			JSONObject json = listaddress();
			if(isError(json)){
				logger.error("listaddress 失败:"+json.toString());
				return null;
			}
			List<String> list=new ArrayList<String>();
			JSONArray jsonArray=json.getJSONArray(RESULT);
			if(jsonArray!=null&&jsonArray.size()>0)
				for (Object object : jsonArray) {
					JSONObject jObj=(JSONObject)object;
					list.add(jObj.getString("address"));
				}
			return list;
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getNewAddress失败！");
			e.printStackTrace();
		}
		return null;
	}
	public long getConfirmations(String txid) {
		try {
			JSONObject json = getRawTransaction(txid);
			if(isError(json)){
				logger.error("getConfirmations 失败:"+json.toString());
				return 0;
			}
			return json.getLongValue("confirmations");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  getConfirmations失败！");
			e.printStackTrace();
		}
		return 0;
	}
	public synchronized String sendFrom(String assetId,String fromAddr,String toAddr,double value) {
		try {
			if(!validateAddress(toAddr)){
				logger.error("地址错误！");
				throw new RuntimeException("地址错误！");
			}
			JSONObject json = doRequest(SEND_FROM,assetId,fromAddr,toAddr,value);
			logger.error("==============NEO转账返回："+json);
			if(isError(json)){
				logger.error("sendfrom  失败:"+json.toString());
				return null;
			}
			return json.getJSONObject(RESULT).getString("txid");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  sendfrom 失败！");
			e.printStackTrace();
		}
		return null;
	}
	public synchronized String sendtoaddress(String assetId,String toAddr,double value) {
		try {
			if(!validateAddress(toAddr)){
				logger.error("地址错误！");
				throw new RuntimeException();
			}
			JSONObject json = doRequest(SEND_TO_ADDRESS,assetId,toAddr,value);
			logger.error("==============NEO转账返回："+json);
			if(isError(json)){
				logger.error("sendtoaddress 失败:"+json.toString());
				return null;
			}
			return json.getJSONObject(RESULT).getString("txid");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  sendtoaddress失败！");
			e.printStackTrace();
		}
		return null;
	}
	public boolean validateAddress(String addr) {
		try {
			JSONObject json = doRequest(VALIDATE_ADDRESS,addr);
			if(isError(json)){
				logger.error("validateaddress 失败:"+json.toString());
				return false;
			}
			return json.getJSONObject(RESULT).getBooleanValue("isvalid");
		} catch (Exception e) {
			logger.error("==============虚拟币-NEO  validateaddress失败！");
			e.printStackTrace();
		}
		return false;
	}
	public static void main(String[] args) {
	}

}
