package com.coin.neo.Implementations.Wallets;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.coin.neo.Core.Block;
import com.coin.neo.Core.Transaction;
import com.coin.neo.Wallets.Account;
import com.coin.neo.Wallets.Coin;
import com.coin.neo.Wallets.Contract;
import com.coin.neo.Wallets.Wallet;


public class AbstractWallet extends Wallet {

	protected AbstractWallet(String path, String password, boolean create)
			throws BadPaddingException, IllegalBlockSizeException {
		super(path, password, create);
	}

	@Override
	protected Account[] loadAccounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Contract[] loadContracts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Coin[] loadCoins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] loadStoredData(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveStoredData(String name, byte[] value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onProcessNewBlock(Block block, Coin[] added, Coin[] changed,
			Coin[] deleted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onSaveTransaction(Transaction tx, Coin[] added,
			Coin[] changed) {
		// TODO Auto-generated method stub
		
	}

}
