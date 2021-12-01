package com.example.ftpsever;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Utils {
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
