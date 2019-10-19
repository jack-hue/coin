package com.coin.neo.Core;

import java.io.IOException;

import com.coin.neo.IO.BinaryReader;
import com.coin.neo.IO.BinaryWriter;

public class MinerTransaction extends Transaction {
	private long nonce; // nonce is not exist when version=2

	public MinerTransaction() {
		super(TransactionType.MinerTransaction);
	}

	@Override
	protected void deserializeExclusiveData(BinaryReader reader) throws IOException {
		if(version == 3) {
			nonce = reader.readLong();
		}
	}
	
	@Override
	protected void serializeExclusiveData(BinaryWriter writer) throws IOException {
		if(version == 3) {
			writer.writeLong(nonce);
		}
	}
}
