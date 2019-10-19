package com.coin.neo.Core;


import com.coin.neo.Core.Scripts.Program;
import com.coin.neo.Fixed8;
import com.coin.neo.IO.BinaryReader;
import com.coin.neo.IO.BinaryWriter;
import com.coin.neo.UInt160;
import com.coin.neo.Wallets.Contract;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class InvocationTransaction extends Transaction {
	public byte[] script;
	public Fixed8 gas;
	public ECPoint invoker;

	public InvocationTransaction() {
		super(TransactionType.InvocationTransaction);
	}
	public InvocationTransaction(ECPoint invoker) {
		super(TransactionType.InvocationTransaction);
		this.invoker = invoker;
	}
	@Override
	protected void deserializeExclusiveData(BinaryReader reader) throws IOException {
		try {
			script = reader.readVarBytes();
			gas = reader.readSerializable(Fixed8.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void serializeExclusiveData(BinaryWriter writer) throws IOException {
		writer.writeVarBytes(script);
		writer.writeSerializable(gas);
	}
	@Override
	public UInt160[] getScriptHashesForVerifying() {
		HashSet<UInt160> hashes = new HashSet<UInt160>(Arrays.asList(super.getScriptHashesForVerifying()));
		hashes.add(Program.toScriptHash(Contract.createSignatureRedeemScript(invoker)));
		return hashes.stream().sorted().toArray(UInt160[]::new);
	}
}
