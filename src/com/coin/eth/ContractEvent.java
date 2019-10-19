package com.coin.eth;


import java.util.Arrays;
import java.util.List;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

/**
 * 
 * All rights Reserved, Designed By 郑州好聚点科技有限公司
 * @Description:执行合约相关log监听
 * @author: auth    
 * @date:   2018年7月4日 下午2:23:27   
 * @Copyright: 2018 http://www.hjd123.com Inc. All rights reserved.
 */
public class ContractEvent {
	public static String contractAddress = "0x4c1ae77bc2df45fb68b13fa1b4f000305209b0cb";

	public static void main(String[] args) {
//		eventContract();
		
	}
	/**
	 * 监听ERC20 token 交易
	 */
	public static void eventContract(Web3j web3j){
		EthFilter filter = new EthFilter(
				DefaultBlockParameterName.EARLIEST,
				DefaultBlockParameterName.LATEST,
				contractAddress);
		Event event = new Event("Transfer",
				Arrays.<TypeReference<?>>asList(
						new TypeReference<Address>() {
						},
						new TypeReference<Address>() {
						}
				),
				Arrays.<TypeReference<?>>asList(
						new TypeReference<Uint256>() {
						}
				)
		);

		String topicData = EventEncoder.encode(event);
		filter.addSingleTopic(topicData);
		System.out.println(topicData);

		web3j.ethLogObservable(filter).subscribe(log -> {
			System.out.println(log.getBlockNumber());
			System.out.println(log.getTransactionHash());
			List<String> topics = log.getTopics();
			for (String topic : topics) {
				System.out.println(topic);
			}
		});
	}
}
