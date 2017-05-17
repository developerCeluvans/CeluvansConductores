package com.imaginamos.taxisya.taxista.io;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.activities.NotificationActivity;
import com.imaginamos.taxisya.taxista.model.Actions;

import org.json.JSONObject;

public class GCMIntentService extends GCMBaseIntentService {
    private static Context mContext;

    public GCMIntentService() {
        super(GooglePushNotification.SENDER_ID);
    }

    private static void generateNotification(Context context, String message) {

        mContext = context;

        try {
            String title = context.getString(R.string.app_name);

            NotificationCompat.Builder notification =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title).setContentText(message)
                            .setAutoCancel(true);

            Intent notificationIntent = new Intent(context, NotificationActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            notification.setContentIntent(intent);

            notification.setSound(Uri.parse("android.resource://"
                    + context.getPackageName() + "/"
                    + R.raw.audio2), 1);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(155200, notification.build());
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200);
        } catch (Exception e) {

            Log.e(TAG, "ERROR :" + e.toString());
        }
    }

    @Override
    protected void onError(Context context, String text) {
        Log.e(TAG, "ERROR :" + text);
    }

    @Override
    protected void onMessage(final Context context, Intent intent) {
        for (String key : intent.getExtras().keySet()) {
            Object value = intent.getExtras().get(key);
            Log.e(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        }
        Log.e(TAG, "RECIEVED A MESSAGE");
        final String message = intent.getExtras().getString("message");

        try {
            final JSONObject extra = new JSONObject(intent.getExtras().getString("extra"));
            Handler h = new Handler(Looper.getMainLooper());

            h.post(new Runnable() {
                public void run() {
                    try {
                        if (extra.has("push_type")) {
                            Log.e("PUSH", "push_type=" + String.valueOf(extra.getInt("push_type")));
                            switch (extra.getInt("push_type")) {
                                case Actions.TYPE_NEW_SERVICES:
                                    Log.v("SERVICE_CMS", "GCM TYPE_NEW_SERVICES");
                                    checkService(extra);
                                    break;

                                case Actions.SERVICE_CANCEL:
                                    Log.v("SERVICE_CMS", "GCM SERVICE_CANCEL service_id= " + extra.getString("service_id"));
                                    Intent mintent = new Intent(Actions.ACTION_SERVICE_CANCEL);
                                    mintent.putExtra("service_id", extra.getString("service_id"));
                                    sendBroadcast(mintent);
                                    break;

                                case Actions.USER_CANCELED_SERVICE:
                                    Log.v("SERVICE_CMS", "GCM USER_CANCELED_SERVICE service_id= " + extra.getString("service_id"));
                                    Intent i = new Intent(Actions.ACTION_USER_CANCELED_SERVICE);
                                    sendBroadcast(i);
                                    generateNotification(context, message);
                                    break;

                                case Actions.CANCEL_FOR_O:
                                    Log.v("SERVICE_CMS", "GCM CANCEL_FOR_O service_id= " + extra.getString("service_id"));
                                    Intent io = new Intent(Actions.ACTION_OPE_CANCELED_SERVICER);
                                    sendBroadcast(io);
                                    generateNotification(context, message);
                                    break;

                                case Actions.MESSAGE_MASSIVE:
                                    Log.e("PUSH", "Mensaje masivo");
                                    Log.e("PUSH", message);
                                    // se quita para la prueba de hoy
                                    Intent im = new Intent(Actions.ACTION_MESSAGE_MASSIVE);
                                    im.putExtra("message", message);
                                    sendBroadcast(im);
                                    generateNotification(context, message);
                                    break;

                                case Actions.DRIVER_CLOSE_SESSION:
                                    Log.e("PUSH", "Sesíón: cerrada por otro dispositivo");
                                    Intent ic = new Intent(Actions.ACTION_DRIVER_CLOSE_SESSION);
                                    sendBroadcast(ic);
                                    generateNotification(context, getString(R.string.gcmintentservice_mensaje_otro_dispositivo));
                                    break;

                                // default:
                                //     generateNotification(context, message);
                                //     break;
                            }
                        } else {
                            generateNotification(context, message);
                        }


                    } catch (Exception e) {
                        Log.e(TAG, "" + e.toString());
                        if (message != null && message != "") {
                            generateNotification(context, message);
                        }
                    }

                }


            });

        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }

    }

    private void checkService(JSONObject extra) {
        Log.e("getDistance", "checkService ");
        try {

            // if (extra.getJSONObject("service").getString("kind_id").equals("3") ||
            //         extra.getJSONObject("service").getString("kind_id").equals("2")) {
            //     Intent intent = new Intent(Actions.ACTION_NEW_SERVICES);
            //     intent.putExtra("user_name", extra.getString("user_name"));
            //     intent.putExtra("service", extra.getString("service"));
            //     sendBroadcast(intent);
            // }

            if (extra.getJSONObject("service").getString("kind_id").equals("3")) {
                Log.e("getDistance", "por obtener distancia ");
                if (getDistance(Double.parseDouble(extra.getJSONObject("service").getString("lat")), Double.parseDouble(extra.getJSONObject("service").getString("lng")), MyService.latitud, MyService.longitud) <= MyService.getMeters()) {
                    Log.e("getDistance", "dentro de distancia kind_id 3 - operadora");
                    Intent intent = new Intent(Actions.ACTION_NEW_SERVICES);
                    intent.putExtra("user_name", extra.getString("user_name"));
                    intent.putExtra("service", extra.getString("service"));
                    sendBroadcast(intent);
                }
            }
            else if (extra.getJSONObject("service").getString("kind_id").equals("2")) {
                Intent intent = new Intent(Actions.ACTION_NEW_SERVICES);
                intent.putExtra("user_name", extra.getString("user_name"));
                intent.putExtra("service", extra.getString("service"));
                sendBroadcast(intent);
            }
            else {
                Log.e("getDistance", "por obtener distancia ");
                if (getDistance(Double.parseDouble(extra.getJSONObject("service").getString("lat")), Double.parseDouble(extra.getJSONObject("service").getString("lng")), MyService.latitud, MyService.longitud) <= MyService.getMeters()) {
                    Log.e("getDistance", "dentro de distancia kind_id 1 - normal");
                    Intent intent = new Intent(Actions.ACTION_NEW_SERVICES);
                    intent.putExtra("user_name", extra.getString("user_name"));
                    intent.putExtra("service", extra.getString("service"));
                    sendBroadcast(intent);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }

    }

    private float getDistance(double latA, double lngA, double latB, double lngB) {
        Log.e(TAG, "getDistance:" + latA + "," + lngA + " <->" + latB + "," + lngB);
        Log.e("getDistance", "getDistance:" + latA + "," + lngA + " <->" + latB + "," + lngB);
        Location locationA = new Location("point A");
        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);
        Location locationB = new Location("point B");
        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);
        Log.e(TAG, "locationA.distanceTo(locationB):" + locationA.distanceTo(locationB));
        Log.e("getDistance", "locationA.distanceTo(locationB):" + locationA.distanceTo(locationB));
        return locationA.distanceTo(locationB);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.e(TAG, "Device registered: regId = " + registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.e(TAG, "Device unregistered: regId = " + registrationId);
    }

}
