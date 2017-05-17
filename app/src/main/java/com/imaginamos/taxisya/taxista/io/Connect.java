package com.imaginamos.taxisya.taxista.io;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.imaginamos.taxisya.taxista.BuildConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Connect {

    public static final String BASE_URL = "http://" + BuildConfig.HOST + "/public/";
    private static final String CONNECTIVITY_QUALITY_CHECKING = "http://www.taxisya.co/dev/";
    public static final int timeout = 40;

    public static AsyncHttpClient syncHttpClient= new SyncHttpClient();
    public static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().setTimeout(timeout * 1000);
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().setTimeout(timeout * 1000);
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postSync(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().setTimeout(timeout * 1000);
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }


    public static void sendJson(Context context, String url, JSONObject bodyAsJson,
                                AsyncHttpResponseHandler responseHandler) {
        try {

            StringEntity stringEntity = new StringEntity(bodyAsJson.toString());
            //ByteArrayEntity entity = new ByteArrayEntity(bodyAsJson.toString().getBytes("UTF-8"));
            getClient().post(context, getAbsoluteUrl(url), stringEntity, "application/json", responseHandler);

        } catch (Exception e) {
            Log.e("sendJson error", e.toString());
        }
    }

    public static void connectivityQualityTest(AsyncHttpResponseHandler responseHandler)
    {
        getClient().setTimeout(timeout * 1500);
        getClient().get(CONNECTIVITY_QUALITY_CHECKING, null,responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.e("BASE_URL", BASE_URL + relativeUrl);
        return BASE_URL + relativeUrl;
    }

    private static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }
}
