package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.AbstractResponse;
import com.example.ftpsever.response.EnterPassModeResponse;
import com.example.ftpsever.response.LoginFailedResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;

public class PASVCommand extends AbstractCommand {
    public PASVCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {

        //如果没有成功登录，就写入没有登录成功的响应
        if (!handleUserRequestThread.isLoginSuccessful()) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        SecureRandom random = new SecureRandom();

        boolean openServerSocketSuccess = false;

        //尝试随机在服务器上打开一个ServerSocket
        do {
            //被动模式，随机在服务器上的一个端口打开一个监听
            //先随机生成两个0~255的整数
            int p1 = random.nextInt(256);
            int p2 = random.nextInt(256);

            //获得端口
            int port = p1 * 256 + p2;


            //设置ServerSocket监听用户的连接
            System.out.println("port:"+port);
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(serverSocket.getLocalPort());
            } catch (IOException e) {//由于可能端口冲突之类的问题，打开ServerSocket可能会抛出异常，因此如果抛出了异常就再试试一次
                continue;
            }

            //设置用来跳出循环的变量
            openServerSocketSuccess = true;

            //获取本机的地址

            //返回给用户的response
            //IP地址
            AbstractResponse rsp = new EnterPassModeResponse("10.13.177.83", p1, p2);

            //如果原来有打开被动模式的数据连接监听端口，就把他关掉
            if (handleUserRequestThread.getPassiveModeServerSocket() != null) {
                handleUserRequestThread.getPassiveModeServerSocket().close();
            }

            //将传输模式设置成被动模式，并将serverSocket传给线程对象
            handleUserRequestThread.setPassiveActive(UserHandlerThread.PassiveActive.PASSIVE);
            handleUserRequestThread.setPassiveModeServerSocket(serverSocket);

            System.out.println(serverSocket.getLocalPort());

            System.out.println(serverSocket.getLocalSocketAddress());

            System.out.println("rsp:"+rsp);

            //把请求写给用户
            handleUserRequestThread.writeLine(rsp.toString());

        } while (!openServerSocketSuccess);
    }
}
