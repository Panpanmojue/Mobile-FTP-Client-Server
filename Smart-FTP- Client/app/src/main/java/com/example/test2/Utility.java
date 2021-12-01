package com.example.test2;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Utility {


    /**
     * 用于生成每个文件在客户端上存放的位置
     * @param downloadDirectory 在客户端上下载的总地址
     * @param serverFileList
     * @param folderName
     * @param path
     * @return
     */
    public static List<String> generateClientPath(String downloadDirectory, List<String> serverFileList, String folderName, String path ) {
        List<String> clientFileList = new ArrayList<>();
        for (String name : serverFileList) {
            String clientName = downloadDirectory + "/" + folderName + "/" + name;
            clientFileList.add(clientName);
        }
        return clientFileList;
    }

    /**
     * 用于将一个String的List转换为FTPFile的List
     * @param fileNameList 服务器端返回的List，偶数号是文件的总路径，奇数号是代表文件类型的String(file/directory)
     * @return 根据文件路径生成FTPFile
     */
    public static List<FTPFile> stringToFTPFile(List<String> fileNameList) {
        List<FTPFile> ftpFiles = new ArrayList<>();
        for (int i = 0; i < fileNameList.size(); i = i+2) {
            FTPFile ftpFile = new FTPFile(fileNameList.get(i));
            Log.d("debug1", fileNameList.get(i));
            String type = fileNameList.get(i + 1);
            Log.d("debug1", type);
            if (type.equals("file")) {
                ftpFile.setType(FTPFile.Type.File);
            }
            else{
                ftpFile.setType(FTPFile.Type.Directory);
            }
            ftpFiles.add(ftpFile);

        }
        return ftpFiles;
    }

}
