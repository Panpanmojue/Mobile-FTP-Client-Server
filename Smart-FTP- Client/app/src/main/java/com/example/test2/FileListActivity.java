package com.example.test2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test2.exceptions.ClientException;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FileListActivity extends AppCompatActivity {
    private List<File> fileList;
    private final List<FTPFile> ftpFileList = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private FTPFileAdapter ftpFileAdapter;
    //private ConnectBroadcastR
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
          long time = message.getData().getLong("Time");
          double result = time / 1e9;
          Toast.makeText(FileListActivity.this, "文件传输成功，花费时间为"+result+"s", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        checkNeedPermissions();
        getPermissions();
        initFileData();
        initUI();
        initMenu();
    }


    private void initFileData() {
        File file = Environment.getExternalStorageDirectory();
        fileList = new ArrayList<>(Arrays.asList(file.listFiles()));
        System.out.println(Arrays.toString(file.listFiles()));
    }

    private void initUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }


        recyclerView = findViewById(R.id.file_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //adapter = new FileAdapter(fileList);
        adapter = new FileAdapter(fileList, handler);
        recyclerView.setAdapter(adapter);



    }

    private void initMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //连接服务器
                    case R.id.nav_setting:
                        Intent intent = new Intent(FileListActivity.this, ConnectionActivity.class);
                        startActivity(intent);
                        break;
                    // 查看服务器文件
                    case R.id.nav_FtpServerDirectory:
                        Toast.makeText(FileListActivity.this, "查看服务器文件", Toast.LENGTH_SHORT).show();
                        // 如果连接成功的话
                        if (FTPUtil.getFtpClient() != null && FTPUtil.getFtpClient().isConnected()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ftpFileList.clear();
                                        List<String> temp = FTPUtil.getFtpClient().list("");
                                        List<FTPFile> files = Utility.stringToFTPFile(temp);
                                        // 寻找ftpFileList中是否有file
                                        for (FTPFile file : files) {
                                            file.setPath(file.getFileName());
                                            boolean check = false;
                                            for (FTPFile ftpFile : ftpFileList) {
                                                if (file.getFileName().equals(ftpFile.getFileName())) {
                                                    check = true;
                                                    break;
                                                }
                                            }
                                            // 如果没有则添加
                                            if (!check) {
                                                ftpFileList.add(file);
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ftpFileAdapter = new FTPFileAdapter(ftpFileList, FileListActivity.this, handler);
                                                recyclerView.setAdapter(ftpFileAdapter);
                                                ftpFileAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (ClientException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();
                        }
                        // 如果连接未成功的话
                        else {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FileListActivity.this, "未连接到服务器", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //查看本地文件
                    case R.id.nav_LocalDirectory:
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //断开当前连接
                    case R.id.nav_close:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (FTPUtil.getFtpClient() != null) {
                                    try {
                                        FTPUtil.getFtpClient().disconnect();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FileListActivity.this, "断开当前连接", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FileListActivity.this, "断开连接失败", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }


    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                }else{
                    //Toast.makeText(this,"没有权限打开相册",Toast.LENGTH_SHORT).show();
                }
            case 2:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.type:
                //Toast.makeText(this, "You click type", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请选择TYPE");
                String[] ss = {"Ascii", "Binary"};
                builder.setSingleChoiceItems(ss, FTPUtil.getFtpClient().getType(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(FileListActivity.this, ss[which], Toast.LENGTH_SHORT).show();
                        switch (which) {
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().setType(FTPClient.Type.A);
                                    Toast.makeText(FileListActivity.this, "成功设置为Ascii模式", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().setType(FTPClient.Type.B);
                                    Toast.makeText(FileListActivity.this, "成功设置为Binary模式", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            case R.id.mode:
                //Toast.makeText(this, "You click mode", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("请选择MODE");
                String[] ss1 = {"Stream","Block", "Compressed"};
                builder1.setSingleChoiceItems(ss1, FTPUtil.getFtpClient().getMode(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().setMode(FTPClient.Mode.S);
                                    Toast.makeText(FileListActivity.this, "成功设置为Stream Mode", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().setMode(FTPClient.Mode.B);
                                    Toast.makeText(FileListActivity.this, "成功设置为Block Mode", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                try {
                                    FTPUtil.getFtpClient().setMode(FTPClient.Mode.C);
                                    Toast.makeText(FileListActivity.this, "成功设置为Compressed Mode", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });

                builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog1 = builder1.create();
                alertDialog1.show();
                break;
            case R.id.stru:
                //Toast.makeText(this, "You click stru", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("请选择STRU");
                String[] ss2 = {"File", "Record", "Page"};
                builder2.setSingleChoiceItems(ss2, FTPUtil.getFtpClient().getStru(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().setStru(FTPClient.Stru.F);
                                    Toast.makeText(FileListActivity.this, "成功设置为File", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().setStru(FTPClient.Stru.R);
                                    Toast.makeText(FileListActivity.this, "成功设置为Record", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                try {
                                    FTPUtil.getFtpClient().setStru(FTPClient.Stru.P);
                                    Toast.makeText(FileListActivity.this, "成功设置为Page", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });

                builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog2 = builder2.create();
                alertDialog2.show();
                break;
            case R.id.passiveOrActive:
                //Toast.makeText(this, "You click passiveOrActive", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle("请选择使用主动模式或被动模式");
                String[] ss3 = {"被动模式Passive", "主动模式Active"};
                builder3.setSingleChoiceItems(ss3, FTPUtil.getFtpClient().getPassiveOrActive(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().pasv();
                                    Toast.makeText(FileListActivity.this, "成功设置为被动模式", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().port();
                                    Toast.makeText(FileListActivity.this, "成功设置为主动模式", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });

                builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog3 = builder3.create();
                alertDialog3.show();
                break;
            case R.id.keepConnection:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
                builder4.setTitle("请选择是否快速传输");
                String[] ss4 = {"是", "否"};
                builder4.setSingleChoiceItems(ss4, FTPUtil.getFtpClient().getKeepConnection(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().setKeepConnected(true);
                                    Toast.makeText(FileListActivity.this, "成功设置为快速传输", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().setKeepConnected(false);
                                    Toast.makeText(FileListActivity.this, "不快速传输", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });

                builder4.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog4 = builder4.create();
                alertDialog4.show();
                break;
            case android.R.id.home:
                drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (recyclerView.getAdapter() == adapter) {
            adapter.toLastFile();
        }
        else {
            if (FTPUtil.getFtpClient() != null && FTPUtil.getFtpClient().isConnected()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ftpFileList.clear();
                            List<String> temp = FTPUtil.getFtpClient().list("");
                            List<FTPFile> files = Utility.stringToFTPFile(temp);
                            // 寻找ftpFileList中是否有file
                            for (FTPFile file : files) {
                                file.setPath(file.getFileName());
                                boolean check = false;
                                for (FTPFile ftpFile : ftpFileList) {
                                    if (file.getFileName().equals(ftpFile.getFileName())) {
                                        check = true;
                                        break;
                                    }
                                }
                                // 如果没有则添加
                                if (!check) {
                                    ftpFileList.add(file);
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ftpFileAdapter = new FTPFileAdapter(ftpFileList, FileListActivity.this);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (ClientException e) {
                                        e.printStackTrace();
                                    }
                                    recyclerView.setAdapter(ftpFileAdapter);
                                    ftpFileAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClientException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
            // 如果连接未成功的话
            else {
                drawerLayout.closeDrawer(GravityCompat.START);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileListActivity.this, "未连接到服务器", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void checkNeedPermissions(){
        //6.0以上需要动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }
}