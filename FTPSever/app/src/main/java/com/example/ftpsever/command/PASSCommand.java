package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.LoginSuccessResponse;
import com.example.ftpsever.response.LoginFailedResponse;

import java.io.IOException;
import java.util.Map;

public class PASSCommand extends AbstractCommand {

    public PASSCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {
        String password = commandArg;
        String username = handleUserRequestThread.getUsername();
        Map<String, String> usernameToPassword = handleUserRequestThread.usernameToPassword;

        System.out.println("enter PASS");

        //如果username为null或者密码不匹配，则登录失败
        if (username == null || !usernameToPassword.containsKey(username) || !usernameToPassword.get(username).equals(password)) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
        } else {
            //否则就登录成功
            handleUserRequestThread.writeLine(new LoginSuccessResponse().toString());
            handleUserRequestThread.setLoginSuccessful(true);
        }
        System.out.println("end!");
    }
}
