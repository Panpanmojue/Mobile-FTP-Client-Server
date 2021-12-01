package com.example.ftpsever.response;


import android.annotation.SuppressLint;

public class EnterPassModeResponse extends AbstractResponse {

    @SuppressLint("DefaultLocale")
    public EnterPassModeResponse(String localAddress, int p1, int p2) {
        super(227, String.format("进入被动模式(%s,%d,%d)", localAddress.replace('.', ','), p1, p2));
    }
}
