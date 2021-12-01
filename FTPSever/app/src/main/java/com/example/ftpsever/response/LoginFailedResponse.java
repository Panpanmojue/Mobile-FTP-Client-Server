package com.example.ftpsever.response;

public class LoginFailedResponse extends AbstractResponse {
    public LoginFailedResponse() {
        super(530, "Login failed!无法登录");
    }
}