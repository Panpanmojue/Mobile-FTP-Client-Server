package com.example.ftpsever.response;

public class OpenDataConnectionFailedResponse extends AbstractResponse {

    public OpenDataConnectionFailedResponse() {
        super(425, "建立连接失败！");
    }
}
