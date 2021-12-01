package com.example.ftpsever.response;

public class CommandOrderWrongResponse extends AbstractResponse {

    public CommandOrderWrongResponse() {
        super(503, "命令顺序错误");
    }
}
