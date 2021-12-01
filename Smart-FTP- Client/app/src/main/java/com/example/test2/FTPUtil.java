package com.example.test2;

import com.example.test2.exceptions.ClientException;

import java.io.IOException;

public class FTPUtil {
    private static String hostname;
    private static int port = 21;// 端口号默认是21
    private static boolean connect;
    private static String username;
    private static String password;
    private static FTPClient ftpClient;

    public static void init(String hostname1, int port1, String username1, String password1) throws IOException, ClientException {
        hostname = hostname1;
        port = port1;
        username = username1;
        password = password1;
        if (ftpClient == null) {
            ftpClient = new FTPClient(hostname1, port1);
        }


    }

    public static FTPClient getFtpClient() {
        return ftpClient;
    }

}
