package com.example.test2.exceptions;

public class UserInputException extends Exception {
    public UserInputException() {
        System.out.println("输入不合法");
    }
}
