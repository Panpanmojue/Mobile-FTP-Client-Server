package com.example.test2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test2.exceptions.ClientException;
import com.example.test2.exceptions.UserInputException;

import java.io.IOException;

public class ConnectionActivity extends AppCompatActivity {
    private String address, username, password;
    private int port = 0;
    private FTPClient ftpClient;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        EditText editText1 = findViewById(R.id.address);
        EditText editText2 = findViewById(R.id.port);
        EditText editText3  = findViewById(R.id.username);
        EditText editText4 = findViewById(R.id.password);
        Button button = findViewById(R.id.connect);
        Switch anonymous = findViewById(R.id.anonymous_login);

        SharedPreferences pref = getSharedPreferences("connectData", MODE_PRIVATE);
        address = pref.getString("address", "X");
        password = pref.getString("password", "X");
        username = pref.getString("username", "X");
        port = pref.getInt("port", 0);

        if (address != null && !address.equals("X")) {
            editText1.setText(address);
        }
        if (password != null && !password.equals("X")) {
            editText4.setText(password);
        }
        if (username != null && !username.equals("X")) {
            editText3.setText(username);
        }
        if (port != 0) {
            editText2.setText(String.valueOf(port));
        }

        anonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 选中状态
                if (isChecked) {
                    editText3.setText("anonymous");
                    editText4.setText("");
                    editText4.setHint("no need for password");
                }
                // 未选中状态
                else {
                    editText3.setText("");
                    editText4.setHint("Please input the password");
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = editText1.getText().toString();
                port =Integer.parseInt (editText2.getText().toString().trim());
                username = editText3.getText().toString();
                password = editText4.getText().toString();

                // 保存用户之前输入过的信息
                SharedPreferences.Editor editor = getSharedPreferences("connectData", MODE_PRIVATE).edit();
                editor.putString("address", address);
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putInt("port", port);
                editor.apply();

                try {
                    tryConnect(address, port, username, password);
                    Intent intent = new Intent(ConnectionActivity.this, FileListActivity.class);
                    startActivity(intent);
                } catch (IOException | UserInputException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    public void tryConnect(String address, int port, String username, String password) throws IOException, UserInputException {
        if (address.equals("") || port == 0 ) {
            Toast.makeText(this, "请填写完整的信息", Toast.LENGTH_SHORT).show();
            throw new UserInputException();
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FTPUtil.init(address, port, username, password);
                        ftpClient = FTPUtil.getFtpClient();
                        //ftpClient = new FTPClient(address, port);
                        Log.d("debug1" , String.valueOf(ftpClient.isConnected()));
                    }
                    catch (IOException | ClientException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ConnectionActivity.this, "与服务器连接失败！" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    Log.d("debug1",String.valueOf(ftpClient.isConnected()));
                    //System.out.println(ftpClient);
                    // 如果与服务器连接成功的话，进行登陆
                    if (ftpClient.isConnected()) {
                        // 打印连接成功的提示
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ConnectionActivity.this, "与服务器连接成功！" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        boolean success = false;
                        try {
                            success = FTPUtil.getFtpClient().login(username, password);
                            if (success) {
                                // 跳转到主页面
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ConnectionActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Intent intent = new Intent(ConnectionActivity.this, FileListActivity.class);
                                startActivity(intent);
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ConnectionActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        catch (IOException | ClientException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ConnectionActivity.this, "登陆失败" ,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    }
}