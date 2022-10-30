package com.yarg.robotpiserver.server.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseModel {

	private int responseCode;
	private Map<String, List<String>> headers;
	private String payload;

	public ResponseModel(int responseCode, Map<String, List<String>> headers, String payload) {

		this.responseCode = responseCode;

		if (headers == null) {
			this.headers = new HashMap<>();
		} else {
			this.headers = new HashMap<>(headers);
		}

		if (payload == null) {
			this.payload = "";
		} else {
			this.payload = payload;
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public String getPayload() {
		return payload;
	}

}
