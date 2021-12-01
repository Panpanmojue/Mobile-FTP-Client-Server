package com.example.test2;

import android.os.Environment;
import android.util.Log;

import com.example.test2.exceptions.ClientException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FTPClient {
    private Socket commandSocket;
    private BufferedWriter commandSocketWriter;
    private BufferedReader commandSocketReader;
    private boolean isConnected = false;
    private volatile String address;// 服务器的地址 被动模式时RETR或者STOR需要连接
    private volatile int port;// 服务器端口 被动模式时RETR或者STOR需要连接
    private String downloadDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FTPDownload/";
    private volatile Socket dataSocket;// 用来进行数据连接的Socket
    private boolean keepConnected = false; // 是否保持数据连接


    public enum Type {
        A,  // A means Ascii
        B   // B means Binary
    }
    private volatile Type type = Type.B; // 默认为Binary模式

    public enum PassiveOrActive {
        PASSIVE,    // 被动模式
        ACTIVE  // 主动模式
    }
    // 默认为被动模式
    private volatile PassiveOrActive passiveOrActive = PassiveOrActive.PASSIVE;
    private volatile ServerSocket serverSocket;// 主动连接的时，客户端要在这个socket上监听并accept


    public enum Mode {
        S,  //Stream（流，默认值）
        B,  //Block（块）
        C   //Compressed（经过压缩）
    }
    // 默认为Stream模式
    private volatile Mode mode = Mode.S;

    //指定传达数据的结构类型
    public enum Stru {
        F,  //文件结构（默认值） File
        R,  //记录结构  Record
        P   //页结构   Page
    }
    //默认为文件结构
    private volatile Stru stru = Stru.F;



    public FTPClient() {}

    public FTPClient(String host, int port) throws IOException {
        try {
            commandSocket = new Socket(host, port);
            commandSocketReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
            commandSocketWriter = new BufferedWriter(new OutputStreamWriter(commandSocket.getOutputStream()));
            //isConnected = true;
            this.isConnected = true;
        } catch (IOException e) {
            this.isConnected = false;
            throw new ConnectException();
        }


    }

    public void connect(String host, int port) throws ConnectException {
        try {
            commandSocket = new Socket(host, port);
            commandSocketReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
            commandSocketWriter = new BufferedWriter(new OutputStreamWriter(commandSocket.getOutputStream()));
            //isConnected = true;
            this.isConnected = true;
        } catch (IOException e) {
            this.isConnected = false;
            throw new ConnectException();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 实现用户登陆
     * @param username 用户输入的用户名
     * @param password 用户输入的密码
     * @return 返回用户是否登陆成功
     * @throws IOException
     * @throws ClientException
     */
    public boolean login(String username, String password) throws IOException, ClientException {
        // 写USER指令 USER <SP> <username> <CRLF>
        writeCommand(String.format("USER %s", username));
        // 读取响应
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        // 状态码是230，说明该用户是没有密码的，登陆成功
        if (response.startsWith("230")) {
//            Log.d("debug1", "登场成功向服务器发送PASV命令");
//            writeCommand("PASV");
//            response = commandSocketReader.readLine();
//            Log.d("debug1", response);

            return true;

        }
        // 用户名正确，需要输入密码
        else if (response.startsWith("331")) {
            // 写密码指令 PASS <SP> <password> <CRLF>
            writeCommand(String.format("PASS %s", password));
            response = commandSocketReader.readLine();
            Log.d("debug1", response);
            // 状态码是230就是登陆成功
            if (response.startsWith("230")) {
//                Log.d("debug1", "登场成功向服务器发送PASV命令");
//                writeCommand("PASV");
//                response = commandSocketReader.readLine();
//                Log.d("debug1", response);
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * 向服务器发送 TYPE 指令
     * @param type
     * @throws IOException
     * @throws ClientException
     */
    public synchronized void setType(Type type) throws IOException, ClientException {
        String arg = (type == Type.A)? "A" : "B";
        String line = String.format("TYPE %s", arg);
        Log.d("debug1", "向服务器发送TYPE请求");
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", "type"+response);
        if (response != null && response.startsWith("200")) {
            this.type = type;
        }
        else {
            throw new ClientException(response);
        }


    }

    /**
     * 向服务器发送 Mode 指令
     * @param mode
     */
    public synchronized void setMode(Mode mode) throws IOException, ClientException {
        String arg = "";
        if (mode == Mode.S) {
            arg = "S";
        } else if (mode == Mode.B) {
            arg = "B";
        } else if (mode == Mode.C) {
            arg = "C";
        }
        String line = String.format("MODE %s", arg);
        Log.d("debug1", "向服务器发送MODE指令");
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response != null && response.startsWith("200")) {
            this.mode = mode;
        }
        else {
            throw new ClientException(response);
        }
    }

    /**
     * 向服务器发送 STRU 指令
     * @param stru
     * @throws IOException
     * @throws ClientException
     */
    public synchronized void setStru(Stru stru) throws IOException, ClientException {
        String arg = "";
        if (stru == Stru.F) {
            arg = "F";
        } else if (stru == Stru.P) {
            arg = "P";
        } else if (stru == Stru.R) {
            arg = "R";
        }
        String line = String.format("STRU %s", arg);
        Log.d("debug1", "向服务器发送STRU指令");
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response != null && response.startsWith("200")) {
            this.stru = stru;
        }
        else {
            throw new ClientException(response);
        }
    }

    /**
     * 向服务器发送QUIT指令
     * @return
     * @throws IOException
     */
    public boolean disconnect() throws IOException {
        Log.d("debug1", "向服务器发送QUIT指令");
        writeCommand("QUIT");
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response == null) {
            Log.d("debug1", "没有接收到服务器quit的返回");
        }
        if (response != null && response.startsWith("221")) {
            commandSocket.close();
            commandSocketReader.close();
            commandSocketWriter.close();
            dataSocket.close();
            isConnected = false;
            address = "";
            port = 0;
            return true;
        }
        return false;
    }

    /**
     * 向服务器发送 PASV <CRLF> 指令
     * 并读取到服务器返回的IP地址和端口号
     * @throws IOException
     */
    public synchronized void pasv() throws IOException, ClientException {
        Log.d("debug1", "向服务器发送PASV命令");
        writeCommand("PASV");
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        // 返回头是227说明成功
        if (response != null && response.startsWith("227")) {
            int start = response.indexOf("(");
            int end = response.indexOf(")");
            String addressPort = response.substring(start+1, end);
            String[] strings = addressPort.split(",");
            // 前四位是IP地址
            address = strings[0] + "." + strings[1] + "." + strings[2] + "." + strings[3];
            // 最后两位代表着端口号
            port = Integer.parseInt(strings[4]) * 256 + Integer.parseInt(strings[5]);
            passiveOrActive = PassiveOrActive.PASSIVE;
        }
        else {
            throw new ClientException(response);
        }
        Log.d("debug1", "address"+address);
        Log.d("debug1", "port"+port);

    }


    /**
     * 向服务器发送 PORT 命令 为数据连接指定一个IP地址和本地端口
     * @throws IOException
     */
    public synchronized void port() throws IOException {
        SecureRandom random = new SecureRandom();
        boolean success = false;

        do {
            // 随机生成端口号的参数p1, p2
            int p1 = random.nextInt(256);
            int p2 = random.nextInt(256);

            // 由p1, p2生成端口号
            int port = p1 * 256 + p2;

            //设置ServerSocket监听服务器端传来的连接
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                continue;
            }

            success = true;
            //获取本机的地址
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            String line = String.format("PORT %s,%d,%d", "192,168,43,230", p1, p2);
            Log.d("debug1", "向服务器发送PORT指令"+line);
            writeCommand(line);
            String response = commandSocketReader.readLine();
            Log.d("debug1", response);
            if (response != null && response.startsWith("200")) {
                // 如果成功的话，就将传输模式设置为主动模式
                passiveOrActive = PassiveOrActive.ACTIVE;
                this.serverSocket = serverSocket;

            }
        } while (!success);

    }

    /**
     *向服务器发送LIST指令，获取文件夹内的文件
     * @param folderName 想获取到到文件夹
     * @return 返回文件中子文件名的List
     * @throws IOException
     * @throws ClientException
     */
    public synchronized List<String> list(String folderName) throws IOException, ClientException {
        String line = String.format("LIST %s", folderName);
        Log.d("debug1", "向服务器发送LIST指令");
        Log.d("debug1", line);
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response == null || !response.startsWith("200")) {
            throw new ClientException(response);
        }
        List<String> filenameList = new ArrayList<>();

        while (true) {
            String filenameInfo = commandSocketReader.readLine();
            if (filenameInfo == null || filenameInfo.length() == 0) {
                break;
            }
            filenameList.add(filenameInfo);
        }
        return filenameList;
    }

    /**
     * 向服务器发送SROT指令，实现文件的上传
     * @param pathname 客户端要上传文件的路径名
     * @throws IOException
     * @throws ClientException
     */
    public synchronized void stor(String pathname) throws IOException, ClientException {
        String[] strings = pathname.split("/");
        String fileName = strings[strings.length - 1];
        String line = String.format("STOR %s", fileName);
        Log.d("debug1", "向服务器发送STOR指令"+line);
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);

        //如果命令得到的回应不是200或无回应则失败
        if (response == null || !response.startsWith("200")) {
            throw new ClientException(response);
        }

        //否则则进行下一步，检查数据连接
        if (!keepConnected || (keepConnected && dataSocket == null)) {
            boolean success = dataConnect();
            //数据连接失败就抛出异常
            if (!success) {
                Log.d("debug1", "建立数据连接失败");
                throw new ClientException("建立数据连接失败");
            }
            Log.d("debug1", "建立数据连接");
        }


        response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response.startsWith("125")) {
            // 如果是以Ascii方式传输
            if (this.type == Type.A) {
                File file = new File(pathname);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new FileReader(file));

                int count = 0;
                while (true) {
                    String line1 = reader.readLine();
                    if (line1 == null) {
                        break;
                    }
                    if (count > 0) {
                        writer.newLine();
                    }
                    writer.write(line1);
                    count++;
                }
                // 读取完毕之后就非持久连接就关闭数据连接
                if (!keepConnected) {
                    dataSocket.close();
                    dataSocket = null;
                }
                writer.close();
                //reader.close();
            }
            // type为Binary模式
            else {
                File file = new File(pathname);
                FileInputStream inputStream = new FileInputStream(file);
                byte[] bytes = new byte[1024 * 1024];
                OutputStream outputStream = dataSocket.getOutputStream();
                int size = 0;
                while ((size = inputStream.read(bytes)) != -1) {
                    // 如果文件的大小超过了1024 * 1024
                    if (size == 1024 * 1024) {
                        outputStream.write(bytes);
                        outputStream.flush();
                        bytes = new byte[1024 * 1024];
                    }
                    else {
                        byte[] result = new byte[size];
                        System.arraycopy(bytes, 0 , result, 0, size);
                        outputStream.write(result);
                        outputStream.flush();
                        bytes = new byte[1024 * 1024];
                    }
                }
                if (!keepConnected) {
                    dataSocket.close();
                    dataSocket = null;
                }
                outputStream.close();
                //inputStream.close();
            }

            //根据状态码检验文件传输是否成功
            response = commandSocketReader.readLine();
            Log.d("debug1", response);
            if (response == null || !response.startsWith("226")) {
                Log.d("debug1", "不知道什么错误");
                throw new ClientException(response);
            }
        }
//            boolean success = dataConnect();
//            // 如果数据连接成功
//            if (success) {
////                response = commandSocketReader.readLine();
////                Log.d("debug1", response);
////                if (response.startsWith("125")) {
////                    // 如果是以Ascii方式传输
////                    if (this.type == Type.A) {
////                        File file = new File(pathname);
////                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
////                        BufferedReader reader = new BufferedReader(new FileReader(file));
////
////                        int count = 0;
////                        while (true) {
////                            String line1 = reader.readLine();
////                            if (line1 == null) {
////                                break;
////                            }
////                            if (count > 0) {
////                                writer.newLine();
////                            }
////                            writer.write(line1);
////                            count++;
////                        }
////                        // 读取完毕之后就关闭数据连接
////                        dataSocket.close();
////                        dataSocket = null;
////                        writer.close();
////                    }
////                    // type为Binary模式
////                    else {
////                        File file = new File(pathname);
////                        FileInputStream inputStream = new FileInputStream(file);
////                        byte[] bytes = new byte[1024 * 1024];
////                        OutputStream outputStream = dataSocket.getOutputStream();
////                        int size = 0;
////                        while ((size = inputStream.read(bytes)) != -1) {
////                            // 如果文件的大小超过了1024 * 1024
////                            if (size == 1024 * 1024) {
////                                outputStream.write(bytes);
////                                outputStream.flush();
////                                bytes = new byte[1024 * 1024];
////                            }
////                            else {
////                                byte[] result = new byte[size];
////                                System.arraycopy(bytes, 0 , result, 0, size);
////                                outputStream.write(result);
////                                outputStream.flush();
////                                bytes = new byte[1024 * 1024];
////                            }
////                        }
////                        dataSocket.close();
////                        dataSocket = null;
////                        outputStream.close();
////                        inputStream.close();
////                    }
////
////                    //根据状态码检验文件传输是否成功
////                    response = commandSocketReader.readLine();
////                    Log.d("debug1", response);
////                    if (response == null || !response.startsWith("226")) {
////                        throw new ClientException(response);
////                    }
////                }
//            }

    }

    public synchronized void setKeepConnected(boolean keep) throws IOException, ClientException {
        String arg = (keep)? "T" : "F";
        String line = String.format("KEEP %s", arg);
        Log.d("debug1", "向服务器发送KEEP指令:"+line);
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);
        //如果没有成功的话
        if (response == null || !response.startsWith("200")) {
            throw new ClientException(response);
        }
        //成功，是否使用持久化连接被设置
        this.keepConnected = keep;
        Log.d("debug1", "持久化"+keep);
    }

    /**
     * 向服务器发送RETR指令，实现从服务器端下载文件
     * @param source 想要从服务器端获取的文件名
     * @param target 客户端下载路径
     * @throws IOException
     * @throws ClientException
     */
    public synchronized void retrieveFile(String source, String target) throws IOException, ClientException {
        String line = String.format("RETR %s", source);
        Log.d("debug1", "向服务器发送RETR命令 "+line);
        writeCommand(line);
        String response = commandSocketReader.readLine();
        Log.d("debug1", response);

        //若是没有回应或者是未成功
        if (response == null || !response.startsWith("200")) {
            throw new ClientException(response);
        }

        // 以下两种情况要进行数据连接
        // 1.不是持久化数据连接 2.持久化数据连接但是从来没有过数据连接
        if (!keepConnected || (keepConnected && dataSocket == null)) {
            //建立数据连接
            boolean success = dataConnect();
            if (!success) {
                Log.d("debug1", "建立数据连接失败");
                throw new ClientException("建立数据连接失败");
            }
        }
//        //建立数据连接
//        boolean success = dataConnect();
//        if (!success) {
//            Log.d("debug1", "建立数据连接失败");
//            throw new ClientException("建立数据连接失败");
//        }

        // 根据状态码检验数据连接是否成功
        response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response == null || !response.startsWith("125")) {
            Log.d("debug1", "错误");
            throw new ClientException(response);
        }

        target = target + source;
        Log.d("debug1", "target:"+target);
        File file = new File(target);
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

        // type为Ascii
        if (type == Type.A) {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target)));
            //逐行读取并写入文件
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

            int count = 0;
            while (true) {
                String line1 = bufferedReader.readLine();
                if (line1 == null) {
                    break;
                }
                if (count > 0) {
                    bufferedWriter.newLine();
                }
                bufferedWriter.write(line1);
                count++;
            }

            // 读取完毕之后就关闭数据连接
            if (!keepConnected) {
                dataSocket.close();
                dataSocket = null;
            }
            bufferedWriter.close();
            //bufferedReader.close();
        }
        //type为Binary模式
        else {
            //获得文件输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
            //从服务器上读取字节流的缓冲区，每次最多读取1MB
            byte[] bytes = new byte[1024 * 1024];
            //从数据连接获得input stream
            InputStream inputStream = dataSocket.getInputStream();

            int num = 0;
            while ((num = inputStream.read(bytes)) != -1) { // 没读到末尾
                if (num == 1024 * 1024) {
                    out.write(bytes);
                    out.flush();
                    bytes = new byte[1024 * 1024];
                }
                else {
                    byte[] result = new byte[num];
                    System.arraycopy(bytes, 0, result,0, num);
                    out.write(result);
                    out.flush();
                    bytes = new byte[1024 * 1024];
                }
            }

            //如果是非持久连接则关闭
            if (!keepConnected) {
                dataSocket.close();
                dataSocket = null;
            }
//            dataSocket.close();
//            dataSocket = null;
            out.close();
            //inputStream.close();
        }

        //根据状态码检验文件传输是否成功
        response = commandSocketReader.readLine();
        Log.d("debug1", response);
        if (response == null || !response.startsWith("226")) {
            throw new ClientException(response);
        }
    }

    /**
     * to do 实现下载文件夹
     * @param path 想要下载的文件夹所在的路径
     * @throws IOException
     * @throws ClientException
     */
    public synchronized void retrieveFolder(String path) throws IOException, ClientException {
        String[] strings = path.split("[/][\\\\]");

        // 需要下载的文件夹的名字
        String folderName = strings[strings.length - 1];
        //先用LIST指令获取该文件夹下所有的文件名
        List<String> serverFileList = list(path);
        // 生成每个文件在客户端上存放的位置
        List<String> clientFileList = Utility.generateClientPath(downloadDirectory, serverFileList, folderName, path);
        //根据生成的位置新建文件夹
        HashSet<String> folderNames = new HashSet<>();
        for (String fileName : clientFileList) {
            int i = fileName.lastIndexOf("/");
            folderNames.add(fileName.substring(0, i));
        }
        for (String foldername : folderNames) {
            boolean success = new File(foldername).mkdirs();
        }
        // 根据文件夹中的文件个数一个个地下载文件
        for (int i = 0; i < serverFileList.size(); i++) {
            retrieveFile(serverFileList.get(i), clientFileList.get(i));
        }


    }

    /**
     * 与服务器建立数据连接
     * @return 数据连接是否成功
     * @throws IOException
     */
    public boolean dataConnect() throws IOException {
        // 如果已经有了数据连接，先关闭
        if (dataSocket != null) {
            dataSocket.close();
        }

        //如果是主动模式，由客户端来生成port,服务器来连接
        if (passiveOrActive == PassiveOrActive.ACTIVE) {
            this.dataSocket = serverSocket.accept();
            return true;
        }
        // 如果是被动模式
        else {
            Log.d("debug1", "被动模式下的数据连接");
            this.dataSocket = new Socket(address, port);
            Log.d("debug1", "serveraddress"+address+"port"+port);
            return true;
        }
    }


    /**
     * 设定从服务器下载的文件所在目录
     * @param downloadDirectory
     * @throws ClientException
     */
    public synchronized void setDownloadDirectory(String downloadDirectory) throws ClientException {
        File dir = new File(downloadDirectory);
        if (!dir.exists()) {
            throw new ClientException("目录不存在！");
        }
        if (!dir.isDirectory()) {
            throw new ClientException("不是合法目录！");
        }
        this.downloadDirectory = downloadDirectory;
    }

    private void writeCommand(String line) throws IOException {
        commandSocketWriter.write(line);
        commandSocketWriter.write("\r\n");
        commandSocketWriter.flush();
    }

    public int getType() {
        if (this.type == Type.A) {
            return 0;
        }
        else if (this.type == Type.B) {
            return 1;
        }
        return -1;
    }

    public int getMode() {
        if (this.mode == Mode.S) {
            return 0;
        }
        else if (this.mode == Mode.B) {
            return 1;
        }
        else if (this.mode == Mode.C) {
            return 2;
        }
        return -1;
    }

    public int getStru() {
        if (this.stru == Stru.F) {
            return 0;
        }
        else if (this.stru == Stru.R) {
            return 1;
        }
        else if (this.stru == Stru.P) {
            return 2;
        }
        return -1;
    }

    public int getPassiveOrActive() {
        if (this.passiveOrActive == PassiveOrActive.PASSIVE) {
            return 0;
        }
        else if (this.passiveOrActive == PassiveOrActive.ACTIVE) {
            return 1;
        }
        return -1;
    }

    public String getDownloadDirectory() {
        return this.downloadDirectory;
    }

    public int getKeepConnection() {
        return (this.keepConnected)? 0:1;
    }


}
