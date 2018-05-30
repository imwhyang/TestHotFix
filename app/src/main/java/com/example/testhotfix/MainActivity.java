package com.example.testhotfix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt = findViewById(R.id.tv);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });
        fixBug();
        Log.d(TAG, "onCreate: sdfasdf");
    }

    private void fixBug() {
        //当前应用目录下，存放修改后的dex
        File fileDir = getDir(ProCst.DEX_DIR, Context.MODE_PRIVATE);
        //创建一个应用内的文件目录
        String name = "classes2.dex";
        File dexFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + name);
        if (!dexFile.exists()) {
            Log.d(TAG, "fixBug: path isn't exists");
            return;
        }
        String filePath = fileDir.getAbsoluteFile() + File.separator + name;
        File file = new File(filePath);
        if (file.exists())
            file.delete();
//把SD 卡里的dex 转到应用内目录下
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + name);
//            is = new FileInputStream(Environment.getDataDirectory().getAbsolutePath() + File.separator + name);
//            is = new FileInputStream(getCacheDir().getAbsolutePath() + File.separator + name);
            os = new FileOutputStream(filePath);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            File f = new File(filePath);
            if (f.exists()) {
                Log.d(TAG, "dex turn in: success");
            }

            FixDexUtils.loadFixeDes(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "fixBug: ", e);
        }
    }
}
