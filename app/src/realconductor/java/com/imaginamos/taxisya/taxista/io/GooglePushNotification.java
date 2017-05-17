package com.imaginamos.taxisya.taxista.io;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.imaginamos.taxisya.taxista.model.Conf;

import java.io.IOException;

public class GooglePushNotification {

    public static String SENDER_ID = "217009125456";

    private Context context;
    private Handler hand;

    public GooglePushNotification(Context context) {
        this.context = context;
        new Registry().execute("");
    }

    public GooglePushNotification(Context context, Handler hand) {
        this.context = context;
        this.hand = hand;
        new Registry().execute("");
    }

    public int getAppVersion(Context context) {

        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;

        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    class Registry extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String uuid = null;

            try {

                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                uuid = gcm.register(SENDER_ID);

                Log.e("UUID_GENERADO", uuid);

            } catch (IOException e) {
                Log.e("UUID_ERROR", e.toString() + "");
            }

            return uuid;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Conf conf = new Conf(context);

            conf.setUuid(result);

            if (hand != null) {
                hand.sendEmptyMessage(0);
            }
        }

    }

}
