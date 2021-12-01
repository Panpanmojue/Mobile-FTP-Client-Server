package com.example.ftpsever.response;


public class ArgumentWrongResponse extends AbstractResponse {

    public ArgumentWrongResponse(String message) {
        super(501, message);
    }

    public ArgumentWrongResponse() {
        super(501, "参数语法错误");
    }

}
