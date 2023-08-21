package com.yarg.robotpiserver.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HandlerBaseTest {

    private HttpExchange exchange = mock(HttpExchange.class);
    private HandlerBase handlerBase = spy(HandlerBase.class);
    private HandlerResponse<ResponseModel> handlerResponse = mock((HandlerResponse.class));
    private OutputStream outputStream = mock(OutputStream.class);
    private Gson gson = new Gson();

    @BeforeMethod(alwaysRun = true)
    public void reset() {
        Mockito.reset(exchange, handlerBase, handlerResponse, outputStream);
    }

    // -----------------------------------------------
    // handle()

    @Test
    public void success() throws Exception {

        // Setup the data we will hand back
        ResponseModel responseModel = new ResponseModel("Success");
        when(handlerResponse.getResponseModel()).thenReturn(responseModel);
        when(handlerResponse.getResponseCode()).thenReturn(HandlerResponseCode.SUCCESS);

        // Wire up the rest
        when(handlerBase.handleRequest(exchange)).thenReturn(handlerResponse);
        when(exchange.getResponseBody()).thenReturn(outputStream);
        doNothing().when(outputStream).write(any(byte[].class));
        doNothing().when(outputStream).close();

        handlerBase.handle(exchange);

        String response = gson.toJson(responseModel);

        verify(exchange, times(1)).sendResponseHeaders(HandlerResponseCode.SUCCESS.getResponseCode(), response.getBytes().length);
        verify(outputStream, times(1)).write(response.getBytes());
        verify(outputStream, times(1)).close();
    }

    @Test
    public void error() throws Exception {

        // Setup the data we will hand back
        ResponseModel responseModel = new ResponseModel("Internal Server Error");
        when(handlerResponse.getResponseModel()).thenReturn(responseModel);
        when(handlerResponse.getResponseCode()).thenReturn(HandlerResponseCode.INTERNAL_SERVER_ERROR);

        // Wire up the rest
        when(handlerBase.handleRequest(exchange)).thenReturn(handlerResponse);
        when(exchange.getResponseBody()).thenReturn(outputStream);
        doNothing().when(outputStream).write(any(byte[].class));
        doNothing().when(outputStream).close();

        handlerBase.handle(exchange);

        String response = gson.toJson(responseModel);

        verify(exchange, times(1)).sendResponseHeaders(HandlerResponseCode.INTERNAL_SERVER_ERROR.getResponseCode(), response.getBytes().length);
        verify(outputStream, times(1)).write(response.getBytes());
        verify(outputStream, times(1)).close();
    }

    // ----------------------------------------------------
    // readRequestBody()

    @Test
    public void readNullRequestBody() throws Exception {
        String result = handlerBase.readRequestBody(null);
        assertThat(result, is(equalTo("")));
    }

    @Test
    public void readEmptyRequestBody() throws Exception {
        String inputValue = "";
        InputStream inputStream = new ByteArrayInputStream(inputValue.getBytes());
        String result = handlerBase.readRequestBody(inputStream);
        assertThat(result, is(equalTo("")));
    }

    @Test
    public void readRequestBody() throws Exception {
        String inputValue = "{ \"test\": \"value\" }";
        InputStream inputStream = new ByteArrayInputStream(inputValue.getBytes());
        String result = handlerBase.readRequestBody(inputStream);
        assertThat(result, is(equalTo(inputValue)));
    }

    /**
     * Model to serialize in response for testing.
     */
    class ResponseModel {
        private final String message;
        public ResponseModel(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}