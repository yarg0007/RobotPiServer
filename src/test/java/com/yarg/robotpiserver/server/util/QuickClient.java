package com.yarg.robotpiserver.server.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class QuickClient {

	/**
	 * Send a get request and return response model.
	 *
	 * @param urlValue URL to send.
	 * @return Response model.
	 * @throws Exception Pass through.
	 */
	public static ResponseModel sendGet(String urlValue) throws Exception {

		URL url = new URL(urlValue);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		Map<String, List<String>> headers = connection.getHeaderFields();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return new ResponseModel(responseCode, headers, response.toString());
	}
}
