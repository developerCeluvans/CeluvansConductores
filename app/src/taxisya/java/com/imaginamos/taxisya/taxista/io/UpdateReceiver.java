package com.imaginamos.taxisya.taxista.io;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

public class UpdateReceiver extends BroadcastReceiver {

    private UpdateReceiverListener listener;

    public UpdateReceiver(){

    }

    public UpdateReceiver(UpdateReceiverListener listener){

        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();

        if(listener != null) {
            listener.onNetworkConnectivityChange(Connectivity.isConnected(context) && isOnline());
        }

        if (isConnected) {
            Log.e("ERROR", "YES_NET " + String.valueOf(new Date()));
            context.sendBroadcast(new Intent("YES_NET"));
        } else {
            Log.e("ERROR", "NO_NET " + String.valueOf(new Date()));
            context.sendBroadcast(new Intent("NO_NET"));
        }


    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 www.taxisya.co");
            int     exitValue = ipProcess.waitFor();

            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public interface UpdateReceiverListener {

        void onNetworkConnectivityChange(boolean connected);
    }

}
