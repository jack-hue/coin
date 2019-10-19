package com.coin.eth;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 
 * All rights Reserved, Designed By 郑州好聚点科技有限公司
 * @Description:eth转账相关接口
 * @author: auth    
 * @date:   2018年7月4日 下午2:30:18   
 * @Copyright: 2018 http://www.hjd123.com Inc. All rights reserved.
 */
public class TransactionClient {
//	public static Web3j web3j;
//	public static Admin admin;

//	public static String fromAddress = "0x7b1cc408fcb2de1d510c1bf46a329e9027db4112";
//	public static String toAddress = "0x05f50cd5a97d9b3fec35df3d0c6c8234e6793bdf";
	public static BigDecimal defaultGasPrice = BigDecimal.valueOf(5);

	public static void main(String[] args) {
//		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
//		admin = Admin.build(new HttpService(Environment.RPC_URL));

//		getBalance(fromAddress);
//		sendTransaction();
	}

	/**
	 * 获取余额
	 *
	 * @param address 钱包地址
	 * @return 余额
	 */
	public static BigInteger getBalance(Web3j web3j,String address) {
		BigInteger balance = null;
		try {
			EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
			balance = ethGetBalance.getBalance();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("address " + address + " balance " + balance + "wei");
		return balance;
	}

	/**
	 * 生成一个普通交易对象
	 *
	 * @param fromAddress 放款方
	 * @param toAddress   收款方
	 * @param nonce       交易序号
	 * @param gasPrice    gas 价格
	 * @param gasLimit    gas 数量
	 * @param value       金额
	 * @return 交易对象
	 */
	public static Transaction makeTransaction(String fromAddress, String toAddress,
											   BigInteger nonce, BigInteger gasPrice,
											   BigInteger gasLimit, BigInteger value) {
		Transaction transaction;
		transaction = Transaction.createEtherTransaction(fromAddress, nonce, gasPrice, gasLimit, toAddress, value);
		return transaction;
	}

	/**
	 * 获取普通交易的gas上限
	 *
	 * @param transaction 交易对象
	 * @return gas 上限
	 */
	public static BigInteger getTransactionGasLimit(Web3j web3j,Transaction transaction) {
		BigInteger gasLimit = BigInteger.ZERO;
		try {
			EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
			gasLimit = ethEstimateGas.getAmountUsed();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gasLimit;
	}

	/**
	 * 获取账号交易次数 nonce
	 *
	 * @param address 钱包地址
	 * @return nonce
	 */
	public static BigInteger getTransactionNonce(Web3j web3j,String address) {
		BigInteger nonce = BigInteger.ZERO;
		try {
			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
			nonce = ethGetTransactionCount.getTransactionCount();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nonce;
	}

	/**
	 * 发送一个普通交易
	 *
	 * @return 交易 Hash
	 */
	public static String sendTransaction(Web3j web3j,Admin admin,String fromAddress,String password,String toAddress,String amountVal) {
		BigInteger unlockDuration = BigInteger.valueOf(60L);
		BigDecimal amount = new BigDecimal(amountVal);
		String txHash = null;
		try {
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(fromAddress, password, unlockDuration).send();
			if (personalUnlockAccount.accountUnlocked()) {
				BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
				Transaction transaction = makeTransaction(fromAddress, toAddress,
						null, null, null, value);
				//不是必须的 可以使用默认值
				BigInteger gasLimit = getTransactionGasLimit(web3j,transaction);
				//不是必须的 缺省值就是正确的值
				BigInteger nonce = getTransactionNonce(web3j,fromAddress);
				//该值为大部分矿工可接受的gasPrice
				BigInteger gasPrice = Convert.toWei(defaultGasPrice, Convert.Unit.GWEI).toBigInteger();
				transaction = makeTransaction(fromAddress, toAddress,
						nonce, gasPrice,
						gasLimit, value);
				EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();
				txHash = ethSendTransaction.getTransactionHash();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("tx hash " + txHash);
		return txHash;
	}

	//使用 web3j.ethSendRawTransaction() 发送交易 需要用私钥自签名交易 详见ColdWallet.java
}
