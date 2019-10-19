package com.coin.neo.IO;

import java.io.IOException;

import com.coin.neo.IO.Json.JObject;

public class JsonWriter {
	private JObject json;
	
	public JsonWriter(JObject json) {
		this.json = json;
	}
	public void writeSerializable(JsonSerializable v) throws IOException {
		v.toJson(this);
	}
	
	public void writeSerializableArray(JsonSerializable[] v) throws IOException {
		for (int i = 0; i < v.length; i++) {
			v[i].toJson(this);
		}
	}
	
	public JObject json() {
		return json;
	}

}
