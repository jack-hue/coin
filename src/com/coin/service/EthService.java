package com.coin.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.coin.eth.EthKeyPair;
import com.coin.eth.TokenClient;
import com.coin.utils.BigDecimalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EthService  {
	private static Logger logger = Logger.getLogger(EthService.class);
	public static BigDecimal WEI = new BigDecimal("1000000000000000000");
	public static BigInteger GAS_PRICE = BigInteger.valueOf(22000000000L);
	public static BigInteger GAS_LIMIT = BigInteger.valueOf(4300000L);
	public static BigInteger GAS_LIMIT_TOKEN = BigInteger.valueOf(90000L);
	/**
	 * erc20 转帐方法
	 */
	public static final String METHOD_CODE_ERC20_TRANSFER = "0xa9059cbb";
	public static final String METHOD_CODE_ERC20_TRANSFERFROM = "0x23b872dd";
	private Web3j web3j;
	private Admin admin;
	private Parity parity;

	public EthService( String url) {
		try {
			HttpService service = new HttpService(url);
			web3j = Web3j.build(service);
			parity = Parity.build(service);
			admin = Admin.build(service);
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * 方法描述：获取gas价格 
	 * 
	 * @param cxt
	 * @return BigInteger
	 */
	public BigInteger ethGasPriceWei() {
		try {
			return parity.ethGasPrice().send().getGasPrice();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取最新区块 
	 * 
	 * @return
	 * @throws IOException
	 *             BigInteger
	 */
	public BigInteger ethBlockNumber() {
		try {
			return parity.ethBlockNumber().send().getBlockNumber();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：获取区块高度 
	 * 
	 * @return int
	 */
	public int ethBlockHeight() {
		try {
			int height = 0;
			EthBlockNumber eNum = web3j.ethBlockNumber().send();
			if (!eNum.hasError()) {
				height = eNum.getBlockNumber().intValue();
			}
			if (height == 0) {
				EthSyncing syncing = web3j.ethSyncing().send();
				if (!syncing.hasError()) {
					if (syncing.isSyncing()) {
						EthSyncing.Syncing str = (EthSyncing.Syncing) syncing.getResult();
						BigInteger value = Numeric.decodeQuantity(str.getCurrentBlock());
						return value.intValue();
					} else {
						return 0;
					}
				} else {
					return 0;
				}
			} else {
				return height;
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * 方法描述：获取钱包中第一个默认地址 
	 * 
	 * @return
	 * @throws IOException
	 *             String
	 */
	public String ethCoinbase() {
		try {
			return parity.ethCoinbase().send().getAddress();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}



	/**
	 * 
	 * 方法描述：获取费用 
	 * 
	 * @param cxt
	 * @return BigDecimal
	 */
	public BigDecimal ethFee() {
		try {
			return new BigDecimal(GAS_LIMIT).multiply(new BigDecimal(ethGasPriceWei())).divide(WEI);
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：解锁账号 
	 * 
	 * @param addr
	 * @param passwd
	 * @return
	 * @throws IOException
	 *             boolean
	 */
	@Deprecated
	public boolean ethPersonalUnlockAccount(String addr, String passwd) {
		try {
			return parity.personalUnlockAccount(addr, passwd).send().accountUnlocked();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * 方法描述：解锁账号 duration是账户处于解锁状态的时间，例如300表示在300秒内无需再次解锁。成功解锁账户，返回 true。
	 * 
	 * @param addr
	 * @param passwd
	 * @param duration
	 * @return
	 * @throws IOException
	 *             boolean
	 */
	@Deprecated
	public boolean ethPersonalUnlockAccount(String addr, String passwd, BigInteger duration) {
		try {
			return parity.personalUnlockAccount(addr, passwd, duration).send().accountUnlocked();
			// Admin admin = Admin.build(new HttpService(url)); // defaults to
			// http://localhost:8545/
			// PersonalUnlockAccount personalUnlockAccount =
			// admin.personalUnlockAccount(addr, passwd,duration).send();
			// return personalUnlockAccount.accountUnlocked();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * 方法描述：根据txid，返回交易信息 
	 * 
	 * @param transaction
	 * @return
	 * @throws IOException
	 *             String
	 */

	public org.web3j.protocol.core.methods.response.Transaction ethGetTransactionByTxid(String txid) {
		try {
			return parity.ethGetTransactionByHash(txid).send().getResult();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	public TransactionReceipt ethGetTransactionReceipt(String txid) {
		try {
			return parity.ethGetTransactionReceipt(txid).send().getResult();
//			return parity.ethGetTransactionByHash(txid).send().getResult();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取交易记录 
	 * 
	 * @param blockNumber
	 * @return
	 * @throws IOException
	 *             List<TransactionResult>
	 */
	public List<TransactionResult> ethGetTransactionResult(long blockNumber) {
		try {
			DefaultBlockParameter defaultblockparameter = new DefaultBlockParameterNumber(blockNumber);
			return parity.ethGetBlockByNumber(defaultblockparameter, true).send().getBlock().getTransactions();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	public String ethGetAddressByWalletFile(String walletFilePath, String password) {
		try {
			File file = new File(walletFilePath);
			Credentials credentials = WalletUtils.loadCredentials(password, file);
			return credentials.getAddress();

		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：根据钱包文件地址获取公钥、私钥 
	 * 
	 * @param walletFilePath
	 * @param password
	 * @return ECKeyPair
	 */
	public ECKeyPair ethGetECKeyPairByWalletFile(String walletFilePath, String password) {
		try {
			File file = new File(walletFilePath);
			Credentials credentials = WalletUtils.loadCredentials(password, file);
			return credentials.getEcKeyPair();

		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	

	/**
	 * 
	 * 方法描述：获取账号列表 
	 * 
	 * @return List<String>
	 */
	public List<String> ethGetAccountlist() {
		try {
			return parity.ethAccounts().send().getAccounts();
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：获取账号 
	 * 
	 * @param index
	 * @return String
	 */
	public String ethGetAccount(int index) {
		String account = null;
		try {
			account = parity.ethAccounts().send().getAccounts().get(index);

		} catch (IOException e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return account;
	}

	@Deprecated
	public String ethCreateAccount(String password) {
		try {
			NewAccountIdentifier newAccountIdentifier = parity.personalNewAccount(password).send();
			if (newAccountIdentifier != null) {
				String accountId = newAccountIdentifier.getAccountId();
				return accountId;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	public EthKeyPair ethCreateLocalAccount(String walletPwd) {
		return ethCreateLocalAccount(walletPwd, "/data/keystore");
	}
	public EthKeyPair ethCreateLocalAccount(String walletPwd,String filePath) {
		try {
			File file=new File(filePath);
			if(!file.isDirectory()) {
				file.mkdirs();
			}
			Bip39Wallet wallet=	WalletUtils.generateBip39Wallet(walletPwd, new File(filePath));
			//keyStore文件名
//			String keyStoreKey = wallet.getFilename();
//			System.out.println("keyStoreKey="+keyStoreKey);
			//生成12个单词的助记词
//			String memorizingWords = wallet.getMnemonic();
//			System.out.println("memorizingWords="+memorizingWords);
//			通过钱包密码与助记词获得钱包地址、公钥及私钥信息
			Credentials credentials = WalletUtils.loadBip39Credentials(walletPwd,
			wallet.getMnemonic());
			String address=credentials.getAddress();
//			System.out.println("address="+address);
//			公钥16进制字符串表示：
			String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
//			System.out.println("publicKey="+publicKey);
//			私钥16进制字符串表示：
			String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
//			System.out.println("privateKey="+privateKey);
			EthKeyPair ethKeyPair=new EthKeyPair();
			ethKeyPair.setAddress(address);
			ethKeyPair.setPrivateKey(privateKey);
			ethKeyPair.setPublicKey(publicKey);
			return ethKeyPair;
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取余额 
	 * 
	 * @param accountId
	 * @return BigInteger
	 */
	public BigInteger ethGetBalanceWei(String accountId) {
		try {
			EthGetBalance ethGetBalance = parity.ethGetBalance(accountId, DefaultBlockParameterName.LATEST).send();
			if (ethGetBalance != null) {
				return ethGetBalance.getBalance();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	public BigInteger ethGetNonce(String accountId) {
		try {
			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(accountId, DefaultBlockParameterName.PENDING).sendAsync().get();
			return ethGetTransactionCount.getTransactionCount();
			
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取余额，将以太币位转换为整数 
	 * 
	 * @param cxt
	 * @param accountId
	 * @return BigInteger
	 */
	public Double ethGetBalance(String accountId) {
		return weiToDouble((ethGetBalanceWei(accountId)).toString());
	}

	public Double weiToDouble(String value) {
		return new BigDecimal(value).divide(WEI).doubleValue();
	}

	/**
	 * 
	 * 方法描述：解锁交易 
	 * 
	 * @param fromAddr
	 * @param passsword
	 * @param toAddr
	 * @param amountVal
	 * @return String
	 */
	@Deprecated
	public synchronized String ethUnlockAndSendTransaction(String fromAddr, String passsword, String toAddr, double amountVal) {

		try {
			boolean flag = ethPersonalUnlockAccount(fromAddr, passsword, BigInteger.valueOf(60L));
			BigDecimal amount = new BigDecimal(amountVal);
			String tradeHash = "";
			if (flag) {
				BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
				Transaction transaction = Transaction.createEtherTransaction(fromAddr, ethGetNonce(fromAddr), GAS_PRICE, GAS_LIMIT, toAddr, value);
				EthSendTransaction ethSendTransaction = parity.personalSendTransaction(transaction, passsword).send();
				if (ethSendTransaction != null) {
					tradeHash = ethSendTransaction.getTransactionHash();
					logger.error("账户:[" + fromAddr + "]转账到账户:[" + toAddr + "],交易金额:[" + amountVal + "],交易hash:["
							+ tradeHash + "]");
				}
			}
			return tradeHash;
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			logger.error("账户:[" + fromAddr + "]交易失败!", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：签名交易 
	 * 
	 * @param fromAddr
	 * @param passsword
	 * @param toAddr
	 * @param amountVal
	 * @return String
	 */
	public synchronized String ethSendSignTransaction(String fromAddr, String privateKey, String toAddr,
			double amountVal) {

		try {
			BigDecimal amount = new BigDecimal(amountVal);
			String tradeHash = "";
				Credentials credentials = Credentials.create(privateKey);
				BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
				RawTransaction rawTransaction = RawTransaction.createEtherTransaction(ethGetNonce(fromAddr), GAS_PRICE, GAS_LIMIT, toAddr,value);
				// 签名Transaction，这里要对交易做签名
				byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
				String hexValue = Numeric.toHexString(signedMessage);
				// 发送交易
				EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
				if (ethSendTransaction != null) {
					tradeHash = ethSendTransaction.getTransactionHash();
					logger.error("================ETH转账 账户:[" + fromAddr + "]转账到账户:[" + toAddr + "],交易金额:[" + amountVal + "],交易hash:["
							+ tradeHash + "]");
				}
			return tradeHash;
		} catch (Exception e) {
			logger.error("==============虚拟币-以太坊链接获取失败！");
			logger.error("账户:[" + fromAddr + "]交易失败!", e);
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 方法描述：获取私钥 
	 * 
	 * @param keystore
	 * @param password
	 * @return String
	 */
	public String getPrivateKey(String keystore, String password) {
		String privateKey = null;
		ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
		try {
			WalletFile walletFile = objectMapper.readValue(keystore, WalletFile.class);
			ECKeyPair ecKeyPair = null;
			ecKeyPair = Wallet.decrypt(password, walletFile);
			privateKey = ecKeyPair.getPrivateKey().toString(16);
		} catch (CipherException e) {
			if ("Invalid password provided".equals(e.getMessage())) {
				logger.error("密码错误");
			}
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return privateKey;
	}



	/**
	 * 
	 * 方法描述：获取代币余额 
	 * 
	 * @param fromAddress
	 * @param contractAddress
	 * @param decimal
	 * @return long
	 */
	public double tokenGetBalance(String fromAddress, String contractAddress, int decimal) {
		try {
			String methodName = "balanceOf";
			List<Type> inputParameters = new ArrayList<>();
			List<TypeReference<?>> outputParameters = new ArrayList<>();
			Address address = new Address(fromAddress);
			inputParameters.add(address);
			TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
			};
			outputParameters.add(typeReference);
			Function function = new Function(methodName, inputParameters, outputParameters);
			String data = FunctionEncoder.encode(function);
			Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

			EthCall ethCall;
			BigInteger balanceValue = BigInteger.ZERO;
			try {
				ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
				List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
				balanceValue = (BigInteger) results.get(0).getValue();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return BigDecimalUtil.div(new BigDecimal(balanceValue).doubleValue(), Math.pow(10, decimal), 8);
		} catch (Exception e) {
			logger.error("==============以太坊代币链接获取失败！");
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * 方法描述：获取代币名称 
	 * 
	 * @param web3j
	 * @param contractAddress
	 * @return String
	 */
	public String tokenGetName(String contractAddress) {
		try {
			return TokenClient.getTokenName(web3j, contractAddress);
		} catch (Exception e) {
			logger.error("==============以太坊代币链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：查询代币符号 
	 * 
	 * @param web3j
	 * @param contractAddress
	 * @return String
	 */
	public String tokenGetSymbol(String contractAddress) {
		try {
			return TokenClient.getTokenSymbol(web3j, contractAddress);
		} catch (Exception e) {
			logger.error("==============以太坊代币链接获取失败！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 方法描述：查询代币精度 
	 * 
	 * @param web3j
	 * @param contractAddress
	 * @return int
	 */
	public int tokenGetDecimals(String contractAddress) {
		try {
			return TokenClient.getTokenDecimals(web3j, contractAddress);
		} catch (Exception e) {
			logger.error("==============以太坊代币链接获取失败！");
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * 方法描述：查询代币发行总量 
	 * 
	 * @param web3j
	 * @param contractAddress
	 * @return BigInteger
	 */
	public BigInteger tokenGetTotalSupply(String contractAddress) {
		try {
			return TokenClient.getTokenTotalSupply(web3j, contractAddress);
		} catch (Exception e) {
			logger.error("==============以太坊代币链接获取失败！");
			e.printStackTrace();
		}

		return null;
	}


	
	@Deprecated
  public synchronized String tokenSendTransaction(String fromAddr,String passwd,String toAddr,String contractAddr,double value,int decimal){
	  String txHash="";
	  try {
		  PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(fromAddr, passwd, BigInteger.valueOf(10)).send();
		  if (personalUnlockAccount.accountUnlocked()) {
			  BigInteger valueLong = convertTokenValue(value,decimal);
			  Function function = new Function("transfer", Arrays.<Type>asList(new Address(toAddr),
					  new Uint256(valueLong)), Collections.emptyList());
			  String funEncode = FunctionEncoder.encode(function);
			  Transaction transaction = Transaction.createFunctionCallTransaction(fromAddr,ethGetNonce(fromAddr),ethGasPriceWei(),GAS_LIMIT_TOKEN,contractAddr,funEncode);
			  EthSendTransaction ethSendTransaction = parity.personalSendTransaction(transaction,passwd).send();
			  if(!ethSendTransaction.hasError()){
				  logger.error("eth代币转帐成功：转出地址：{"+fromAddr+"} 接受地址：{"+toAddr+"}  数量：{"+value+"}  合约地址：{"+contractAddr+"} txid:{"+ethSendTransaction.getTransactionHash()+"}");
				  txHash= ethSendTransaction.getTransactionHash();
			  }else{
				  logger.error("eth代币转帐失败 转出地址：{"+fromAddr+"} 合约地：{"+contractAddr+"}  接受地址：{"+toAddr+"} 数量：{"+value+"}  msg:{"+ethSendTransaction.getError().getMessage()+"}");
			  }
		  }
		  return txHash;
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
	  return "";
  }
    /**
     * 离线签名eth
     *
     * @param contractAddress//合约地址
     * @param to//转账的钱包地址
     * @param nonce//获取到的交易次数
     * @param gasPrice
     * @param gasLimit
     * @param value                 //转账的值
     * @return
     */
	   public static String signedEthContractTransactionData(String privateKey, String contractAddress, String to, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, Double value, Double decimal) throws Exception {
	        //因为每个代币可以规定自己的小数位, 所以实际的转账值=数值 * 10^小数位
	        BigDecimal realValue = BigDecimal.valueOf(value * Math.pow(10.0, decimal));

	        //0xa9059cbb代表某个代币的转账方法hex(transfer) + 对方的转账地址hex + 转账的值的hex
	        String data = METHOD_CODE_ERC20_TRANSFER + Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(to), 64) + Numeric.toHexStringNoPrefixZeroPadded(realValue.toBigInteger(), 64);
	        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
	        //手续费= (gasPrice * gasLimit ) / 10^18 ether

	        Credentials credentials = Credentials.create(privateKey);
	        //使用TransactionEncoder对RawTransaction进行签名操作
	        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
	        //转换成0x开头的字符串
	        return Numeric.toHexString(signedMessage);
	    }
	public synchronized String tokenSendLocalTransaction(String fromAddr,String privateKey,String toAddr,String contractAddr,double amountVal,int decimal){
		String txHash="";
		try {
			String tradeHash = "";
			   //因为每个代币可以规定自己的小数位, 所以实际的转账值=数值 * 10^小数位
			 BigDecimal realValue = BigDecimal.valueOf(amountVal * Math.pow(10.0, decimal));
			   //0xa9059cbb代表某个代币的转账方法hex(transfer) + 对方的转账地址hex + 转账的值的hex
		        String data = METHOD_CODE_ERC20_TRANSFER + Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(toAddr), 64) + Numeric.toHexStringNoPrefixZeroPadded(realValue.toBigInteger(), 64);
		        RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetNonce(fromAddr), GAS_PRICE, GAS_LIMIT, contractAddr, data);
		        //手续费= (gasPrice * gasLimit ) / 10^18 ether

		        Credentials credentials = Credentials.create(privateKey);
		        //使用TransactionEncoder对RawTransaction进行签名操作
		        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		        //转换成0x开头的字符串
			String hexValue = Numeric.toHexString(signedMessage);
			// 发送交易
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			if (ethSendTransaction != null) {
				tradeHash = ethSendTransaction.getTransactionHash();
				logger.error("eth代币转帐成功：转出地址：{"+fromAddr+"} 接受地址：{"+toAddr+"}  数量：{"+amountVal+"}  合约地址：{"+contractAddr+"} txid:{"+ethSendTransaction.getTransactionHash()+"}");
			}
			return tradeHash;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public synchronized String tokenSendLocalTransaction1(String fromAddr,String privateKey,String toAddr,String contractAddr,double amountVal,int decimal){
		String txHash="";
		try {
			String tradeHash = "";
				Credentials credentials = Credentials.create(privateKey);
				Address address = new Address(toAddr);
				Uint256 value = new Uint256(convertTokenValue(amountVal,decimal));
				List<Type> parametersList = new ArrayList<>();
				parametersList.add(address);
				parametersList.add(value);
				List<TypeReference<?>> outList = new ArrayList<>();
				Function function = new Function("transfer", parametersList, outList);
				String encodedFunction = FunctionEncoder.encode(function);
				RawTransaction rawTransaction = RawTransaction.createTransaction(ethGetNonce(fromAddr), GAS_PRICE, 
						GAS_LIMIT, contractAddr,encodedFunction);
				// 签名Transaction，这里要对交易做签名
				byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
				String hexValue = Numeric.toHexString(signedMessage);
				// 发送交易
				EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
				if (ethSendTransaction != null) {
					tradeHash = ethSendTransaction.getTransactionHash();
					logger.error("eth代币转帐成功：转出地址：{"+fromAddr+"} 接受地址：{"+toAddr+"}  数量：{"+amountVal+"}  合约地址：{"+contractAddr+"} txid:{"+ethSendTransaction.getTransactionHash()+"}");
				}
			return tradeHash;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public String getAddressFromeInpuData(String inputData) {
		if (StringUtils.isEmpty(inputData)) {
			return null;
		}
		try {
			return inputData.substring(10, 74);
		} catch (Exception e) {
			return null;
		}

	}

	public BigDecimal getValueFromData(String data, int decimal) {
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		try {
			String str = data.substring(74, 138);
			BigInteger a = new BigInteger(str, 16);
			return new BigDecimal(a).divide(new BigDecimal(Math.pow(10, decimal)), 8,BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			return null;
		}
	}

	public double webTogether(BigInteger value) {
		return BigDecimalUtil.div(value.doubleValue(), Math.pow(10, 18), 6);
	}

	/**
	 * 代币余额处理
	 * 
	 * @param data
	 * @param decimal
	 * @return
	 */
	public double getTokenBalanceFromResult(String data, int decimal) {
		if (data.startsWith("0x")) {
			data = data.substring(2);
		}
		long value = new BigInteger(data, 16).longValue();
		return BigDecimalUtil.div(value, Math.pow(10, decimal), 8);
	}

	/**
	 * 代币数额转化
	 * 
	 * @param value
	 * @param decimal
	 * @return
	 */
	public BigInteger convertTokenValue(double value, int decimal) {
		 BigDecimal bd1 = new BigDecimal(Double.toString(Math.pow(10, decimal)));
	        BigDecimal bd2 = new BigDecimal(value);
	        return bd1.multiply(bd2).toBigInteger();
	}

	
}
