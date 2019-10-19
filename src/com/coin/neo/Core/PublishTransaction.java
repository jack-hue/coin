package com.coin.neo.Core;


import com.coin.neo.IO.BinaryReader;
import com.coin.neo.IO.BinaryWriter;
import com.coin.neo.Core.Contract.ContractParameterType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublishTransaction extends Transaction {
	public byte[] script;
	public ContractParameterType[] parameterList;
	public ContractParameterType returnType;
	public boolean needStorage;
	public String name;
	public String codeVersion;
	public String author;
	public String email;
	public String description;
	
	public PublishTransaction() {
		super(TransactionType.PublishTransaction);
	}
	@Override
	protected void deserializeExclusiveData(BinaryReader reader) throws IOException {
		script = reader.readVarBytes();
		parameterList = toEnum(reader.readVarBytes());
		returnType = toEnum(reader.readByte());
		needStorage = reader.readBoolean();
		name = reader.readVarString();
		codeVersion = reader.readVarString();
		author = reader.readVarString();
		email = reader.readVarString();
		description = reader.readVarString();
	}
	@Override
	protected void serializeExclusiveData(BinaryWriter writer) throws IOException {
		writer.writeVarBytes(script);
		writer.writeVarBytes(toByte(parameterList));
		writer.writeByte((byte)returnType.ordinal());
		writer.writeBoolean(needStorage);
		writer.writeVarString(name);
		writer.writeVarString(codeVersion);
		writer.writeVarString(author);
		writer.writeVarString(email);
		writer.writeVarString(description);
	}
	private ContractParameterType toEnum(byte bt) {
		return Arrays.stream(ContractParameterType.values()).filter(p -> p.ordinal() == bt).findAny().get();
	}
	private ContractParameterType[] toEnum(byte[] bt) {
		if(bt == null) {
			return null;
		}
		List<ContractParameterType> list = new ArrayList<ContractParameterType>();
		for(byte b: bt) {
			ContractParameterType type = toEnum(b);
			list.add(type);
		}
		return list.stream().toArray(ContractParameterType[]::new);
	}
	private byte[] toByte(ContractParameterType[] types) {
		if(types == null) {
			return new byte[0];
		}
		int len = types.length;
		byte[] bt = new byte[len];
		for(int i=0; i<len; ++i) {
			bt[i] = (byte) types[i].ordinal();
		}
		return bt;
	}
}
