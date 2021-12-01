package com.example.ftpsever.response;

public class RequirePasswordResponse extends AbstractResponse {
    public RequirePasswordResponse() {
        super(331, "需要密码");
    }
}
