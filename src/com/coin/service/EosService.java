package com.coin.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.eblock.eos4j.Ecc;
import io.eblock.eos4j.EosRpcService;
import io.eblock.eos4j.api.vo.ChainInfo;
import io.eblock.eos4j.api.vo.TableRows;
import io.eblock.eos4j.api.vo.TableRowsReq;
import io.eblock.eos4j.api.vo.account.Account;
import io.eblock.eos4j.api.vo.action.Action;
import io.eblock.eos4j.api.vo.action.ActionTrace;
import io.eblock.eos4j.api.vo.action.Actions;
import io.eblock.eos4j.api.vo.transaction.Transaction;
import com.coin.novacrypto.bip39.MnemonicGenerator;
import com.coin.novacrypto.bip39.Words;
import com.coin.novacrypto.bip39.wordlists.English;
import com.coin.utils.JsonUtil;
import com.coin.utils.StringUtils;


public class EosService {
	private Logger logger = Logger.getLogger(getClass());
//  正式网络 https://api-v2.eosasia.one     https://proxy.eosnode.tools
	//测试网络 https://api-kylin.eosasia.one
//	"eosqxyx11111", "5JJB2oiCXwASK9fumjyTgbtHcwDVedVRaZda1kBhFuQcmjnrDWB"
//private String account = "eosqxyx22222";
//private String account;
private final static String EOS_NAME = "EOS";
public static final String K_mnemonic = "mnemonic";
public static final String K_privateKey = "privateKey";
public static final String K_publicKey = "publicKey";
private static final String EOS_TOKEN = "eosio.token";
private static final String ACTION_TRANSFER = "transfer";

private EosRpcService eosRpcService = null;

public EosService(){
	String chainUrl="https://proxy.eosnode.tools";
	eosRpcService = new EosRpcService(chainUrl);
}
public EosService(String chainUrl){
	eosRpcService = new EosRpcService(chainUrl);
}
	    
public static void main(String[] args) {
	EosService eos = new EosService(null);
	System.out.println(eos.getBalance("bitcashlif12"));
}

/**
 * 查询余额
 *
 * @param accountName:账户名
 * @return
 */
public double getBalance(String accountName) {
	try {
		List<String> list = eosRpcService.getCurrencyBalance(EOS_TOKEN, accountName, EOS_NAME);
		if (list != null && list.size() > 0) {
			return Double.parseDouble(list.get(0).replaceAll(" EOS", ""));
		}
	} catch (Exception e) {
		logger.error("EOS获取余额:getBalance失败");
		e.printStackTrace();
	}
	return 0.00;
}
/**
 * 获取用户交易记录
 * @param index 获取到第几条记录
 * @return
 */
@Deprecated
private boolean getActions(String account,Long index){
	try {
		Actions actions = eosRpcService.getActions(account, index,1);
		if (actions != null) {
			List<Action> list = actions.getActions();
			if (list==null || list.size() == 0) {
				return false;
			}
			for (Action action : list) {
				ActionTrace actionTrace = action.getActionTrace();
				String acc = actionTrace.getAct().getAccount();
				if (!EOS_TOKEN.equals(acc)) {
					logger.info("非EOS交易记录：{"+acc+"}");
                	return true;
				}
				String name = actionTrace.getAct().getName();
				if (!ACTION_TRANSFER.equals(name)) {
					logger.info("非EOS转账交易记录：{"+acc+"}");
					return true;
				}
				//{from=eosqxyx11111, to=eosqxyx22222, quantity=10.0000 EOS, memo=test}
				logger.info("交易详情：{"+actionTrace.getAct().getData().toString()+"}");
				JSONObject json = JSONObject.parseObject(JsonUtil.getSting(actionTrace.getAct().getData()));
				if (!acc.equals(json.getString("to"))) {
					logger.info("非充值记录：{"+actionTrace.getTrxId()+"}");
					return true;
				}
				String[] quantity = json.getString("quantity").split(" ");
				if (!EOS_NAME.equals(quantity[1])) {
					logger.info("非EOS充值记录：{"+json.getString("quantity")+"}");
					return true;
				}
				String memo = json.getString("memo");
                if (StringUtils.isEmpty(memo)) {
					logger.info("记录TrxId：{"+actionTrace.getTrxId()+"}为空");
					return true;
				}
                //判断用户是否存在
                /*UserEntity user = userService.getUserById(Integer.parseInt(memo));
                if (user == null) {
					logger.info("用户信息不存在：memo：{}",memo);
					continue;
				}*/
                //添加充值记录
        		return true;
			}
		}
	} catch (Exception e) {
		logger.error("获取用户交易记录失败:{"+e.getMessage()+"}");
		e.printStackTrace();
	}
	return false;
}
/**
 * 3.充值
 *
 * @param account：账户名
 * @param index：记录索引
 * @param pageSize    ：记录条数
 * @return
 */
@Deprecated
public boolean getActions(String account, Long index, Integer pageSize) {
    //todo:修改
    try {
        Actions actions = eosRpcService.getActions(account, index, pageSize);
        if (actions != null) {
            List<Action> list = actions.getActions();
            if (list == null || list.size() == 0) {
                return false;
            }
            System.out.println("list.size()="+list.size());
            //不可变更区块高度
            int lastIrreversibleBlock = actions.getLastIrreversibleBlock();
            //每次处理一条数据,不需要去重
            list = this.removeDuplicate(list);
            for (Action action : list) {
            	  System.out.println("=========================action="+action);
                ActionTrace actionTrace = action.getActionTrace();
                String eos_token = actionTrace.getAct().getAccount();
                if (!EOS_TOKEN.equals(eos_token)) {
                    //log.info("非EOS交易记录：{}", account);
                    continue;
                }
                if (action.getBlockNum() > lastIrreversibleBlock) {
                    //log.info("未确认交易：{}", account);
                    continue;
                }
                String name = actionTrace.getAct().getName();
                if (!ACTION_TRANSFER.equals(name)) {
                    //log.info("非EOS转账交易记录：{}", account);
                    continue;
                }
                //{from=eosqxyx11111, to=eosqxyx22222, quantity=10.0000 EOS, memo=test}
                JSONObject json = JSONObject.parseObject(JSON.toJSONString(actionTrace.getAct().getData()));
                System.out.println("============json="+json);
                if (!account.equals(json.get("to").toString())) {
                    //log.info("非充值记录：{}", actionTrace.getTrxId());
                    continue;
                }
                String[] quantity = json.get("quantity").toString().split(" ");
                if (!EOS_NAME.equals(quantity[1])) {
                    //log.info("非EOS充值记录：{}", json.get("quantity"));
                    continue;
                }
                String memo = json.get("memo").toString();
                /*if (StringUtils.isEmpty(memo)) {
                    log.info("记录memo为空");
                    continue;
                }*/
                logger.info("充值{"+(index + 1)+"}-【{"+actionTrace.getTrxId()+"}】--【{"+json+"}】");
                //判断是否存在用户,并添加充值记录
                /*UserEntity user = userService.getUserById(Integer.parseInt(memo));
                if (user == null) {
					log.info("用户信息不存在：memo：{}",memo);
					return true;
				}
                record.setUserId(user.getUserId());
                rechargeParse(record);*/

                return true;
            }
        }
    } catch (Exception e1) {
        e1.printStackTrace();
        logger.error("获取用户交易记录失败:{"+e1.getMessage()+"}");
    }
    return true;
}
public Actions getBlockList(String account, Long index, Integer pageSize) {
	try {
		return eosRpcService.getActions(account, index, pageSize);
	} catch (Exception e1) {
		e1.printStackTrace();
		logger.error("获取用户交易记录失败:{"+e1.getMessage()+"}");
	}
	return null;
}
/**
 * 去重
 * @param list
 * @return
 */
public List<Action> removeDuplicate(List<Action> list) {
	for (int i = 0; i < list.size() - 1; i++) {
		for (int j = list.size() - 1; j > i; j--) {
			if (list.get(j).getActionTrace().getTrxId()
					.equals(list.get(i).getActionTrace().getTrxId())) {
				list.remove(j);
			}
		}
	}
	return list;
}

/**
 * 发送交易
 * @param toAccount 收款方
 * @param amount 转账金额，保留4位小数点
 * @param memo  备注
 * @return
 */
public synchronized String send(String privateKey,String fromAccount,String toAccount,double amount,String memo){
	try {
//		String amt = BigDecimalUtil.getFourString(amount)+" EOS";
		String amt = toEosUtit(amount);
//		Transaction t1 = rpc.transfer("5JJB2oiCXwASK9fumjyTgbtHcwDVedVRaZda1kBhFuQcmjnrDWB","eosio.token", "eosqxyx11111","eosqxyx22222", "10.0000 EOS", "te1");
		Transaction t1 = eosRpcService.transfer(privateKey,EOS_TOKEN, fromAccount,toAccount, amt, memo);
		logger.error("==============EOS转账返回："+t1.toString());
        if (t1 != null) {
        	logger.error("EOS转账成功：transactionId:{"+t1.getTransactionId()+"}");
    		return t1.getTransactionId();
		}else {
			logger.error("EOS转账失败");
		}
	} catch (Exception e) {
		logger.error("EOS转账失败:{"+e.getMessage()+"}");
		e.printStackTrace();
	}
    return null;
}
/**
 * 转账
 *
 * @param pk                  ：秘钥
 * @param from                ：付款方
 * @param to：收款方
 * @param amount：金额（小数点后4位）
 * @param memo：备注
 * @return
 * @throws Exception
 */
@Deprecated
public Transaction transfer(String privateKey, String from, String to, double amount, String memo) {
	try {
		  String quantity = toEosUtit(amount);
		  return eosRpcService.transfer(privateKey, EOS_TOKEN, from, to, quantity, memo);
	} catch (Exception e) {
		logger.error("EOS转账失败:{"+e.getMessage()+"}");
		e.printStackTrace();
	}
	return null;
  
}

/**
 * 1. 查询链信息
 *
 * @return
 */
public ChainInfo getChainInfo() {
    try {
        return eosRpcService.getChainInfo();
    } catch (Exception e) {
    	logger.error("getAccount exception msg[{"+e.getMessage()+"}]");
    	e.printStackTrace();
    }
return null;
}
public String getBlockNumber() {
	try {
		return getChainInfo().getHeadBlockNum();
	} catch (Exception e) {
		logger.error("getAccount exception msg[{"+e.getMessage()+"}]");
		e.printStackTrace();
	}
	return null;
}
/**
 * 2.创建账户
 *
 * @param pk                          :创建者私钥
 * @param creator：创建者
 * @param newAccount：新账户名（1.必须短于13个字符 2.仅能包含以下字符：.12345abcdefghijklmnopqrstuvwxyz）
 * @param owner：账户所有者
 * @param active：账户所有者
 * @param buyRam                      ：为新账户购买的内存
 * @return
 * @throws Exception
 */
public Transaction createAccount(String pk, String creator, String newAccount, String owner, String active, long buyRam) throws Exception {
    return eosRpcService.createAccount(pk, creator, newAccount, owner, active, buyRam);
}




/**
 * 4.生成密钥对
 *
 * @throws Exception
 */
public Map generateKey() {
    Map<String, String> resultMap = null;
    try {
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE)
                .createMnemonic(entropy, sb::append);
        String mnemonic = sb.toString();
        String privateKey = Ecc.seedPrivate(mnemonic);
        String publicKey = Ecc.privateToPublic(privateKey);
        resultMap = new LinkedHashMap<>();
        resultMap.put(K_mnemonic, mnemonic);
        resultMap.put(K_privateKey, privateKey);
        resultMap.put(K_publicKey, publicKey);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return resultMap;
}




/**
 * 获取账户信息
 *
 * @param accountName:账户名
 * @return null代表账户不存在
 */
public Account getAccount(String accountName) {
    try {
        Account account = eosRpcService.getAccount(accountName);
        return account;
    } catch (Exception e) {
        logger.info("getAccount exception msg[{"+e.getMessage()+"}]" );
        e.printStackTrace();
        return null;
    }
}

public double getRate() {
    double ramPrice = 0;
    try {
        TableRowsReq tableRowsReq = new TableRowsReq();
        tableRowsReq.setJson(true);
        tableRowsReq.setCode("eosio");
        tableRowsReq.setScope("eosio");
        tableRowsReq.setTable("rammarket");
        TableRows tableRows = eosRpcService.getTableRows(tableRowsReq);
        int n = 1;
        Map<String, String> tableRow = tableRows.getRows().get(0);
        Map<String, String> quote = JSON.parseObject(JSON.toJSONString(tableRow.get("quote")), Map.class);
        double quoteBalance = Double.parseDouble(quote.get("balance").split(" ")[0]);
        Map<String, String> base = JSON.parseObject(JSON.toJSONString(tableRow.get("base")), Map.class);
        double baseBalance = Double.parseDouble(base.get("balance").split(" ")[0]);
        ramPrice = (n * quoteBalance) / (n + baseBalance / 1024);
        System.out.println(ramPrice+" EOS/KB");
    }catch (Exception e){
        e.printStackTrace();
    }
    return ramPrice;

}


public static String toEosUtit(double num) {
    String numStr = String.valueOf(num);
    numStr += "0000";
    numStr = numStr.substring(0, numStr.indexOf(".")) + numStr.substring(numStr.indexOf("."), numStr.indexOf(".") + 5);
    numStr += " EOS";
    return numStr;
}

}
