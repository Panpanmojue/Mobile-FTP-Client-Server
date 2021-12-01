package com.example.ftpsever.command;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.example.ftpsever.AbstractCommand;
import com.example.ftpsever.FileMeta;
import com.example.ftpsever.UserHandlerThread;
import com.example.ftpsever.response.CommandSendSuccessResponse;
import com.example.ftpsever.response.ConnectionAlreadyOpenResponse;
import com.example.ftpsever.response.LoginFailedResponse;
import com.example.ftpsever.response.OpenDataConnectionFailedResponse;
import com.example.ftpsever.response.TransferFailedResponse;
import com.example.ftpsever.response.TransferSuccessResponse;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class STORCommand extends AbstractCommand {

    public STORCommand(String commandType, String commandArg) {
        super(commandType, commandArg);
    }

    @Override
    public void execute(UserHandlerThread handleUserRequestThread) throws IOException {


        if (!handleUserRequestThread.isLoginSuccessful()) {
            handleUserRequestThread.writeLine(new LoginFailedResponse().toString());
            return;
        }

        System.out.println("get here!");

        System.out.println("build finished!");

        System.out.println("start to store!");

        handleUserRequestThread.writeLine(new CommandSendSuccessResponse().toString());

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

        try {
            if (handleUserRequestThread.getAsciiBinary() == UserHandlerThread.ASCIIBinary.ASCII) {//ASCII模式

                System.out.println("enter store Ascii");

                System.out.println("handleUserRequestThread.getCommandConnWriter():"+handleUserRequestThread.getCommandConnWriter().toString());


                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/storage/emulated/0/system/"+commandArg)));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(handleUserRequestThread.getDataSockets().get(0).getInputStream()));
                int lineRead = 0;

                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (lineRead > 0) {
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.write(line);
                    lineRead++;
                }

                bufferedWriter.close();
                handleUserRequestThread.writeLine(new TransferSuccessResponse().toString());
                System.out.println("ascii transfer store success!");
            }
            else {//BINARY 模式

                System.out.println("enter store binary");

                String path="/storage/emulated/0/system/"+commandArg;

                System.out.println("path:"+path);

                Log.d("debug1", path);
                File file = new File(path);
                if(file.getParentFile().exists()) {
                    Log.d("debug1", file.getParentFile().getName()+"根目录存在");
                }
                else {
                    file.mkdirs();
                    Log.d("debug1", "根目录不存在");
                }
                if(!file.exists()){
                    Log.d("debug1", "想要新建");
                    file.createNewFile();
                }else {
                    Log.d("debug1", "文件已经存在，即将替换");
                    file.delete();
                    file.createNewFile();
                }

                BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream("/storage/emulated/0/system/"+commandArg));

                System.out.println("buffer created!");

                byte[] buf = new byte[1024 * 1024];

                //new File("/storage/emulated/0/system/"+commandArg);

                System.out.println("open file!");

                InputStream in = handleUserRequestThread.getDataSockets().get(0).getInputStream();

                System.out.println("open inputStream");

                //FileMeta fileMeta = JSON.parseObject(Utils.readline(in), FileMeta.class);

                int totalBytesRead = 0;

                while ((totalBytesRead=in.read(buf))!=-1) {

                    if(totalBytesRead==1024*1024){
                        fOut.write(buf);
                        fOut.flush();
                        buf=new byte[1024*1024];
                    }

                    else{
                        byte[] result=new byte[totalBytesRead];
                        System.arraycopy(buf,0,result,0,totalBytesRead);
                        fOut.write(result);
                        fOut.flush();
                        buf=new byte[1024*1024];
                    }
                }
                //关闭文件输出流
                fOut.close();

                System.out.println("binary transfer store success!");



                handleUserRequestThread.writeLine(new TransferSuccessResponse().toString());

            }
        }catch (IOException e){
            System.out.println("exception!");
            handleUserRequestThread.writeLine(new TransferFailedResponse().toString());
            //如果传输失败，无论如何关闭数据连接
            handleUserRequestThread.getDataSockets().get(0).close();
            handleUserRequestThread.getDataSockets().clear();
        }
    }
}

class Utils {
    public static String readline(InputStream in) throws IOException {
        List<Byte> bytesList = new ArrayList<>();
        while (true) {
            byte b = (byte) in.read();
            if (b == '\n') {
                break;
            }
            bytesList.add(b);
        }
        if (bytesList.get(bytesList.size() - 1) == '\r') {
            bytesList.remove(bytesList.size() - 1);
        }

        byte[] byteArr = new byte[bytesList.size()];
        for (int i = 0; i < byteArr.length; i++) {
            byteArr[i] = bytesList.get(i);
        }

        String line = new String(byteArr);
        return line;
    }

    /**
     * 在下载文件时，根据LFFR指令获得的服务器上文件名列表，再加上要下载到哪个目录，和要下载的文件夹名，和RETR指令的参数，给出客户机上每个下载文件的绝对路径！
     *
     * @param downloadDirectory  下载目录
     * @param serverFilenameList 服务器上文件名的列表
     * @param folderName         要下载的文件夹的名字
     * @return 客户机上每个下载文件存储的绝对路径
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<String> getClientFilenames(String downloadDirectory, List<String> serverFilenameList, String folderName, String retrArg) {
        List<String> clientFilenameList = new ArrayList<>();
        for (String serverFilename : serverFilenameList
        ) {
            String clientFilename =
                    downloadDirectory +
                            File.separator +
                            folderName +
                            File.separator +
                            String.join(File.separator, removePrefix(retrArg.split("[/]|[\\\\]"), serverFilename.split("[/]|[\\\\]")));
            clientFilenameList.add(clientFilename);

        }
        return clientFilenameList;
    }

    /**
     * 给strArr移除掉prefix的前缀，作为list返回
     *
     * @param prefix 前缀数组
     * @param strArr 要操作的字符串数组
     * @return 去除前缀完成后，以list形式返回
     */
    private static List<String> removePrefix(String[] prefix, String[] strArr) {
        int i = 0;
        int j = 0;
        while (i < prefix.length && j < strArr.length) {
            if (prefix[i].length() == 0) {
                i++;
                continue;
            }
            if (strArr[j].length() == 0) {
                j++;
                continue;
            }
            if (Objects.equals(prefix[i], strArr[j])) {
                i++;
                j++;
            } else {
                break;
            }
        }
        List<String> res = new ArrayList<>();
        while (j < strArr.length) {
            res.add(strArr[j]);
            j++;
        }
        return res;
    }

    public static void createFolders(List<String> clientFilenameList) {
        HashSet<String> folderNames = new HashSet<>();
        for (String clientFilename : clientFilenameList
        ) {
            int i = clientFilename.lastIndexOf(File.separator);
            folderNames.add(clientFilename.substring(0, i));
        }
        for (String folderName : folderNames
        ) {
            boolean success = new File(folderName).mkdirs();
        }
    }
}
