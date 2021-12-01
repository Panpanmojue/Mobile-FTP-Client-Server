package com.example.test2;

import static com.example.test2.MyApplication.getContext;

import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<File> files;

    private File currentFile = Environment.getExternalStorageDirectory();// 当前文件夹
    private Handler handler;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.file_image);
            textView = itemView.findViewById(R.id.file_name);
        }
    }

    public FileAdapter(List<File> files) {
        this.files = files;
    }

    public FileAdapter(List<File> files, Handler handler) {
        this.files = files;
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
                File file = files.get(holder.getAdapterPosition());
                // 如果打开的文件是一个文件夹的话
                if (file.isDirectory()) {
                    currentFile = file;
                    files.clear();// 将之前的files列表清空
                    files.addAll(Arrays.asList(file.listFiles()));// 重新将打开的目录下的文件赋值给files
                    notifyDataSetChanged();
            }
        }
        });

        // 长按文件事件，可以进行复制
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //创建弹出菜单
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                //获取菜单填充器
                final MenuInflater inflater = popupMenu.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.upload, popupMenu.getMenu());
                // 进行点击菜单选项事件的绑定
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        File file = files.get(holder.getAdapterPosition());
                        switch (item.getItemId()) {
                            case R.id.upload:
                                //FileUtil.setSaveFile(file);
                                try {
                                    long start = System.nanoTime();
                                    Log.d("debug1", "文件开始上传");
                                    FTPUtil.getFtpClient().stor(file.getPath());
                                    long end = System.nanoTime();
                                    long time = end - start;
                                    Log.d("debug1", "文件上传完毕，耗时"+(end - start));
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putLong("Time", time);
                                    message.setData(bundle);
                                    message.what = 88;
                                    handler.sendMessage(message);

                                } catch (IOException | ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.delete:
                                files.remove(file);
                                file.delete();
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
        File file = files.get(position);
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
        return files.size();
    }


    public void toLastFile() {
        if (!currentFile.equals(Environment.getExternalStorageDirectory())) {
            currentFile = currentFile.getParentFile();
            files.clear();
            files.addAll(Arrays.asList(currentFile.listFiles()));
            notifyDataSetChanged();
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }
}
