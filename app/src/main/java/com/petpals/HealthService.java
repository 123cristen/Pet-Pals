package com.petpals;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import java.util.Calendar;

public class HealthService extends Service {

    long lastFed = 0;
    IBinder binder;

    public HealthService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastFed = intent.getLongExtra("LAST_FED", 0);
        Log.d("HealthService", String.valueOf(lastFed));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
