package com.example.ftpsever;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * 服务端
 *
 */
public class SocketServer{

    private final int listenPort;//FTP服务器的监听接口

    private final String rootPath;//FTP服务器的根目录

    private final ServerSocket serverSocket; //用来监听的ServerSocket

    private final Thread listeningThread;



    public SocketServer(int listenPort, String rootPath) throws IOException {
        this.listenPort = listenPort;
        this.rootPath = rootPath;


        //检查rootPath是否真的是一个目录，如果不是就抛出异常给用户
        if (!new File(rootPath).isDirectory()) {
            System.out.printf(rootPath+" is an invalid root path! 路径无效");
        }
        else {
            System.out.println("the root "+rootPath+" is valid ! 路径有效");
        }

        System.out.println("trying to open port!");
        //在要监听的端口上打开一个ServerSocket
        serverSocket = new ServerSocket(listenPort);

        System.out.println("open port successfully!");

        //主线程对象
        listeningThread = new ListeningThread(serverSocket, rootPath);
    }

    public void start() {
        //开始运行主线程，注意不要run
        System.out.println("try to start!");
        File file= Environment.getRootDirectory();
        System.out.println("root"+file);
        listeningThread.start();
    }



    /**
     * 启动服务监听，等待客户端连接
     */
    public void startService() {
        try {
            // 创建ServerSocket
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("--开启服务器，监听端口 9999--");

            // 监听端口，等待客户端连接
            while (true) {
                System.out.println("--等待客户端连接--");
                Socket socket = serverSocket.accept(); //等待客户端连接
                System.out.println("得到客户端连接：" + socket);

                startReader(socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从参数的Socket里获取最新的消息
     */
    private static void startReader(final Socket socket) {
        new Thread(){
            @Override
            public void run() {
                DataInputStream reader;
                try {
                    // 获取读取流
                    reader = new DataInputStream( socket.getInputStream());
                    while (true) {
                        System.out.println("*等待客户端输入*");
                        // 读取数据
                        String msg = reader.readUTF();
                        System.out.println("获取到客户端的信息：" + msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
