package com.yarg.robotpiserver.util;

import com.google.gson.Gson;
import com.yarg.robotpiserver.util.response.model.TestResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendRequest {

    private static final int TIMEOUT = 60000;
    private static final Gson gson = new Gson();

    /**
     * Send a 'get' request and return the response details.
     * @param urlPath URL path
     * @return Response details.
     * @throws IOException Pass through exception.
     */
    public static TestResponse get(String urlPath) throws IOException {

        URL url = new URL(urlPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);

        int status = con.getResponseCode();
        String response = null;
        try {
            response = getResponsePayload(con.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        con.disconnect();

        return new TestResponse().setPayload(response).setStatusCode(status);
    }

    /**
     * Send a 'post' request and return the response details.
     * @param urlPath URL path
     * @param payloadObject Object to serialize as the payload.
     * @return Response details.
     * @throws IOException Pass through exception.
     */
    public static TestResponse post(String urlPath, Object payloadObject) throws IOException {

        URL url = new URL(urlPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);
        con.setDoInput(true);
        con.setDoOutput(true);

        String payload = gson.toJson(payloadObject);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
        outputStreamWriter.write(payload);
        outputStreamWriter.close();

        int status = con.getResponseCode();
        String response = null;
        try {
            response = getResponsePayload(con.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        con.disconnect();

        return new TestResponse().setPayload(response).setStatusCode(status);
    }

    private static String getResponsePayload(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
