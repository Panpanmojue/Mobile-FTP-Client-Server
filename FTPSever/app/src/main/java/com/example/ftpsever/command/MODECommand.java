package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.CommandSendSuccessResponse;

import java.io.IOException;

public class MODECommand extends AbstractCommand {

    public MODECommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread userHandlerThread) throws IOException {
        System.out.println("Mode:send 200!");
        userHandlerThread.writeLine(new CommandSendSuccessResponse().toString());//此处就是返回200,即命令成功
    }
}
