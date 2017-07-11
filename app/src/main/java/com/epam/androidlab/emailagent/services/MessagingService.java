package com.epam.androidlab.emailagent.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MessagingService extends Service {

    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {
        doTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doTask() {
        new Thread(() -> {
            while (true) {
                System.out.println(1);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
