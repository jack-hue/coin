package com.coin.neo.Core;

import com.coin.neo.Fixed8;

public class Claimable {
    public TransactionOutput output;
    public int startHeight;
    public int endHeight;

    public Fixed8 value() {
    	return output.value;
    }
}
