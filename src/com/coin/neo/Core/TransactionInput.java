package com.coin.neo.Core;

import java.io.IOException;

import com.coin.neo.UInt256;
import com.coin.neo.IO.BinaryReader;
import com.coin.neo.IO.BinaryWriter;
import com.coin.neo.IO.Serializable;
import com.coin.neo.IO.Json.JNumber;
import com.coin.neo.IO.Json.JObject;
import com.coin.neo.IO.Json.JString;

/**
 *  交易输入
 */
public class TransactionInput implements Serializable {
    /**
     *  引用交易的散列值
     */
    public UInt256 prevHash;
    /**
     *  引用交易输出的索引
     */
    public short prevIndex; 

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (null == obj) {
        	return false;
        }
        if (!(obj instanceof TransactionInput)) {
        	return false;
        }
        TransactionInput other = (TransactionInput) obj;
        return prevHash.equals(other.prevHash) && prevIndex == other.prevIndex;
    }

    @Override
    public int hashCode() {
        return prevHash.hashCode() + prevIndex;
    }

    /**
	 * byte格式数据反序列化
	 */
    @Override
	public void deserialize(BinaryReader reader) throws IOException {
		try {
			prevHash = reader.readSerializable(UInt256.class);
			prevIndex = reader.readShort();
//			prevIndex = (short) reader.readVarInt();
		} catch (InstantiationException | IllegalAccessException e) {
		}
	}
	@Override
	public void serialize(BinaryWriter writer) throws IOException {
		writer.writeSerializable(prevHash);
		writer.writeShort(prevIndex);
//		writer.writeVarInt(prevIndex);
	}
	
	public JObject json() {
        JObject json = new JObject();
        json.set("txid", new JString(prevHash.toString()));
        json.set("vout", new JNumber(Short.toUnsignedInt(prevIndex)));
        return json;
    }

	@Override
	public String toString() {
		return "TransactionInput [prevHash=" + prevHash + ", prevIndex="
				+ prevIndex + "]";
	}
}
