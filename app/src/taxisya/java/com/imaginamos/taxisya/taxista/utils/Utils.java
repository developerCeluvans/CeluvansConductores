package com.imaginamos.taxisya.taxista.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private static final String PREFERENCE_KEY_VOICE_SPEED = "voice_speed";

    public Utils() {
    }

    public static final String md5(final String s) {
        try {

            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void showErrorDialog(int code, Activity activity) {
        GooglePlayServicesUtil.getErrorDialog(code, activity,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    public static boolean checkPlayServices(Activity activity) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

        if (status != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                Utils.showErrorDialog(status, activity);
            } else {
                Toast.makeText(activity, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
            }

            return false;

        }
        return true;
    }

    public static void saveVoiceSpeedPreference(float value, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(PREFERENCE_KEY_VOICE_SPEED, value);
        editor.commit();
    }

    public static float getVoiceSpeedPreference(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getFloat(PREFERENCE_KEY_VOICE_SPEED, 0.98F);
    }
}
