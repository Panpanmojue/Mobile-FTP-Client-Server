package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.ConnectionClosedResponse;

import java.io.IOException;

public class QUITCommand extends AbstractCommand {
    public QUITCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {
        //退出指令，写个221 服务器关闭控制连接给用户，关闭掉和这个用户所有的socket
        System.out.println("prepare to quit!");
        System.out.println("quit number:"+new ConnectionClosedResponse().toString());
        handleUserRequestThread.writeLine(new ConnectionClosedResponse().toString());
        handleUserRequestThread.closeAllConnections();
        System.out.println("quit!");
        throw new IOException();//抛出异常表示退出，连接已关闭
    }
}
