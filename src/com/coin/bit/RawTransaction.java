package com.coin.bit;

import java.util.ArrayList;
import java.util.List;
 
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.omg.CORBA.UNKNOWN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import com.alibaba.fastjson.JSON;
import com.coin.bit.pojo.UnSpentUtxo;

import org.bitcoinj.core.TransactionConfidence;
 
  
/**  
    * @ClassName: RawTransaction  
    * @author DHing  
    *    
*/  
public class RawTransaction {
	
	private static Logger LOG = LoggerFactory.getLogger(RawTransaction.class);
	static NetworkParameters params;
	
	static {
		try {
			params =  MainNetParams.get();
			LOG.info("=== [BTC] bitcoin  client networkID：{} ===", params.getId());
		} catch (Exception e) {
			LOG.info("=== [BTC] com.bscoin.coldwallet.cointype.btc.rawtransaction:{} ===", e.getMessage(), e);
		}
	}
	
	  
	    /**  
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
	public static String signTransaction(String privKey, String recevieAddr, String formAddr,
																		  long amount, long fee, 
																		  List<UnSpentUtxo> unUtxos) {
		if(!unUtxos.isEmpty() && null != unUtxos){
			List<UTXO> utxos = new ArrayList<UTXO>();
			// String to a private key
			DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privKey);
			ECKey key = dumpedPrivateKey.getKey();
			// 接收地址
//			Address receiveAddress = Address.fromBase58(params, recevieAddr);
			Address receiveAddress = Address.fromString(params, recevieAddr);
			// 构建交易
			Transaction tx = new Transaction(params);
			tx.addOutput(Coin.valueOf(amount), receiveAddress); // 转出
			// 如果需要找零 消费列表总金额 - 已经转账的金额 - 手续费
			long value = unUtxos.stream().mapToLong(UnSpentUtxo::getValue).sum();
//			Address toAddress = Address.fromBase58(params, formAddr);
			Address toAddress = Address.fromString(params, formAddr);
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
				tx.addSignedInput(outPoint, utxo.getScript(), key, Transaction.SigHash.ALL, true);
			}
			Context context = new Context(params);
			tx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
			tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
			
			LOG.info("=== [BTC] sign success,hash is :{} ===",tx.getHashAsString());
			return new String(Hex.encodeHex(tx.bitcoinSerialize()));
		}
		return null;
	}
	
	public static void main(String[] args) {
		List<UnSpentUtxo> us = new ArrayList<UnSpentUtxo>();
		UnSpentUtxo u = new UnSpentUtxo();
		u.setAddress("mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo");
		u.setHash("2bc6ac92468c2b4f1fcd2349822dc4663dfc0705b30131087a20ed8d17de8274");
		u.setHeight(	1413239);
		u.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
		u.setTxN(1);
		u.setValue(100000);
		
		UnSpentUtxo u1 = new UnSpentUtxo();
		u1.setAddress("mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ");
		u1.setHash("1893b6ff8ef2bd6f5d652937ffbaed5bb669c5d9ab450066253d6692f2d4d972");
		u1.setHeight(1413334);
		u1.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
		u1.setTxN(1);
		u1.setValue(400000);
		us.add(u);
		us.add(u1);
				
		System.out.println(JSON.toJSONString(us));
		String c = signTransaction("cNRE3D1pbPPvGs9wpZd3X9NuLsuUQPzPa7ktQyF1nhqBabraocU9", "mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo", "mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ", 400000, 10000, us);
		System.out.println(c);
	}
}
