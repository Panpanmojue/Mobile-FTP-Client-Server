package com.example.ftpsever.response;

public class NoImplementationResponse extends AbstractResponse {
    public NoImplementationResponse() {
        super(502, "还未实现");
    }
}
