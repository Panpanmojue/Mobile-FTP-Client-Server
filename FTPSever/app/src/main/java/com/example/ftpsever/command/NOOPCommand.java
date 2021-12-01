package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.CommandSendSuccessResponse;

import java.io.IOException;

public class NOOPCommand extends AbstractCommand {

    public NOOPCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {
        //NOOP命令，啥都不做，直接返回给用户一个200 命令OK就行了
        handleUserRequestThread.writeLine(new CommandSendSuccessResponse().toString());
    }
}
