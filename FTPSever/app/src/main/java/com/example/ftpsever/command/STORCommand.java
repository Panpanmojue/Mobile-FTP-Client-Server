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
            if (handleUserRequestThread.getAsciiBinary() == UserHandlerThread.ASCIIBinary.ASCII) {//ASCII??????

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
            else {//BINARY ??????

                System.out.println("enter store binary");

                String path="/storage/emulated/0/system/"+commandArg;

                System.out.println("path:"+path);

                Log.d("debug1", path);
                File file = new File(path);
                if(file.getParentFile().exists()) {
                    Log.d("debug1", file.getParentFile().getName()+"???????????????");
                }
                else {
                    file.mkdirs();
                    Log.d("debug1", "??????????????????");
                }
                if(!file.exists()){
                    Log.d("debug1", "????????????");
                    file.createNewFile();
                }else {
                    Log.d("debug1", "?????????????????????????????????");
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
                //?????????????????????
                fOut.close();

                System.out.println("binary transfer store success!");



                handleUserRequestThread.writeLine(new TransferSuccessResponse().toString());

            }
        }catch (IOException e){
            System.out.println("exception!");
            handleUserRequestThread.writeLine(new TransferFailedResponse().toString());
            //???????????????????????????????????????????????????
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
     * ???????????????????????????LFFR??????????????????????????????????????????????????????????????????????????????????????????????????????????????????RETR????????????????????????????????????????????????????????????????????????
     *
     * @param downloadDirectory  ????????????
     * @param serverFilenameList ??????????????????????????????
     * @param folderName         ??????????????????????????????
     * @return ???????????????????????????????????????????????????
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
     * ???strArr?????????prefix??????????????????list??????
     *
     * @param prefix ????????????
     * @param strArr ???????????????????????????
     * @return ???????????????????????????list????????????
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
