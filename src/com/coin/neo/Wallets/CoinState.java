package com.coin.neo.Wallets;

public enum CoinState {
    Unconfirmed,
    Unspent,
    Spending,
    Spent,
    SpentAndClaimed
}