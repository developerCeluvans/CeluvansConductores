package com.imaginamos.taxisya.taxista.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("onCreate", "NotificationActivity");

        if (isTaskRoot()) {

        }

        finish();
    }
}
