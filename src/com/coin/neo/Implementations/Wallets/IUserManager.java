package com.coin.neo.Implementations.Wallets;

import java.util.Map;

import com.coin.neo.Fixed8;
import com.coin.neo.UInt160;
import com.coin.neo.Core.SignatureContext;
import com.coin.neo.Core.Transaction;
import com.coin.neo.Wallets.Account;
import com.coin.neo.Wallets.Coin;
import com.coin.neo.Wallets.CoinException;
import com.coin.neo.Wallets.Contract;

public interface IUserManager {
	
	public void start();
	
	public void close();
	
	public Account createAccount();
	
	public Account createAccount(byte[] privateKey);
	
	public Contract getContract(String address);
	
	public Account[] getAccounts();
	
	public Contract[] getContracts();
	
	public void rebuild();

	public <T extends Transaction> T makeTransaction(T regTx,Fixed8 zero) throws CoinException;
	
	public <T extends Transaction> T makeTransaction(T tx, Fixed8 fee, UInt160 from) throws CoinException;
	
    public boolean saveTransaction(Transaction tx);

    public boolean sign(SignatureContext context);

    public boolean hasFinishedSyncBlock() throws Exception;

	public Account getAccount(UInt160 publicKeyHash);

	public Account getAccountByScriptHash(UInt160 scriptHash);

	public Coin[] findUnspentCoins();
	
	public Coin[] findUnconfirmedCoins();

	public Map<Transaction, Integer> LoadTransactions();
	
	public Coin[] getCoin();
	
	public int getBlockHeight();
	public int getWalletHeight();
	
}
