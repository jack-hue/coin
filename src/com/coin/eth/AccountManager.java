package com.coin.eth;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;

/**
 * 
 * All rights Reserved, Designed By 郑州好聚点科技有限公司
 * @Description:账号管理相关
 * @author: auth    
 * @date:   2018年7月3日 上午11:45:54   
 * @Copyright: 2018 http://www.hjd123.com Inc. All rights reserved.
 */
public class AccountManager {

//	public static void main(String[] args) {
//		admin = Admin.build(new HttpService(Environment.RPC_URL));
//		createNewAccount();
//		getAccountList();
//		unlockAccount();
//
//		admin.personalSendTransaction(); 该方法与web3j.sendTransaction相同 不在此写例子。
//	}

	/**
	 * 创建账号
	 * @throws IOException 
	 */
	public static String createNewAccount(Admin admin,String password) throws IOException {
			NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(password).send();
			String address = newAccountIdentifier.getAccountId();
			return address;
	}

	/**
	 * 获取账号列表
	 * @throws IOException 
	 */
	public static List<String> getAccountList(Admin admin) throws IOException {
			PersonalListAccounts personalListAccounts = admin.personalListAccounts().send();
			List<String> addressList= personalListAccounts.getAccountIds();
			return addressList;
	}

	/**
	 * 账号解锁
	 * @throws IOException 
	 */
	public static boolean unlockAccount(Admin admin,String address,String password) throws IOException {
		//账号解锁持续时间 单位秒 缺省值300秒
		BigInteger unlockDuration = BigInteger.valueOf(60L);
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, password, unlockDuration).send();
			return  personalUnlockAccount.accountUnlocked();
	}

}
