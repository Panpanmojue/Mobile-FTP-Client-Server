package com.example.ftpsever.command;



import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.ArgumentWrongResponse;
import com.example.ftpsever.response.CommandSendSuccessResponse;
import com.example.ftpsever.response.LoginFailedResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自己加的一个LIST命令，用于列出服务器上一个文件夹中所有的文件，然后发送给客户端
 * LIST = List Files in a Folder
 */
public class LISTCommand extends AbstractCommand {

    private final List<String> filenameList = new ArrayList<>();
    private final List<String> isADirectory = new ArrayList<>();


    public LISTCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {
        //检查用户有没有登录
        if (!handleUserRequestThread.isLoginSuccessful()) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        System.out.println("start to list file!");

        //根目录对应的文件夹
        File rootPathFile = new File(handleUserRequestThread.getRootPath());
        System.out.println(handleUserRequestThread.getRootPath() + File.separator + commandArg);
        //要列出所有文件的文件夹的File对象
        File folderFile = new File(handleUserRequestThread.getRootPath() + File.separator + commandArg);

        //判断是否存在，是文件夹，如果不是就返回给用户错误信息
        if (!folderFile.exists() || !folderFile.isDirectory()) {
            System.out.println("目标“文件夹”不存在或不是一个文件夹");
            handleUserRequestThread.writeLine(new ArgumentWrongResponse("目标“文件夹”不存在或不是一个文件夹").toString());
            return;
        }

        //否则，则递归获取这个文件夹下的全部文件名
        filenameList.clear();
        isADirectory.clear();
        backTrack(folderFile);

        //先给用户一个200 CommandOK
        handleUserRequestThread.writeLine(new CommandSendSuccessResponse().toString());

        //将每个文件的文件名去除掉根目录的前缀后，写给客户端
        for (int i = 0; i < filenameList.size(); i++) {
            String filename = filenameList.get(i);
            String directoryOrFile=isADirectory.get(i);
            //去掉绝对目录文件名的根目录前缀
            String filenameWithoutPrefix = new StringBuilder(filename).delete(0, rootPathFile.getAbsolutePath().length()).toString();

            //把反斜杠换成正斜杠
            filenameWithoutPrefix = filenameWithoutPrefix.replace("\\", "/");

            System.out.println("filenameWithoutPrefix:"+filenameWithoutPrefix);
            System.out.println("directoryOrFile:"+directoryOrFile);

            //写给客户
            handleUserRequestThread.writeLine(filenameWithoutPrefix);
            handleUserRequestThread.writeLine(directoryOrFile);
        }

        //最后写一个<CRLF>，告诉用户结束了
        handleUserRequestThread.writeLine("");
    }

    /**
     * 递归遍历这个folder中的全部文件，并加入到类变量的filenameList中
     *
     * @param folder 要递归的根，即根文件夹！
     */
    private void backTrack(File folder) {
        File[] files = folder.listFiles();
        assert files != null;
        for (File f : files
        ) {
            if (f.isDirectory()) {
                //backTrack(f);
                //System.out.println("directory:"+f.toString());
                filenameList.add(f.toString());
                isADirectory.add("directory");
            } else {
                //System.out.println(f.getAbsolutePath());
                filenameList.add(f.getAbsolutePath());
                isADirectory.add("file");
            }
        }
    }
}

