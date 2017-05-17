package com.imaginamos.taxisya.taxista.io;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Check device's network connectivity and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
public class Connectivity {

    /**
     * Get the network info
     *
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion 
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private int checkConnectivityInterval = 5000;
    private int connectivityIndicator = 1500;
    private boolean connectivityCheckResult = true;

    private ConnectivityQualityCheckListener listener;

    public Connectivity() {
    }

    public Connectivity(ConnectivityQualityCheckListener listener) {
        this.listener = listener;
    }

    public void startConnectivityMonitor() {
        if (!connectivityQualityMonitor.isAlive())
            connectivityQualityMonitor.start();
    }

    public void stopConnectivityMonitor() {
        connectivityQualityMonitor.interrupt();
    }

    public void setConnectivityCheckInterval(int interval) {
        this.checkConnectivityInterval = interval;
    }

    public void setConnectivityIndicator(int indicator) {
        this.connectivityIndicator = indicator;
    }

    public boolean getConnectivityCheckResult(){
        return this.connectivityCheckResult;
    }

    Handler connectivityMonitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            connectivityCheckResult = (msg.what == 0);
            if (listener != null)
                listener.onConnectivityQualityChecked((msg.what == 0));
        }
    };

    Thread connectivityQualityMonitor = new Thread(new Runnable() {
        @Override
        public void run() {

            while (true) {


                MiddleConnect.testConnectivityQuality(connectivityMonitorResponseHandler);

                try {
                    Thread.sleep(checkConnectivityInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    });

    AsyncHttpResponseHandler connectivityMonitorResponseHandler = new AsyncHttpResponseHandler() {
        long beforeTime = -1;
        long afterTime = -1;
        boolean failed = false;

        @Override
        public void onStart() {
            beforeTime = System.currentTimeMillis();
            failed = false;
            afterTime = -1;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String s = new String(responseBody);
            afterTime = System.currentTimeMillis();
            connectivityMonitorHandler.sendEmptyMessage((afterTime - beforeTime) <= connectivityIndicator ? 0 : -1);
            connectivityCheckResult = true;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            String s = new String(responseBody);
            connectivityMonitorHandler.sendEmptyMessage(-1);
            connectivityCheckResult = false;
        }
    };

    public interface ConnectivityQualityCheckListener {
        void onConnectivityQualityChecked(boolean Optimal);
    }

}
