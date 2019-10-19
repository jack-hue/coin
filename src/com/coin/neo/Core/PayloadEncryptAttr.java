package com.coin.neo.Core;

import com.coin.neo.IO.Serializable;

public interface PayloadEncryptAttr extends Serializable{
	public byte[] encrypt(byte[] msg, byte[] keys);
	public byte[] decrypt(byte[] msg, byte[] keys);
}
