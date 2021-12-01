package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.WrongCommandTypeException;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.ArgumentWrongResponse;
import com.example.ftpsever.response.CommandSendSuccessResponse;
import com.example.ftpsever.response.LoginFailedResponse;

import java.io.IOException;

public class PORTCommand extends AbstractCommand {

    public PORTCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {
        //检测用户有无登录，如果没有登录的话就直接显示没有登录
        if (!handleUserRequestThread.isLoginSuccessful()) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        //如果用户传来的东西不含参数......
        if (commandArg==null){
            handleUserRequestThread.writeLine(new ArgumentWrongResponse().toString());
            return;
        }

        //先检测用户传来的参数是不是符合条件，如果不符合条件则直接显示无法解析这个命令
        String pattern = "([0-9]+,){5}[0-9]+";
        if (!commandArg.matches(pattern)) {
            handleUserRequestThread.writeLine(new WrongCommandTypeException(commandType + " " + commandArg).toString());
            return;
        }

        //如果用户的命令格式和参数符合条件的话，开始解析用户的命令
        String[] lineArr = commandArg.split(",");
        //前四个是用户的ip地址
        String clientIPAddress = lineArr[0] + "." + lineArr[1] + "." + lineArr[2] + "." + lineArr[3];
        //后两个是端口
        int clientPort = Integer.parseInt(lineArr[4]) * 256 + Integer.parseInt(lineArr[5]);

        //在thread上设置用户的ip地址及端口！
        handleUserRequestThread.setClientIPAddress(clientIPAddress);
        handleUserRequestThread.setClientPort(clientPort);

        handleUserRequestThread.setPassiveActive(UserHandlerThread.PassiveActive.ACTIVE);


        //给用户写入执行成功的响应
        handleUserRequestThread.writeLine(new CommandSendSuccessResponse().toString());
    }
}
