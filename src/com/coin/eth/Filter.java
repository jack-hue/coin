package com.coin.eth;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.math.BigInteger;

/** 
 * 新块、新交易相关监听
 * 
 * filter相关
 * 监听区块、交易
 * 所有监听都在Web3jRx中
 */
public class Filter {

	public static void main(String[] args) {
		Web3j web3j=null;
//		web3j = Web3j.build(new HttpService(Environment.RPC_URL));
		/**
		 * 新区块监听
		 */
		newBlockFilter(web3j);
		/**
		 * 新交易监听
		 */
		newTransactionFilter(web3j);
		/**
		 * 遍历旧区块、交易
		 */
		replayFilter(web3j);
		/**
		 * 从某一区块开始直到最新区块、交易
		 */
		catchUpFilter(web3j);

		/**
		 * 取消监听
		 */
		//subscription.unsubscribe();
	}

	public static void newBlockFilter(Web3j web3j) {
		Subscription subscription = web3j.
				blockObservable(false).
				subscribe(block -> {
					System.out.println("new block come in");
					System.out.println("block number" + block.getBlock().getNumber());
				});
	}

	public static void newTransactionFilter(Web3j web3j) {
		Subscription subscription = web3j.
				transactionObservable().
				subscribe(transaction -> {
					System.out.println("transaction come in");
					System.out.println("transaction txHash " + transaction.getHash());
				});
	}

	public static void replayFilter(Web3j web3j) {
		BigInteger startBlock = BigInteger.valueOf(2000000);
		BigInteger endBlock = BigInteger.valueOf(2010000);
		/**
		 * 遍历旧区块
		 */
		Subscription subscription = web3j.
				replayBlocksObservable(
						DefaultBlockParameter.valueOf(startBlock),
						DefaultBlockParameter.valueOf(endBlock),
						false).
				subscribe(ethBlock -> {
					System.out.println("replay block");
					System.out.println(ethBlock.getBlock().getNumber());
				});

		/**
		 * 遍历旧交易
		 */
		Subscription subscription1 = web3j.
				replayTransactionsObservable(
						DefaultBlockParameter.valueOf(startBlock),
						DefaultBlockParameter.valueOf(endBlock)).
				subscribe(transaction -> {
					System.out.println("replay transaction");
					System.out.println("txHash " + transaction.getHash());
				});
	}

	public static void catchUpFilter(Web3j web3j) {
		BigInteger startBlock = BigInteger.valueOf(2000000);

		/**
		 * 遍历旧区块，监听新区块
		 */
		Subscription subscription = web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
				DefaultBlockParameter.valueOf(startBlock), false)
				.subscribe(block -> {
					System.out.println("block");
					System.out.println(block.getBlock().getNumber());
				});

		/**
		 * 遍历旧交易，监听新交易
		 */
		Subscription subscription2 = web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
				DefaultBlockParameter.valueOf(startBlock))
				.subscribe(tx -> {
					System.out.println("transaction");
					System.out.println(tx.getHash());
				});
	}
}
