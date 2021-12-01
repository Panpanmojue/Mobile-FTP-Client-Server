package com.example.ftpsever.response;

public class TransferSuccessResponse extends AbstractResponse {
    public TransferSuccessResponse() {
        super(226, "请求文件动作成功");
    }
}
