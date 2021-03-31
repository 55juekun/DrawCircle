package com.drawcircle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private heyksCircleBar cpbHeartBeat;       // 心跳
    private MyHandler handler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cpbHeartBeat = findViewById(R.id.cpb_total);
        cpbHeartBeat.setMIN_VAL(100);
        cpbHeartBeat.setMAX_VAL(1000);
        cpbHeartBeat.setFgColors(new int[]{0xFF11EE96, 0xFF00FFFF, 0xFF000000});
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        handler.sendEmptyMessage(11);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * 消息处理
     */
    private class MyHandler extends Handler {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            cpbHeartBeat.update(800, 4000);
        }
    }
}