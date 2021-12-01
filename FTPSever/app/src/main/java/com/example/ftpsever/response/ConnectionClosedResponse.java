package com.example.ftpsever.response;

public class ConnectionClosedResponse extends AbstractResponse {
    public ConnectionClosedResponse() {
        super(221, "服务器关闭控制连接，已注销");
    }
}
