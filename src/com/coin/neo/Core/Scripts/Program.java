package com.coin.neo.Core.Scripts;

import java.io.IOException;

import com.coin.neo.Helper;
import com.coin.neo.UInt160;
import com.coin.neo.Cryptography.Digest;
import com.coin.neo.IO.BinaryReader;
import com.coin.neo.IO.BinaryWriter;
import com.coin.neo.IO.JsonReader;
import com.coin.neo.IO.JsonSerializable;
import com.coin.neo.IO.JsonWriter;
import com.coin.neo.IO.Serializable;
import com.coin.neo.IO.Json.JObject;
import com.coin.neo.IO.Json.JString;

/**
 *  脚本
 */
public class Program implements Serializable, JsonSerializable {
    public byte[] parameter;
    public byte[] code;

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
    	parameter = reader.readVarBytes();	// sign data
    	code = reader.readVarBytes();		// pubkey
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
    	writer.writeVarBytes(parameter);
    	writer.writeVarBytes(code);
    }

    /**
     *  变成json对象
     *  <returns>返回json对象</returns>
     */
    public JObject json() {
        JObject json = new JObject();
        json.set("code", new JString(Helper.toHexString(code)));
        json.set("parameter", new JString(Helper.toHexString(parameter)));
        return json;
    }

    public static UInt160 toScriptHash(byte[] script) {
    	return new UInt160(Digest.hash160(script));
    }
    
	/**
	 * byte格式数据反序列化
	 */
	@Override
	public void fromJson(JsonReader reader) {
		JObject json = reader.json();
		code = Helper.hexToBytes(json.get("Code").asString());
		parameter = Helper.hexToBytes(json.get("Parameter").asString());
	}
	
	@Override
	public void toJson(JsonWriter writer) {
		// ...
	}
}
