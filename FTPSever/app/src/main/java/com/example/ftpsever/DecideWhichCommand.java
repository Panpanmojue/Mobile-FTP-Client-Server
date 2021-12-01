package com.example.ftpsever;


import java.lang.reflect.Constructor;

public class DecideWhichCommand {

    public static AbstractCommand parseCommand(String commandLine) throws WrongCommandTypeException {


        int firstSpaceIndex = commandLine.indexOf(" ");

        String commandType;
        String commandArg = null;

        //例如NOOP,PASV之类的命令是没有参数的，将有参数和无参数的命令分开处理
        if (firstSpaceIndex == -1) {
            commandType = commandLine.trim();
        } else {
            commandType = commandLine.substring(0, firstSpaceIndex);
            commandArg = commandLine.substring(firstSpaceIndex + 1).trim();
        }


        //获取对应的命令的具体类对象
        Class<?> clazz;
        Constructor<?> constructor;

        try {

            clazz = Class.forName(String.format("com.example.ftpsever.command.%sCommand", commandType));

            //获得构造函数，第一个是命令类型，第二个是命令参数
            constructor = clazz.getConstructor(String.class, String.class);

            System.out.println(constructor.newInstance(commandType, commandArg));

            //用构造函数创建对象并返回
            return (AbstractCommand) constructor.newInstance(commandType, commandArg);




        } catch (Throwable e) {
            throw new WrongCommandTypeException(commandLine);
        }
    }
}

