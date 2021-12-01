package com.example.ftpsever.response;

public class LoginSuccessResponse extends AbstractResponse {

    public LoginSuccessResponse() {
        super(230, "用户登录成功！");
    }
}

