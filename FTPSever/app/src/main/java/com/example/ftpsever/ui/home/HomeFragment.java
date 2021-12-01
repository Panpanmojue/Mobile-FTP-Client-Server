package com.example.ftpsever.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.ftpsever.R;
import com.example.ftpsever.SocketServer;
import com.example.ftpsever.databinding.FragmentHomeBinding;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Socket socket;
    private SocketServer socketServer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.text;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        //需要使用getActivity()方法找到控件
        Button openPort= getActivity().findViewById(R.id.StoreButton);
        openPort.setOnClickListener(v -> {
            System.out.println("try to connect");
            try {
                //System.out.println(Context.getExternalFilesDir(null));

                //File ile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)[0],"Test.apk");
                SocketServer socketServerTest=new SocketServer(8081,"/storage/emulated/0/system");
                //Toast.makeText(MainActivity.class, "open success", Toast.LENGTH_SHORT).show();
                socketServerTest.start();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("socket exception");
            }
            //SocketServer socketServer=new SocketServer(8001,"E:\\");
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}