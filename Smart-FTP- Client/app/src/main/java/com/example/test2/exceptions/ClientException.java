package com.example.test2.exceptions;

public class ClientException extends Exception {

    public ClientException() {

    }
    public ClientException(String response) {
        super(response);
    }
}
