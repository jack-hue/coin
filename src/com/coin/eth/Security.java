package com.coin.eth;


import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;

/**
 * 
 * All rights Reserved, Designed By 郑州好聚点科技有限公司
 * @Description:公钥私钥相关接口
 * @author: auth    
 * @date:   2018年7月4日 下午2:29:01   
 * @Copyright: 2018 http://www.hjd123.com Inc. All rights reserved.
 */
public class Security {

	Web3j web3j= Web3j.build(new HttpService("http://127.0.0.1:8545"));
	public static void main(String[] args) {

		exportPrivateKey("E:\\wallet\\eth\\keystore\\UTC--2018-07-04T07-12-24.273071600Z--c027c278d6c6256360f423f3f23bd40a8bf103bc",
				"123456789");

//		importPrivateKey(new BigInteger("", 16),
//				"yzw",
//				WalletUtils.getTestnetKeyDirectory());
//
//		exportBip39Wallet(WalletUtils.getTestnetKeyDirectory(),
//				"yzw");
	}

	/**
	 * 导出私钥
	 *
	 * @param keystorePath 账号的keystore路径
	 * @param password     密码
	 */
	public static void exportPrivateKey(String keystorePath, String password) {
		try {
			Credentials credentials = WalletUtils.loadCredentials(
					password,
					keystorePath);
			BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
			System.out.println(privateKey.toString(16));
		} catch (IOException | CipherException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导入私钥
	 *
	 * @param publicKey 私钥
	 * @param password   密码
	 * @param directory  存储路径 默认测试网络WalletUtils.getTestnetKeyDirectory() 默认主网络 WalletUtils.getMainnetKeyDirectory()
	 */
	public static void importPrivateKey(BigInteger publicKey, String password, String directory) {
		ECKeyPair ecKeyPair = ECKeyPair.create(publicKey);
		try {
			String keystoreName = WalletUtils.generateWalletFile(password,
					ecKeyPair,
					new File(directory),
					true);
			System.out.println("keystore name " + keystoreName);
		} catch (CipherException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成带助记词的账号
	 *
	 * @param keystorePath
	 * @param password
	 */
	public static void exportBip39Wallet(String keystorePath, String password) {
		try {
			// TODO: 2018/3/14 会抛异常 已经向官方提issue 待回复
			Bip39Wallet bip39Wallet = WalletUtils.generateBip39Wallet(password, new File(keystorePath));
			System.out.println(bip39Wallet);
		} catch (CipherException | IOException e) {
			e.printStackTrace();
		}
	}

}
