package com.example.test2;

import static com.example.test2.MyApplication.getContext;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test2.exceptions.ClientException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FTPFileAdapter extends RecyclerView.Adapter<FTPFileAdapter.ViewHolder> {
    private List<FTPFile> ftpFiles;
    private FTPFile currentFile;
    private FileListActivity mainActivity;
    private Handler handler;

    public FTPFileAdapter(List<FTPFile> ftpFiles, FileListActivity mainActivity) throws IOException, ClientException {
        this.ftpFiles = ftpFiles;
        this.mainActivity = mainActivity;
    }

    public FTPFileAdapter(List<FTPFile> ftpFiles, FileListActivity mainActivity, Handler handler) {
        this.ftpFiles = ftpFiles;
        this.mainActivity = mainActivity;
        this.handler = handler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        // 点击文件事件
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FTPFile file = ftpFiles.get(holder.getAdapterPosition());
                if (file.isDirectory()) {
                    currentFile = file;
                    ftpFiles.clear();
                    try {
                        ftpFiles.addAll(Arrays.asList(file.listFile()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                    notifyDataSetChanged();
                }
            }
        });

        //长按文件事件
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //创建弹出菜单
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                //获取菜单填充器
                final MenuInflater inflater = popupMenu.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.download, popupMenu.getMenu());
                //进行点击菜单选项事件的绑定
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FTPFile file = ftpFiles.get(holder.getAdapterPosition());
                        switch (item.getItemId()) {
                            case R.id.download:
                                try {
                                    long start = System.nanoTime();
                                    Log.d("debug1", "文件开始下载");
                                    FTPUtil.getFtpClient().retrieveFile(file.getName(), FTPUtil.getFtpClient().getDownloadDirectory());
                                    long end = System.nanoTime();
                                    Log.d("debug1", "文件下载完毕，花费"+(end - start));
                                    long time = end - start;
                                    Message message = new Message();
                                    Bundle bundle  =new Bundle();
                                    bundle.putLong("Time", time);
                                    message.setData(bundle);
                                    message.what = 88;
                                    handler.sendMessage(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.delete:
                                break;
                            default:
                                break;
                        }
                        notifyDataSetChanged();
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FTPFile file = ftpFiles.get(position);
        holder.textView.setText(file.getName());
        if (file.isFile()) {
            holder.imageView.setImageDrawable(getContext().getDrawable(R.drawable.file));
        }
        else {
            holder.imageView.setImageDrawable(getContext().getDrawable(R.drawable.directory));
        }
    }

    @Override
    public int getItemCount() {
        return ftpFiles.size();
    }


    public void toLastFile() throws IOException, ClientException {
        if (!currentFile.equals(new FTPFile("/"))) {
            currentFile = currentFile.getParentFile();
            ftpFiles.clear();
            ftpFiles.addAll(Arrays.asList(currentFile.listFile()));
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.file_image);
            textView = itemView.findViewById(R.id.file_name);
        }
    }
}
