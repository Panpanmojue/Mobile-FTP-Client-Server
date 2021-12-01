package com.example.ftpsever.response;

public class TransferFailedResponse extends AbstractResponse {

    public TransferFailedResponse() {
        super(426, "连接关闭，传输失败");
    }
}
