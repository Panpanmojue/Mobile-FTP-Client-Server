package com.example.test2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.test2.exceptions.ClientException;

import java.io.IOException;

public class FirstActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.type:
                Toast.makeText(this, "You click type", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请选择TYPE");
                String[] ss = {"Ascii", "Binary"};
                builder.setSingleChoiceItems(ss, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(FirstActivity.this, ss[which], Toast.LENGTH_SHORT).show();
                        switch (which) {
                            case 0:
                                try {
                                    FTPUtil.getFtpClient().setType(FTPClient.Type.A);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (ClientException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                try {
                                    FTPUtil.getFtpClient().setType(FTPClient.Type.B);
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
                Toast.makeText(this, "You click mode", Toast.LENGTH_SHORT).show();
                break;
            case R.id.stru:
                Toast.makeText(this, "You click stru", Toast.LENGTH_SHORT).show();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);
        Button button1 = findViewById(R.id.button_1);
        EditText editText1 = findViewById(R.id.address_text);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(FirstActivity.this, "You clicked the button1", Toast.LENGTH_SHORT).show();
                //finish();
                String address = editText1.getText().toString();
                Toast.makeText(FirstActivity.this, address, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(FirstActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });
    }


}