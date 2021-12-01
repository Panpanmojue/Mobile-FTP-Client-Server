package com.example.test2;

import com.example.test2.exceptions.ClientException;

import java.io.IOException;
import java.util.List;

public class FTPFile {
    private Type type;
    private String name;
    private String path;
    private String fileName;

    public enum Type {
        File,
        Directory
    }

    public FTPFile(String path, String name) {
        this.name  =name;
        this.path = path;
        this.fileName = path + name;
    }

    public FTPFile(String fileName) {
        this.fileName = fileName;
        String[] strings = fileName.split("/");
        String name = strings[strings.length - 1];
        this.name = name;
        String path = fileName.replace(name, "");
        this.path = path;
    }

    public boolean isDirectory() {
        return this.type == Type.Directory;
    }

    public boolean isFile() {
        return this.type == Type.File;
    }

    public FTPFile[] listFile() throws IOException, ClientException {
        if (this.type == Type.File) {
            return null;
        }
        else {
            List<String> subFiles = FTPUtil.getFtpClient().list(fileName);
            int length = subFiles.size() / 2;
            FTPFile[] result = new FTPFile[length];
            for (int i = 0; i < subFiles.size(); i = i+2) {
                result[i] = new FTPFile(path + name + "/", subFiles.get(i));
            }

            for (int j = 0; j < subFiles.size(); j = j+2) {
                String[] strings = subFiles.get(j).split("/");
                String newName = strings[strings.length - 1];
                result[j] = new FTPFile(fileName+"/", newName);
            }
            return result;
        }
    }

    public FTPFile getParentFile() {
        String[] temp = path.split("/");
        String newName = temp[temp.length - 1];
        String newPath = path.replace(newName , "");
        FTPFile result = new FTPFile(newPath, newName);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
