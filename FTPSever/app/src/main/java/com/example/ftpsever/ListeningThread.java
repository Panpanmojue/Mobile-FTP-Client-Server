package com.example.ftpsever;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 这是用来无限循环（监听）并accept用户的连接的线程
 */
public class ListeningThread extends Thread {

    private final ServerSocket serverSocket;
    //用来监听的ServerSocket
    private final String rootPath;
    //ftp服务器的根目录

    /**
     * 传入一个ServerSocket(在这个套接字上监听并accept用户的连接请求)
     *
     * @param serverSocket 监听并accept用户连接请求的socket
     */
    public ListeningThread(ServerSocket serverSocket, String rootPath) {
        this.serverSocket = serverSocket;
        this.rootPath = rootPath;
    }

    @Override
    public void run() {
        System.out.println("thread is running! ");
        //无线循环，监听用户的连接
        while (true) {
            Socket clientCommandSocket;
            try {

                //accept用户的连接请求，并将得到的socket作为这个用户的控制连接！
                //这里可以连接到
                System.out.println(serverSocket.getInetAddress());
                System.out.println(serverSocket.getLocalSocketAddress());
                clientCommandSocket = serverSocket.accept();

                System.out.println("listening thread is running!");
                //创建处理用户请求的线程
                Thread handleUserRequestThread = new UserHandlerThread(clientCommandSocket, rootPath);

                System.out.println("listening thread is running!");

                Log.d("running:","listening thread is preparing to run!");
                //线程开始运行
                handleUserRequestThread.start();
            } catch (IOException ignored) {
                System.out.println("listening thread exception!");
                Log.e("error:","listening thread exception!");
            }
        }
    }
}

