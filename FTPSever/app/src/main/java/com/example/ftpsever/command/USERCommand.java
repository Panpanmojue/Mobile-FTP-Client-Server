package com.example.ftpsever.command;



import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.LoginSuccessResponse;
import com.example.ftpsever.response.RequirePasswordResponse;

import java.io.IOException;
import java.util.Map;

public class USERCommand extends AbstractCommand {

    public USERCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread userHandlerThread) throws IOException {

        userHandlerThread.setLoginSuccessful(false);

        //参数名就是用户名！
        String username = commandArg;

        //设置登录用的用户名
        userHandlerThread.setUsername(username);

        userHandlerThread.setUsername(username);

        System.out.println("get USER execute!");
        //获得用户名到密码的对象
        Map<String, String> usernameToPassword = userHandlerThread.usernameToPassword;

        //如果匿名用户，就直接登录成功
        if (usernameToPassword.containsKey(username) && usernameToPassword.get(username) == null) {

            userHandlerThread.writeLine(new LoginSuccessResponse().toString());
            userHandlerThread.setLoginSuccessful(true);
            System.out.println("登陆成功！");

        } else {//否则则要求输入密码
            userHandlerThread.writeLine(new RequirePasswordResponse().toString());
            System.out.println("正在请求密码，返回"+new RequirePasswordResponse().toString());
        }
    }
}
