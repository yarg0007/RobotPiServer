package com.yarg.robotpiserver.server.handler;

public class HandlerResponse<ResponseModel> {

    private final HandlerResponseCode responseCode;
    private final ResponseModel responseModel;

    public HandlerResponse(HandlerResponseCode responseCode, ResponseModel responseModel) {
        this.responseCode = responseCode;
        this.responseModel = responseModel;
    }

    /**
     * Get the response code to return to client.
     * @return Response code.
     */
    public HandlerResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * Get the response model to return to client.
     * @return Response model.
     */
    public ResponseModel getResponseModel() {
        return responseModel;
    }
}
