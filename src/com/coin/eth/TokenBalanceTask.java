package com.coin.eth;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量查询token代币余额
 */
public class TokenBalanceTask {

	public class Token {
		public String contractAddress;
		public int decimals;
		public String name;

		public Token(String contractAddress) {
			this.contractAddress = contractAddress;
			this.decimals = 0;
		}

		public Token(String contractAddress, int decimals) {
			this.contractAddress = contractAddress;
			this.decimals = decimals;
		}
	}


	//要查询的token合约地址
	public static List<Token> tokenList;

	//要查询的钱包地址
	public static List<String> addressList;
	public static void main(String[] args) {
		Web3j web3j= Web3j.build(new HttpService("http://127.0.0.1:8545"));
//		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		loadData();
		//如果没有decimals则需要请求
		requestDecimals(web3j);
		requestName(web3j);
		processTask(web3j);
	}


	public static void loadData() {
		tokenList = new ArrayList<>();
		// TODO: 2018/3/14 add...
		addressList = new ArrayList<>();
		// TODO: 2018/3/14 add...
	}

	public static void requestDecimals(Web3j web3j) {
		for (Token token : tokenList) {
			token.decimals = TokenClient.getTokenDecimals(web3j, token.contractAddress);
		}
	}

	public static void requestName(Web3j web3j) {
		for (Token token : tokenList) {
			token.name = TokenClient.getTokenName(web3j, token.contractAddress);
		}
	}

	public static void processTask(Web3j web3j) {
		for (String address : addressList) {
			for (Token token : tokenList) {
				BigDecimal balance = new BigDecimal(TokenClient.getTokenBalance(web3j, address, token.contractAddress));
				balance.divide(BigDecimal.TEN.pow(token.decimals));
				System.out.println("address " + address + " name " + token.name + " balance " + balance);
			}
		}
	}
}
