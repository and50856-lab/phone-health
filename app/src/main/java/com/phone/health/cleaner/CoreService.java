package com.phone.health.cleaner;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import android.widget.Toast;

public class CoreService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceName = Build.MODEL; // Например, Samsung J200F
        // Здесь в будущем будет инициализация связи с пультом
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
