package com.example.ftpsever.response;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public abstract class AbstractResponse {
    protected int code;
    protected String message;

    public AbstractResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("%d %s", code, message);
    }
}
