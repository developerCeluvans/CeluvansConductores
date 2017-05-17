package com.imaginamos.taxisya.taxista.io;

import android.content.Context;
import android.util.Log;

import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/***
 * EJEMPLO:
 * MiddleConnect.login("usename","password",new AsyncHttpResponseHandler(){
 *
 * @Override public void onStart() { }
 * @Override public void onSuccess(String response) {}
 * @Override public void onFailure(Throwable e, String response) {}
 * @Override public void onFinish() {}
 * });
 ***/

public class MiddleConnect {


    public static void sendMyPosition(Context context, String id_driver, String lat, String lon, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("lat", lat);

        params.put("lng", lon);
        Log.e("POSITOEN SEND", "lat = " + lat + " lng = " + lon + " " + context.getResources().getString(R.string.sendmyposition, id_driver) + " " + id_driver);
        Connect.post(context.getResources().getString(R.string.sendmyposition, id_driver), params, responseHandler);
    }

    public static void enableDrive(Context context, double lat, double lng, String driver_id, String uuid, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("uuid", uuid);
        params.put("lat", String.valueOf(lat));
        params.put("lng", String.valueOf(lng));

        Log.e("UUID_ENVIADO", uuid + "," + String.valueOf(lat) + "," + String.valueOf(lng));
        Log.v("DRIVER_SERVICE", "        enableDrive:");
        Log.v("MiddleConnect", "enableDrive " + String.valueOf(new Date()));
        Log.v("MiddleConnect", "params = " + params.toString());
        Connect.post(context.getResources().getString(R.string.enable_driver, driver_id), params, responseHandler);
    }

    public static void disableDrive(Context context, String driver_id, String uuid, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        Log.v("DRIVER_SERVICE", "        disableDrive:");
        Log.v("MiddleConnect", "disableDrive " + String.valueOf(new Date()));
        params.put("uuid", uuid);

        Connect.post(context.getResources().getString(R.string.disable_driver, driver_id), params, responseHandler);
    }

    public static void login(Context context, String user, String pass, String uuid, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("type", "2");

        params.put("login", user);

        params.put("pwd", Utils.md5(pass));

        params.put("uuid", uuid);

        Log.v("DRIVER_SERVICE", "        login: ");
        Log.v("DRIVER_SERVICE", "             login=" + user + " pwd=" + Utils.md5(pass) + " uuid=" + uuid);

        Connect.post(context.getResources().getString(R.string.login), params, responseHandler);
    }

    public static void sendMailReset(Context context,String email,AsyncHttpResponseHandler responseHandler)
    {
        RequestParams params = new RequestParams();

        params.put("email", email);
        params.put("isDriver","driver");

        Log.v("DRIVER_SERVICE", "        sendMailReset: ");

        Connect.post(context.getResources().getString(R.string.resetpass), params, responseHandler);
    }

    public static void sendMailResetConfirm(Context context,String email,String token,String password,AsyncHttpResponseHandler responseHandler)
    {
        RequestParams params = new RequestParams();

        params.put("email", email);
        params.put("token", token);
        params.put("password", password);
        params.put("isDriver","driver");

        Log.v("DRIVER_SERVICE", "        sendMailResetConfirm: ");
        Log.v("DRIVER_SERVICE", "        sendMailResetConfirm: " + password);

        Connect.post(context.getResources().getString(R.string.code_pass), params, responseHandler);
    }

    public static void update_car(Context context, String driver_id, String car_id, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("driver_id", driver_id);

        params.put("car_id", car_id);

        Log.v("DRIVER_SERVICE", "        update_car: ");
        Log.v("DRIVER_SERVICE", "             driver_id=" + driver_id + " car_id=" + car_id);

        Connect.post(context.getResources().getString(R.string.update_car), params, responseHandler);
    }

    public static void logout(Context context, String user, String uuid, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("login", user);

        params.put("uuid", uuid);

        Log.v("DRIVER_SERVICE", "        logout: ");

        Connect.post(context.getResources().getString(R.string.logout), params, responseHandler);
    }

    public static void sendConfirmation(Context context, String service_id, String id_driver, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("service_id", service_id);

        Log.v("DRIVER_SERVICE", "        sendConfirmation: ");

        Connect.post(context.getResources().getString(R.string.confirm_service, id_driver), params, responseHandler);
    }

    public static void arrived(Context context, String driver_id, AsyncHttpResponseHandler responseHandler) {
        Log.v("DRIVER_SERVICE", "        arrived: ");

        Connect.post(context.getResources().getString(R.string.arrived_serivce, driver_id), null, responseHandler);
    }

    public static void arrived2(Context context, String driver_id, String service_id, AsyncHttpResponseHandler responseHandler) {
        Log.v("DRIVER_SERVICE", "        arrived2: " + driver_id + " " + service_id);
        RequestParams params = new RequestParams();

        params.put("driver_id", driver_id);

        params.put("service_id", service_id);

        Connect.post(context.getResources().getString(R.string.arrived_service_probe), params, responseHandler);
    }

    public static void finishService(Context context, String driver_id, String service_id, double lat, double lng, String units, String charge1,String charge2, String charge3, String charge4, String value, String ref, AsyncHttpResponseHandler responseHandler) {
        Log.v("DRIVER_SERVICE", "        finishService: ");
        RequestParams params = new RequestParams();

        params.put("driver_id", driver_id);

        params.put("service_id", service_id);

        params.put("to_lat", String.valueOf(lat));

        params.put("to_lng", String.valueOf(lng));

        params.put("units", units);
        params.put("charge1", charge1);
        params.put("charge2", charge2);
        params.put("charge3", charge3);
        params.put("charge4", charge4);
        params.put("value", value);
        params.put("transaction_id", ref);        



        Connect.post(context.getResources().getString(R.string.finish_service), params, responseHandler);
    }

    public static void cancelService(Context context, String driver_id, AsyncHttpResponseHandler responseHandler) {
        Log.v("DRIVER_SERVICE", "        cancelService: ");

        Connect.post(context.getResources().getString(R.string.cancel_service, driver_id), null, responseHandler);
    }

    public static void loadHistory(Context context, String driver_id, String uuid, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("driver_id", driver_id);

        params.put("uuid", uuid);

        Log.v("DRIVER_SERVICE", "        loadHistory: ");

        Connect.post(context.getResources().getString(R.string.load_history), params, responseHandler);
    }

    public static void getAppVersions(Context context, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();

        params.put("app", "");

        Log.v("DRIVER_SERVICE", "        getAppVersions: ");

        Connect.post(context.getResources().getString(R.string.app_versions), params, responseHandler);
    }

    public static void checkStatusService(Context context, String driver_id, String service_id, String uuid, AsyncHttpResponseHandler responseHandler) throws JSONException {

        if (service_id != null && !service_id.isEmpty()) {
            Log.e("checkStatusService", "driver_id= " + driver_id + " service_id=" + service_id + " uuid=" + uuid);
        } else {
            Log.e("checkStatusService", "driver_id= " + driver_id + " service_id=" + "NULO" + " uuid=" + uuid);
        }

        RequestParams params = new RequestParams();

        params.put("driver_id", driver_id);
        //params.put("uuid", uuid);
        //Connect.post(context.getResources().getString(R.string.checkstatuservice), params, responseHandler);

        Log.v("DRIVER_SERVICE", "        checkStatusService: " + driver_id);

        if (service_id != null && !service_id.isEmpty()) {
            Log.e("checkStatusService", "service_id=" + service_id);
            params.put("driver_id", driver_id);
            params.put("service_id", service_id);
            Connect.post(context.getResources().getString(R.string.checkstatuservice), params, responseHandler);
        } else {
            Log.e("checkStatusService", "service_id= null");
            JSONObject user = new JSONObject();
            user.put("driver_id", driver_id);
            Log.e("DRIVER_ID", user.toString());
            Connect.sendJson(context, context.getResources().getString(R.string.checkstatuservice), user, responseHandler);
        }

    }

    public static void is_logued(Context context, String email, String uuid, AsyncHttpResponseHandler responseHandler) throws JSONException {

        Log.v("DRIVER_SERVICE", "        is_logued: ");

        JSONObject driver = new JSONObject();

        driver.put("login", email);

        driver.put("uuid", uuid);

        Log.e("DRIVER_SERVICE", driver.toString());

        Connect.sendJson(context, context.getResources().getString(R.string.is_logued), driver, responseHandler);

    }

    public static void testConnectivityQuality(AsyncHttpResponseHandler responseHandler){
        Log.v("USER_SERVICE", "        ConnectivityQualityTest: ");
        Connect.connectivityQualityTest(responseHandler);
    }

}
