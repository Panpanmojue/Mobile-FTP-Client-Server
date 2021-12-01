package com.example.ftpsever.response;

public class ConnectionAlreadyOpenResponse extends AbstractResponse {

    public ConnectionAlreadyOpenResponse() {
        super(125, "数据连接已打开，传输开始");
    }
}
