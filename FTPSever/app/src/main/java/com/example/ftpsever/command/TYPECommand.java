package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;

import com.example.ftpsever.WrongCommandTypeException;
import com.example.ftpsever.response.CommandSendSuccessResponse;
import com.example.ftpsever.response.LoginFailedResponse;
import com.example.ftpsever.UserHandlerThread;

import java.io.IOException;
import java.util.Objects;

public class TYPECommand extends AbstractCommand {
    public TYPECommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread userHandlerThread) throws IOException {

        //检查用户是否有登录，如果没有登录则给出没有登录的提醒
        if (!userHandlerThread.isLoginSuccessful()) {
            userHandlerThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        //判断参数是否符合条件，如果不符合条件就直接给用户无法解析命令的提示
        if (!Objects.equals(commandArg, "A") && !Objects.equals(commandArg, "B")) {
            userHandlerThread.writeLine(new WrongCommandTypeException(commandType + " " + commandArg).toString());
            return;
        }

        //在thread中设置是ASCII还是binary传输
        if (Objects.equals(commandArg, "A")) {
            userHandlerThread.setAsciiBinary(UserHandlerThread.ASCIIBinary.ASCII);
        } else if (Objects.equals(commandArg, "B")) {
            userHandlerThread.setAsciiBinary(UserHandlerThread.ASCIIBinary.BINARY);
        }

        //把成功的信息写给用户
        userHandlerThread.writeLine(new CommandSendSuccessResponse().toString());
    }
}
