package com.example.ftpsever.command;

import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.FileMeta;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.CommandSendSuccessResponse;
import com.example.ftpsever.response.ConnectionAlreadyOpenResponse;
import com.example.ftpsever.response.LoginFailedResponse;
import com.example.ftpsever.response.OpenDataConnectionFailedResponse;
import com.example.ftpsever.response.TransferFailedResponse;
import com.example.ftpsever.response.TransferSuccessResponse;
import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RETRCommand extends AbstractCommand {
    public RETRCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {

        //处理未登录的问题
        if (!handleUserRequestThread.isLoginSuccessful()) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        System.out.println("get here!");

        //处理没有指定PASV还是PORT的问题
        //if (handleUserRequestThread.getPassiveActive() == null) {
            //handleUserRequestThread.writeLine(new BadCommandSequenceResponse().toString());
            //return;
        //}

        //根据服务器的根目录，获得文件的绝对路径
        String fileAbsolutePath = new File(handleUserRequestThread.getRootPath() + File.separator + commandArg).getAbsolutePath();
        //改回来
        //String fileAbsolutePath = new File("/system/"+"testCrop").getAbsolutePath();
        File file = new File(fileAbsolutePath);

        //处理找不到文件，或是应该是个文件，而实际是个目录的问题
        /*if (!file.exists()) {
            handleUserRequestThread.writeLine(new ArgumentWrongResponse("找不到文件！").toString());
            System.out.println("file not found!");
            return;
        } else if (file.isDirectory()) {
            handleUserRequestThread.writeLine(new ArgumentWrongResponse("RETR的参数必须是一个文件，而不能是一个目录。如果你需要下载目录，应该先调用LFFR指令获得目录下所有的文件名，再由客户端循环调用RETR获取每个文件").toString());
            System.out.println("not a file!");
            return;
        }*/

        System.out.println("file found!");

        //处理ASCII模式和KeepAlive=T的不兼容问题
        //if (handleUserRequestThread.getAsciiBinary() == UserHandlerThread.ASCIIBinary.ASCII && handleUserRequestThread.getKeepAlive().equals("T")) {
            //handleUserRequestThread.writeLine(new ArgumentWrongResponse("ASCII模式与持久数据连接不兼容").toString());
            //return;
        //}

        System.out.println("ascill set!");

        //写给一个用户命令OK的指令
        handleUserRequestThread.writeLine(new CommandSendSuccessResponse().toString());

        //准备建立数据连接，先看看是不是keep-alive，如果是的话就不用建立连接了。
        if (
                "F".equals(handleUserRequestThread.getKeepAlive()) ||
                        ("T".equals(handleUserRequestThread.getKeepAlive())
                                && handleUserRequestThread.getDataSockets().size() == 0))
        {
            boolean success = handleUserRequestThread.buildDataConnection();
            if (success) {
                System.out.println("ConnectionAlreadyOpenResponse");
                handleUserRequestThread.writeLine(new ConnectionAlreadyOpenResponse().toString());
            } else {
                System.out.println("OpenDataConnectionFailedResponse");
                handleUserRequestThread.writeLine(new OpenDataConnectionFailedResponse().toString());
                return;
            }
        } else {
            System.out.println("ConnectionAlreadyOpenResponse");
            handleUserRequestThread.writeLine(new ConnectionAlreadyOpenResponse().toString());
        }

        //boolean success = handleUserRequestThread.buildDataConnection();

        System.out.println("start to retrieve!");

        //准备写数据！
        if (handleUserRequestThread.getAsciiBinary() == UserHandlerThread.ASCIIBinary.ASCII) {
            //ASCII模式，强制非持久化连接！
            System.out.println("start to write!!");

            try {
                System.out.println("ascii start!");
                //开Scanner
                //Scanner scanner = new Scanner(new File(fileAbsolutePath));
                OutputStream out = handleUserRequestThread.getDataSockets().get(0).getOutputStream();
                /*while (scanner.hasNext()) {//逐行输出
                    out.write(scanner.nextLine().getBytes(StandardCharsets.UTF_8));
                    out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }*/

                File fileAscii=new File(fileAbsolutePath);
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(out));
                BufferedReader bufferedReader=new BufferedReader(new FileReader(fileAscii));

                int count=0;
                while(true){
                    String line1=bufferedReader.readLine();
                    if(line1==null){
                        break;
                    }
                    if(count>0){
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.write(line1);
                }

                bufferedWriter.close();


                //写传输成功
                handleUserRequestThread.writeLine(new TransferSuccessResponse().toString());
            } catch (IOException e) {

                handleUserRequestThread.writeLine(new TransferFailedResponse().toString());
            } finally {
                //无论如何关闭连接
                handleUserRequestThread.getDataSockets().get(0).close();
                handleUserRequestThread.getDataSockets().clear();
            }
        }
        else {//BINARY模式
            System.out.println("binary start!");

            // 创建对应的FileMeta对象
            FileMeta fileMeta = new FileMeta(file.length(), commandArg, FileMeta.Compressed.NOT_COMPRESSED);

            System.out.println("file meta created");


            try {
                System.out.println("start to use fileMeta!");
                OutputStream outputStream = handleUserRequestThread.getDataSockets().get(0).getOutputStream();

                System.out.println("start to write fileMeta!");

                //在数据连接上写FileMeta
                outputStream.write((JSON.toJSONString(fileMeta) + "\r\n").getBytes(StandardCharsets.UTF_8));

                System.out.println("start to read CD!");

                //读取服务器硬盘上文件的缓冲字节流
                System.out.println(fileAbsolutePath);
                BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(fileAbsolutePath));
                System.out.println("Buffer created!!");
                //防止内存爆掉，每次只从带缓冲的字节流中读取1MB文件
                byte[] buf = new byte[1024 * 1024];
                while(true) {
                    //把文件读取到buf，记录读了多少字节
                    System.out.println("is looping!");
                    int bytesRead = fileIn.read(buf);
                    if (bytesRead == -1) {
                        //如果是-1，说明流结束了
                        break;
                    }

                    //写这部分的文件到输出流上
                    System.out.println("write end!start to send!");
                    outputStream.write(buf, 0, bytesRead);
                    outputStream.flush();
                    System.out.println("retr success!");
                }


                //写传输成功
                handleUserRequestThread.writeLine(new TransferSuccessResponse().toString());

                System.out.println("transfer success!");

                //根据是不是keep-alive决定要不要关闭数据连接
                if ("F".equals(handleUserRequestThread.getKeepAlive())) {
                    try {
                        handleUserRequestThread.getDataSockets().get(0).close();
                        handleUserRequestThread.getDataSockets().clear();
                    } catch (IOException ignored) {
                    }
                }
            } catch (IOException e) {

                handleUserRequestThread.writeLine(new TransferFailedResponse().toString());
                //如果传输失败，无论如何关闭数据连接
                handleUserRequestThread.getDataSockets().get(0).close();
                handleUserRequestThread.getDataSockets().clear();
            }

        }


    }


}

