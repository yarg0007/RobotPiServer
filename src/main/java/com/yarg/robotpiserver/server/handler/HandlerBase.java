package com.yarg.robotpiserver.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class HandlerBase<ResponseModel> implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        HandlerResponse<ResponseModel> response = handleRequest(exchange);
        HandlerResponseCode responseCode = response.getResponseCode();
        ResponseModel responseModel = response.getResponseModel();

        Gson gson = new Gson();
        String responseBody = gson.toJson(responseModel);

        //Set the response header status and length
        exchange.sendResponseHeaders(responseCode.getResponseCode(), responseBody.getBytes().length);

        //Write the response string
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }

    /**
     * Read in the request payload input stream and return as a String.
     * @param requestInputStream Request input stream to read in.
     * @return String representation of the input stream.
     * @throws IOException Pass through exception from reading input stream.
     */
    protected String readRequestBody(InputStream requestInputStream) {

        if (requestInputStream == null) {
            return "";
        }

        StringBuilder requestBody = new StringBuilder();
        Reader reader = new BufferedReader(new InputStreamReader(requestInputStream, StandardCharsets.UTF_8));
        int character = 0;
        try {
            while ((character = reader.read()) != -1) {
                requestBody.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return requestBody.toString();
    }

    public abstract HandlerResponse<ResponseModel> handleRequest(HttpExchange exchange);
}
