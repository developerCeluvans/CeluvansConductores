package com.imaginamos.taxisya.taxista.io;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.imaginamos.taxisya.taxista.model.Conf;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class DriverService extends Service {

    private String TAG = "DRIVER_SERVICE";

    private String uuid;
    private String driver_id;
    private Conf conf;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "onStartCommand == null");

        conf = new Conf(this);

        driver_id = conf.getIdUser();

        uuid = conf.getUuid();

        deshabilitarme();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "*** onDestroy *****");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void deshabilitarme() {

        Log.v("DRIVER_SERVICE", "        force disableDrive: ");

        MiddleConnect.disableDrive(this, driver_id, uuid, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.v("DRIVER_SERVICE", "onStart()");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.v("DRIVER_SERVICE", "onSucess() " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                Log.v("DRIVER_SERVICE", "onFailure() " + response);
            }

            @Override
            public void onFinish() {
                Log.v("DRIVER_SERVICE", "onFinish()");
            }

        });

    }


}
