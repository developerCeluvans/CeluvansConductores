package com.imaginamos.taxisya.taxista.io;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.imaginamos.taxisya.taxista.BuildConfig;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean usePush = false;
        if (BuildConfig.USE_PUSH) {
            Log.v("SERVICE_CMS", "GCM BROADCAST_RECEIVER USO DE PUSH OK");
            Log.e("ENTRO", "hola");
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
            usePush = true;
        }
        Log.v("SERVICE_CMS", "GCM BROADCAST_RECEIVER PUSH "  + (usePush ? "ENABLED" : "DISABLED - DEBUG"));

    }
}
