package com.imaginamos.taxisya.taxista.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.io.Connectivity;
import com.imaginamos.taxisya.taxista.io.GPSTracker;
import com.imaginamos.taxisya.taxista.io.MiddleConnect;
import com.imaginamos.taxisya.taxista.io.MyService;
import com.imaginamos.taxisya.taxista.io.UpdateReceiver;
import com.imaginamos.taxisya.taxista.model.Actions;
import com.imaginamos.taxisya.taxista.model.Conf;
import com.imaginamos.taxisya.taxista.model.Servicio;
import com.imaginamos.taxisya.taxista.utils.BDAdapter;
import com.imaginamos.taxisya.taxista.utils.Dialogos;
import com.imaginamos.taxisya.taxista.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class WaitServiceActivity extends Activity implements OnClickListener, UpdateReceiver.UpdateReceiverListener, Connectivity.ConnectivityQualityCheckListener
        ,
        TextToSpeech.OnInitListener {
    private static boolean inBackground = false;
    private static boolean isServiceInProgress = false;
    private String uuid;
    private String driver_id;
    private String full_dir = "";
    private ViewGroup mContainerView;
    private ProgressDialog pDialog;
    private SeekBar mSeekBar;
    private float mVoiceSpeed = 0.98F;
    private String[] mNumbers;
    private ImageView volver;
    private Button btnDeshabilitar;
    private TextToSpeech tts;
    private TextView mPayTypeTextView;
    private Intent intent_service;
    private Locale loc;
    private String name = "";
    private JSONArray services_init;
    private BroadcastReceiver mReceiver;

    private ArrayList<ViewGroup> services;
    private List<String> mVisibleServices;

    private Conf conf;
    private AlertDialog.Builder builder;

    private PowerManager.WakeLock mWakeLock;
    private double lat, lng = 0;

    private Timer myTimer = new Timer();
    private Timer myTimerSpeak = new Timer();

    private Integer recibe_push = 0;

    private String mServiceId;
    private int mStatusOld;
    private int mStatusNew;

    private BDAdapter mySQLiteAdapter;
    private Cursor mCursor;

    private long mLastClickTime = 0;

    PendingIntent mPendingIntent;
    BroadcastReceiver mBroadcastReceiver;
    AlarmManager mAlarmManager;

    private UpdateReceiver mNetworkMonitor;
    private ImageView mConnectivityLoaderImage;
    private RelativeLayout mNoConnectivityPanel;
    private Connectivity connectivityChecker = new Connectivity(this);


    @Override
    public void onRestart() {

        super.onRestart();
        Log.v("onRestart", "WaitServiceActivity");
        overridePendingTransition(R.anim.hold, R.anim.pull_out_to_right);
        /*
         * GPSTracker gps = new GPSTracker(this); if (gps.canGetLocation()) {
		 * lat = gps.getLatitude(); lng = gps.getLongitude(); }
		 * gps.stopUsingGPS();
		 *
		 * enable(driver_id,uuid);
		 */

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String status = sharedPref.getString("service", "");
        Log.v("onActivityResult", "WaitServiceActivity = status " + status);
        if (status != null && !TextUtils.isEmpty(status)) {
            if (status.equals("ended")) {
                GPSTracker gps = new GPSTracker(this);
                if (gps.canGetLocation()) {
                    lat = gps.getLatitude();
                    lng = gps.getLongitude();
                }
                gps.stopUsingGPS();

                isServiceInProgress = false;

                enable(driver_id, uuid);

                // enableService();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("onStop", "WaitServiceActivity");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("onCreate", "WaitServiceActivity");
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("service", "espera");
        editor.commit();

        conf = new Conf(this);

        // open sqlite
        mySQLiteAdapter = new BDAdapter(this);
        mySQLiteAdapter.openToWrite();

        mVisibleServices = new ArrayList<String>();


        mNumbers = getResources().getStringArray(R.array.waitservice_numbers);

        try {
            overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

            loc = Locale.getDefault();

        } catch (Exception e) {
        }

        IntentFilter intentFilter = new IntentFilter();

// test servicio alternativo

        intentFilter.addAction(Actions.ACTION_NEW_SERVICES);

        intentFilter.addAction(Actions.ACTION_SERVICE_CANCEL);

        intentFilter.addAction(Actions.NO_NET);

        intentFilter.addAction(Actions.YES_NET);

        intentFilter.addAction(Actions.ACTION_DRIVER_CLOSE_SESSION);

        intentFilter.addAction(Actions.ACTION_MESSAGE_MASSIVE);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String value = intent.getAction();

                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                Log.e("PUSH", "WaitServiceActivity - onReceive() value = " + value);
                Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - onReceive() value = " + value);

                if (value.equals(Actions.ACTION_NEW_SERVICES)) {
                    recibe_push = 0;
                    Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - ACTION_NEW_SERVICES");

                    try {
                        Log.v("WaitServiceActivity", "Action NEW_SERVICES ok " + String.valueOf(new Date()));
                        Intent i = new Intent(getApplicationContext(),
                                NotificationActivity.class);

                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        getApplication().startActivity(i);

                        setService(intent.getExtras().getString("service"),
                                intent.getExtras().getString("user_name"));

                    } catch (Exception e) {
                        Log.e("ERROR", "" + e.toString());
                        Log.v("WaitServiceActivity", "Action NEW_SERVICES bad " + String.valueOf(new Date()));

                    }

                } else if (value.equals(Actions.ACTION_SERVICE_CANCEL)) {
                    recibe_push = 0;
                    Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - ACTION_SERVICE_CANCEL");

                    Log.e("ESTEBAAN", intent.getExtras()
                            .getString("service_id"));
                    Log.v("WaitServiceActivity", "Action SERVICE_CANCEL " + String.valueOf(new Date()));

                    deleteService(intent.getExtras().getString("service_id"));

                    mySQLiteAdapter.updateStatusService(intent.getExtras().getString("service_id"), "7");


                } else if (value.equals(Actions.NO_NET)) {
                    Log.v("WaitServiceActivity", "Action NO NET " + String.valueOf(new Date()));
                    Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - ACTION_NO_NET");

                    // showDialog("Hubo un error en su conexión a internet! En este momento no recibirá servicios");
                    // enviar enable service
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.waitservice_errot_conexion,
                            Toast.LENGTH_LONG).show();
                } else if (value.equals(Actions.YES_NET)) {
                    Log.v("WaitServiceActivity", "Action YES NET " + String.valueOf(new Date()));

                } else if (value.equals(Actions.ACTION_DRIVER_CLOSE_SESSION)) {
                    Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - ACTION_DRIVER_CLOSE_SESSION");

                    Log.v("DRIVER_CLOSE_SESSION", "close ");
                    Log.v("WaitServiceActivity", "Action DRIVER_CLOSE_SESSION " + String.valueOf(new Date()));

                    Toast.makeText(getApplicationContext(),
                            R.string.waitservice_se_deshabilito_otro_dispositivo,
                            Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);

                    conf.setPass(null);
                    conf.setUser(null);

                    startActivity(i);
                    Log.v("finish", "WaitServiceActivity onCreate");
                    finish();

                } else if (value.equals(Actions.ACTION_MESSAGE_MASSIVE)) {
                    Log.v("TAXISTA_SRV_PUSH", "WaitServiceActivity - ACTION_MESSAGE_MASSIVE");
                    Log.v("WaitServiceActivity", "Action MESSAGE_MASSIVE " + String.valueOf(new Date()));

                    Log.v("MESSAGE_MASSIVE", "mensaje global recibido");
                    String message = intent.getExtras().getString("message");
                    mostrarMensaje(message);

                }
            }

        };

        try {
            registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            Log.e("ERROR", "" + e.toString());
        }

        conf = new Conf(this);

        driver_id = conf.getIdUser();

        uuid = conf.getUuid();

        buildView();

        mySQLiteAdapter.deleteServices();

        mVisibleServices.clear();

        if (intent_service != null) {
            stopService(intent_service);

            intent_service = null;

        } else if (intent_service == null && !isMyServiceRunning()) {
            intent_service = new Intent(this, MyService.class);
            intent_service.putExtra("driver_id", driver_id);
            intent_service.putExtra("uuid", uuid);

            startService(intent_service);
        }

        tts = new TextToSpeech(this, this);

        services = new ArrayList<ViewGroup>();

        if (getIntent().getExtras().containsKey("name")) {
            name = getIntent().getExtras().getString("name");
        }

        if (getIntent().getExtras().containsKey("services")) {
            try {

                services_init = new JSONArray(getIntent().getExtras().getString("services"));

                for (int i = 0; i < services_init.length(); i++) {
                    setService(services_init.getString(i));
                }

            } catch (Exception e) {
                Log.e("ERROR", "" + e.toString());
            }
        }

        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.imaginamos.taxisya.taxista"), 0);
        mAlarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));

        setup();

        recibe_push = 0;
        // timer
        validateStatusDriver();

        servicesToSpeek();

    }


    private void setup() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                //Toast.makeText(c, "Rise and Shine!", Toast.LENGTH_LONG).show();
                Log.v("ALARMA_ACTIVA", "se lanzo la alarma");
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter("com.imaginamos.taxisya.taxista"));
        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.imaginamos.taxisya.taxista"), 0);
        mAlarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }

    void mostrarMensaje(final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.waitservice_titulo_dialogo);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("PUSH", "mensaje: " + message);
                    }
                });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    private void setService(String service_json) {
        try {

            JSONObject service = new JSONObject(service_json);
            Integer agendamiento = 0;
            String destino = "";
            String hora = "";

            // !service.getString("schedule_type").isEmpty()
            if (service.getString("schedule_type") != null
                    && !TextUtils.isEmpty(service.getString("schedule_type"))) {
                agendamiento = Integer.parseInt(service
                        .getString("schedule_type"));
            }
            // Integer.parseInt(service.getString("schedule_type")),
            if ((agendamiento == 2) || (agendamiento == 3)) {
                if (service.getString("destination") != null
                        && !TextUtils.isEmpty(service.getString("destination"))) {
                    destino = "Destino: " + service.getString("destination");
                }
            }

            if (service.getString("service_date_time") != null
                    && !TextUtils.isEmpty(service
                    .getString("service_date_time"))) {
                hora = service.getString("service_date_time");
            }
            Log.v("ADDRESS1", "direccion " + service.getString("address"));

            // mServiceId = service.getString("service_id");
            // Log.v("VALIDATE_SERVICE", "CHECK service service_id " + mServiceId);
            // mCursor = mySQLiteAdapter.filterService(mServiceId);
            // if (mCursor.getCount() > 0) {
            //     mCursor.moveToPosition(0);
            //     Log.v("VALIDATE_SERVICE", "NO ADD SERVICE ");
            //     mCursor.close();
            // }
            // else {
            // Log.v("VALIDATE_SERVICE", "ADD SERVICE ");
            int kindId = Integer.parseInt(service.getString("kind_id"));

            if (kindId == 2) {
                if (getDistance(Double.parseDouble(service.getString("lat")),
                                Double.parseDouble(service.getString("lng")),
                                MyService.latitud,
                                MyService.longitud) <= MyService.getMeters()) {
                    addNewService(new Servicio(
                            service.getString("service_id"), service.getString("index_id"),
                            service.getString("comp1"), service.getString("comp2"),
                            service.getString("no"), service.getString("barrio"),
                            service.getString("obs"), service.getString("lat"),
                            service.getString("lng"), agendamiento,
                            service.getString("username"), Integer.parseInt(service
                            .getString("kind_id")), destino, hora, service.getString("address"),
                            Integer.valueOf(service.getString("pay_type")),
                            service.getString("pay_reference"),
                            service.getString("user_id"),
                            service.getString("user_email"),
                            service.getString("user_card_reference"),
                            service.getString("units"),
                            service.getString("charge1"),
                            service.getString("charge2"),
                            service.getString("charge3"),
                            service.getString("charge4"),
                            service.getString("value")
                            ));
                }
            }
            else {

                if (getDistance(Double.parseDouble(service.getString("lat")),
                                Double.parseDouble(service.getString("lng")),
                                MyService.latitud,
                                MyService.longitud) <= MyService.getMeters()) {
                    addNewService(new Servicio(
                        service.getString("service_id"), service.getString("index_id"),
                        service.getString("comp1"), service.getString("comp2"),
                        service.getString("no"), service.getString("barrio"),
                        service.getString("obs"), service.getString("lat"),
                        service.getString("lng"), agendamiento,
                        service.getString("username"), Integer.parseInt(service
                        .getString("kind_id")), destino, hora, service.getString("address"),
                            Integer.valueOf(service.getString("pay_type")),
                            service.getString("pay_reference"),
                            service.getString("user_id"),
                            service.getString("user_email"),
                            service.getString("user_card_reference"),
                            service.getString("units"),
                            service.getString("charge1"),
                            service.getString("charge2"),
                            service.getString("charge3"),
                            service.getString("charge4"),
                            service.getString("value")));
                 }
            }
            // }
        } catch (Exception e) {
            Log.e("ERROR", "error con el servicio" + e.toString());
        }
    }

    private void setService(String service_json, String username) {

        try {
            JSONObject service = new JSONObject(service_json);
            Integer agendamiento = 0;
            String destino = "";
            String hora = "";

            // !service.getString("schedule_type").isEmpty()
            if (service.getString("schedule_type") != null
                    && !TextUtils.isEmpty(service.getString("schedule_type"))) {
                agendamiento = Integer.parseInt(service
                        .getString("schedule_type"));
            }
            // Integer.parseInt(service.getString("schedule_type")),
            if ((agendamiento == 2) || (agendamiento == 3)) {
                if (service.getString("destination") != null
                        && !TextUtils.isEmpty(service.getString("destination"))) {
                    destino = "Destino: " + service.getString("destination");
                }
            }
            if (service.getString("service_date_time") != null
                    && !TextUtils.isEmpty(service
                    .getString("service_date_time"))) {
                hora = service.getString("service_date_time");
            }

            // mServiceId = service.getString("service_id");
            // Log.v("SQLITE", "2 CHECK service service_id " + mServiceId);
            // mCursor = mySQLiteAdapter.filterService(mServiceId);
            // if (mCursor.getCount() > 0) {
            //     mCursor.moveToPosition(0);
            //     Log.v("SQLITE", "2 NO ADD SERVICE ");
            //     mCursor.close();
            // }
            // else {
            // Log.v("SQLITE", "2 ADD SERVICE ");
            int kindId = Integer.parseInt(service.getString("kind_id"));
            if (kindId == 2) {
                if (getDistance(Double.parseDouble(service.getString("lat")),
                                Double.parseDouble(service.getString("lng")),
                                MyService.latitud,
                                MyService.longitud) <= MyService.getMeters()) {
                     addNewService(new Servicio(
                         service.getString("service_id"), service.getString("index_id"),
                         service.getString("comp1"), service.getString("comp2"),
                         service.getString("no"), service.getString("barrio"),
                         service.getString("obs"), service.getString("lat"),
                         service.getString("lng"), agendamiento, username,
                             Integer.parseInt(service.getString("kind_id")), destino,
                             hora, service.getString("address"),
                             Integer.valueOf(service.getString("pay_type")),
                             service.getString("pay_reference"),
                             service.getString("user_id"),
                             service.getString("user_email"),
                             service.getString("user_card_reference"),
                             service.getString("units"),
                             service.getString("charge1"),
                             service.getString("charge2"),
                             service.getString("charge3"),
                             service.getString("charge4"),
                             service.getString("value")));
                }
            }
            else {
                if (getDistance(Double.parseDouble(service.getString("lat")),
                                Double.parseDouble(service.getString("lng")),
                                MyService.latitud,
                                MyService.longitud) <= MyService.getMeters()) {
                    addNewService(new Servicio(
                            service.getString("service_id"), service.getString("index_id"),
                            service.getString("comp1"), service.getString("comp2"),
                            service.getString("no"), service.getString("barrio"),
                            service.getString("obs"), service.getString("lat"),
                            service.getString("lng"), agendamiento, username,
                            Integer.parseInt(service.getString("kind_id")), destino,
                            hora, service.getString("address"),
                            Integer.valueOf(service.getString("pay_type")),
                            service.getString("pay_reference"),
                            service.getString("user_id"),
                            service.getString("user_email"),
                            service.getString("user_card_reference"),
                            service.getString("units"),
                            service.getString("charge1"),
                            service.getString("charge2"),
                            service.getString("charge3"),
                            service.getString("charge4"),
                            service.getString("value")));
                }
            }
            // }
        } catch (Exception e) {
            Log.e("ERROR", "error con el servicio" + e.toString());
        }

    }

    private float getDistance(double latA, double lngA, double latB, double lngB) {
       // Log.e(TAG, "getDistance:" + latA + "," + lngA + " <->" + latB + "," + lngB);
        Log.e("getDistance", "getDistance:" + latA + "," + lngA + " <->" + latB + "," + lngB);
        Location locationA = new Location("point A");
        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);
        Location locationB = new Location("point B");
        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);
        //Log.e(TAG, "locationA.distanceTo(locationB):" + locationA.distanceTo(locationB));
        Log.e("getDistance", "locationA.distanceTo(locationB):" + locationA.distanceTo(locationB));
        return locationA.distanceTo(locationB);
    }

    private void buildView() {

        setContentView(R.layout.activity_waitservice);

        volver = (ImageView) findViewById(R.id.btn_volver);

        volver.setOnClickListener(this);

        btnDeshabilitar = (Button) findViewById(R.id.btnDeshabilitar);

        btnDeshabilitar.setOnClickListener(this);

        mNoConnectivityPanel = (RelativeLayout) findViewById(R.id.layout_no_connectivity);
        mConnectivityLoaderImage = (ImageView) findViewById(R.id.loader_icon);

        mContainerView = (ViewGroup) findViewById(R.id.content_services);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mVoiceSpeed = Utils.getVoiceSpeedPreference(getApplicationContext());

        if(!mSeekBar.isIndeterminate())
            mSeekBar.setProgress((int) (mVoiceSpeed*100));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                float value = (float)progresValue / 100;
                Log.v("SEEKBAR", "progress " + String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
                mVoiceSpeed = (float)seekBar.getProgress() / 100;
                testSpeak();
                Utils.saveVoiceSpeedPreference(mVoiceSpeed, getApplicationContext());
            }
        });
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {

        super.onPause();
        connectivityChecker.stopConnectivityMonitor();
        unregisterReceiver(mNetworkMonitor);
//        ((MyApplication)this.getApplication()).startActivityTransitionTimer();
//        Log.v("MY_APP","onPause en background");

    }

    private void displayConnectivityPanel(boolean display){
        mNoConnectivityPanel.setVisibility(display ? View.VISIBLE:View.GONE);
        if(display)
            mConnectivityLoaderImage.startAnimation(AnimationUtils.loadAnimation(this,R.anim.connection_loader));
    }

    public void onResume() {

        super.onResume();
        displayConnectivityPanel(!Connectivity.isConnected(this) && !connectivityChecker.getConnectivityCheckResult());
        connectivityChecker.startConnectivityMonitor();
        mNetworkMonitor = new UpdateReceiver(this);
        registerReceiver(mNetworkMonitor, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        MyApplication myApp = (MyApplication)this.getApplication();
//        if (myApp.wasInBackground)
//        {
//            //Do specific came-here-from-background code
//            Log.v("MY_APP","despierta");
//        }
//
//        myApp.stopActivityTransitionTimer();
//
//        if (inBackground) {
//            // You just came from the background
//            inBackground = false;
//        }
//        else {
//            // You just returned from another activity within your own app
//        }
    }

    @Override
    public void onUserLeaveHint() {
        Log.w("APP_TAX_DRIVER", "onUserLeaveHint en background 1");
        Log.w("ALARMA_ACTIVA", "onUserLeaveHint en background 1");

        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                300000, mPendingIntent);
        inBackground = true;
        super.onUserLeaveHint();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (!moveTaskToBack(true)) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(i);

            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_volver:
                Desabilitar_service();
                break;

            case R.id.btnDeshabilitar:
                Desabilitar_service();
                break;
        }

    }

    @Override
    public void onDestroy() {
        Log.v("onDestroy", "WaitServiceActivity");
        if (tts != null) {
            Log.v("onDestroy", "stop WaitServiceActivity stop tts");
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        Log.v("ALARMA_ACTIVA", "onDestroy");

        mAlarmManager.cancel(mPendingIntent);

        //if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        //}
        unregisterReceiver(mBroadcastReceiver);

        if (mVisibleServices != null) {
            mVisibleServices = null;
        }

        if (mySQLiteAdapter != null) {
            Log.v("onDestroy", "WaitServiceActivity clear all services");

            mySQLiteAdapter.deleteAllServices();

            mySQLiteAdapter.close();
        }

        if (myTimer != null) {
            Log.v("onDestroy", "WaitServiceActivity stop myTimer");
            myTimer.cancel();
            myTimer.purge();
            myTimer = null;
        }

        if (myTimerSpeak != null) {
            Log.v("onDestroy", "WaitServiceActivity stop myTimerSpeak");
            myTimerSpeak.cancel();
            myTimerSpeak.purge();
            myTimerSpeak = null;
        }

        // Desabilitar_service();
        // this.mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        Log.v("SPEAK1","onInit init");
        if (status == TextToSpeech.SUCCESS) {
            Locale locSpanish = Locale.getDefault();

            int result = tts.setLanguage(locSpanish);

            tts.setPitch(1);

            tts.setSpeechRate(mVoiceSpeed);

            if (result != TextToSpeech.LANG_MISSING_DATA || result != TextToSpeech.LANG_NOT_SUPPORTED) {
                try {
                    Log.v("SPEAK1","onInit se pronuncio");
                    tts.speak(getString(R.string.ready_enable, name), TextToSpeech.QUEUE_FLUSH, null);
                } catch (Exception e) {
                    Log.e("error", "" + e.toString());
                    Log.v("SPEAK1","onInit catch se pronuncio");
                }
            }
        } else {
            Log.e("TTS", "Initilization Failed");
            Log.v("SPEAK1", "onInit Initialization Failed");
        }

    }

    private void speakOut(String service_id, String texto, int pt) {

        try {

         //   JSONObject service = new JSONObject(service_json);
            String texto1 = "" ;

            if (pt == 2)

            {
                texto1 = " pago tarjeta" ;

            }
             else

            {

               texto1 = "";
            }

            if (tts != null) {
                if (!tts.isSpeaking()) {
                    Locale loc = Locale.getDefault();


                    texto = texto.toLowerCase(loc) + texto1  ;

                    Log.e("LECTURA ", texto);

                    texto = parseAddresToSpeak(texto).toString();

                    Log.e("LECTURA2 ", texto);
                    Log.v("SERVICE_SPEAK", "addNewService se reproducira ");

                    mySQLiteAdapter.updateAddressService(service_id, texto);
                    mySQLiteAdapter.updateSpokenService(service_id, "1");

                    tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                } else { // esta hablando por lo que la almacena
                    Locale loc = Locale.getDefault();

                    texto = texto.toLowerCase(loc);

                    texto = parseAddresToSpeak(texto).toString();

                    Log.v("SERVICE_SPEAK", "addNewService se almacena ");

                    mySQLiteAdapter.updateAddressService(service_id, texto);

                }
            }

        } catch (Exception e) {
        }

    }

    public void testSpeak() {
        try {

            if (tts != null) {
                if (!tts.isSpeaking()) {
                    Log.v("TEST_SPEAK", "testSpeak ");

                    tts.setSpeechRate(mVoiceSpeed);

                    tts.speak(getString(R.string.ready_enable, name), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        } catch (Exception e) {
        }
    }

    private String parseAddresToSpeak(String address) {
        //private List<String> parseAddresToSpeak(String address) {

        List<String> output = new ArrayList<String>();

        address = address.replace("bis", " bis ");

        address = address.replace("-", " ");

        address = address.replace("#", getString(R.string.waitservice_texto_hablado_numero));

        // separa números de letras
        Matcher match = Pattern.compile("[0-9]+|[a-z]+|[A-Z]+").matcher(address);
        while (match.find()) {
            output.add(match.group());
        }
        if (output.size() > 2) {
            int position = 2;
            String strPrefix = output.get(1);
            if (strPrefix.equals("av")) {
                output.set(1, "avenida");
                if (output.size() >= 3) position = 3;
            }
            String strNumber = output.get(position);
            // chequea si este componente es un número
            if (isValidInteger(strNumber)) {
                // si esta entre 1 y 10  cambiar cadena
                int number = Integer.parseInt(strNumber);
                if (number <= 10 && number > 0) {
                    strNumber = mNumbers[number - 1];
                    output.set(position, strNumber);
                }
            }
        }

        String strOutput = TextUtils.join(" ", output);
        //return output;
        return strOutput;
    }

    public static Boolean isValidInteger(String value) {
        try {
            Integer val = Integer.valueOf(value);
            if (val != null)
                return true;
            else
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void Desabilitar_service() {

        try {
            stopService(intent_service);
        } catch (Exception e) {
        }

        deshabilitarme();
    }

    private void err_desabilitar() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(),
                getString(R.string.error_disable), Toast.LENGTH_SHORT).show();
    }

    private void deshabilitarme() {
        this.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        this.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        MiddleConnect.disableDrive(this, driver_id, uuid,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        try {
                            pDialog = new ProgressDialog(WaitServiceActivity.this);
                            pDialog.setMessage(getString(R.string.waitservice_titulo_deshabilitando));
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            pDialog.show();
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        try {
                            JSONObject responsejson = new JSONObject(response);

                            if (responsejson.getBoolean("success")) {
                                Log.v("finish", "WaitServiceActivity deshabilitarme()");
                                finish();
                            } else {
                                err_desabilitar();
                            }

                        } catch (Exception e) {
                            err_desabilitar();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String response = new String(responseBody);
                        Log.e("ERROR", "" + response);
                        err_desabilitar();
                    }

                    @Override
                    public void onFinish() {
                        try {
                            pDialog.dismiss();
                        } catch (Exception e) {
                            Log.e("ERROR", e.toString() + "");
                        }
                    }

                });
    }

    private void enable(String driver_id, String id) {
        Log.v("WaitServiceActivity", "driver_id=" + driver_id + " uuid=" + id);
        Log.v("WaitServiceActivity", "MyService.lat=" + String.valueOf(MyService.latitud) + " MyService.longitud=" + String.valueOf(MyService.longitud));

        lat = MyService.latitud;
        lng = MyService.longitud;

        //clearAllServices();
        Log.v("VALIDATE_SERVICE", "enable");


        // elimina todos los servicios de operadora almacenados
//        mySQLiteAdapter.deleteServicesOperator();

        // remove services operator in erray
        this.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        this.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        MiddleConnect.enableDrive(this, lat, lng, driver_id, id,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        try {
                            pDialog = new ProgressDialog(WaitServiceActivity.this);
                            pDialog.setMessage(getString(R.string.enable));
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            pDialog.show();
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        Log.e("WaitServiceActivity", "onSuccess" + response);
                        Log.v("VALIDATE_SERVICE1", "enable onSuccess try " + response);

                        try {

                            JSONObject respJson = new JSONObject(response);

                            Log.e("WaitServiceActivity", "antes de responsejson" + respJson.toString());

                            if (respJson.getBoolean("success")) {
                                Log.v("VALIDATE_SERVICE1", "enable onSuccess in try " + response);


                                Log.e("WaitServiceActivity", "responsejson" + respJson.toString());
                                Log.e("WaitServiceActivity", "en respJson hay services");

// TEST
                                // clearAllServices();
                                // mVisibleServices.clear();

                                // services = null;
                                // services = new ArrayList<ViewGroup>();

                                Log.e("WaitServiceActivity", "se limpiaron las views");

                                List<String> receivedServices = new ArrayList<String>();

                                try {
                                    Log.v("VALIDATE_SERVICE3", "ver si hay services try " + response);

                                    //clearAllServices();
                                    mVisibleServices.clear();
                                    //services = null;
                                    //services = new ArrayList<ViewGroup>();
                                    //mContainerView = (ViewGroup) findViewById(R.id.content_services);

                                    services_init = new JSONArray(respJson.getString("services"));

                                    // obtener los ids de los servicios
                                    for (int i = 0; i < services_init.length(); i++) {
                                        // obtiene el id
                                        JSONObject serviceJson = new JSONObject(services_init.getString(i));


                                        // ver si esta dentro de la distancia +

                                        if (getDistance(Double.parseDouble(serviceJson.getString("lat")),
                                                        Double.parseDouble(serviceJson.getString("lng")),
                                                        MyService.latitud, MyService.longitud) <= MyService.getMeters()) {
                                            receivedServices.add(serviceJson.getString("service_id"));

                                        }

                                        // ver si esta dentro de la distancia -
                                        //receivedServices.add(serviceJson.getString("service_id"));

                                    }

                                    Log.v("VALIDATE_SERVICE1", "receivedServices " + receivedServices.toString());

                                    Log.v("VALIDATE_SERVICE1", "mVisibleServices " + mVisibleServices.toString());

                                    Set<String> set1 = new HashSet<String>();
                                    set1.addAll(receivedServices);

                                    Set<String> set1a = new HashSet<String>();
                                    set1a.addAll(receivedServices);


                                    Set<String> set2 = new HashSet<String>();
                                    set2.addAll(mVisibleServices);

                                    set1.removeAll(set2);
                                    set2.removeAll(set1a);

                                    Log.v("VALIDATE_SERVICE3", "mVisibleServices not in receivedServices" + set1.toString());
                                    Log.v("VALIDATE_SERVICE3", "mVisibleServices not in receivedServices" + set2.toString());

                                    for (String diffElement : set2) {
                                        //System.out.println(diffElement.toString());
                                        Log.v("VALIDATE_SERVICE3", "   delete " + diffElement.toString());
                                        deleteService(diffElement.toString());

                                        mVisibleServices.remove(diffElement.toString());
                                    }

                                    for (int i = 0; i < services_init.length(); i++) {
                                        Log.e("WaitServiceActivity", "servicio individual " + services_init.getString(i));
                                        setService(services_init.getString(i));
                                    }
                                    //mContainerView.invalidate();
                                    Log.e("WaitServiceActivity", "total servicios - invalidate ");
                                } catch (Exception e) {
                                    Log.e("WaitServiceActivity", "" + e.toString());
                                    Log.v("VALIDATE_SERVICE4", "ver si hay services catch " + response);
//                                  clearAllServices();
//                                  mVisibleServices.clear();

                                    services = null;
                                    services = new ArrayList<ViewGroup>();

                                    // vino un servicio recuperado
                                    try {
                                        JSONObject serviceJson = respJson.getJSONObject("service");
                                        Log.v("VALIDATE_SERVICE4", "servicio recuperado " + serviceJson.toString());

                                        // servicio recuperado +
                                        int status_id = serviceJson.getInt("status_id");
                                        Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - success " + response);

                                        if ((status_id == 2) || (status_id == 4)) {
                                            savePreferencias("servicieTomado", "true");

                                            Intent intent = new Intent(WaitServiceActivity.this, MapActivity.class);

                                            intent.putExtra("lng", Double.parseDouble(serviceJson.getString("from_lat")));
                                            intent.putExtra("lat", Double.parseDouble(serviceJson.getString("from_lng")));

                                            intent.putExtra("id_servicio", serviceJson.getString("service_id"));

                                            conf.setServiceId(serviceJson.getString("service_id"));

                                            String type = String.valueOf(serviceJson.getString("schedule_type"));
                                            String direccion = "";
                                            if ((type.equals("1")) || (type.equals("2")) || (type.equals("3"))) {

                                                // 012345678901234567
                                                // yyyy-MM-dd hh:mm:ss"
                                                String serviceDateTime = serviceJson.getString("service_date_time");
                                                String substr = serviceDateTime.substring(11, 16);
                                                if (serviceJson.getString("address") != null) {
                                                    direccion = serviceJson.getString("address") + getString(R.string.waitservice_texto_barrio2)
                                                            + serviceJson.getString("barrio") + "\n"
                                                            + serviceJson.getString("destination") + getString(R.string.waitservice_texto_hora)
                                                            + substr;
                                                } else {
                                                    direccion = serviceJson.getString("index_id") + " - "
                                                            + serviceJson.getString("comp1") + " # "
                                                            + serviceJson.getString("comp2") + " - "
                                                            + serviceJson.getString("no") + " "
                                                            + serviceJson.getString("obs") + getString(R.string.waitservice_texto_barrio2)
                                                            + serviceJson.getString("barrio") + "\n"
                                                            + serviceJson.getString("destination") + getString(R.string.waitservice_texto_hora)
                                                            + substr;
                                                }

                                            } else {
                                                Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - address " + response);
                                                String stringAddress = serviceJson.getString("address");

                                                if (stringAddress != null && !TextUtils.isEmpty(stringAddress)) {
                                                    //if (serviceJson.getString("address") != null) {

                                                    direccion = serviceJson.getString("address") + getString(R.string.waitservice_texto_barrio2)
                                                            + serviceJson.getString("barrio");

                                                } else {
                                                    direccion = serviceJson.getString("index_id") + " - "
                                                            + serviceJson.getString("comp1") + " # "
                                                            + serviceJson.getString("comp2") + " - "
                                                            + serviceJson.getString("no") + " "
                                                            + serviceJson.getString("obs") + getString(R.string.waitservice_texto_barrio2)
                                                            + serviceJson.getString("barrio");
                                                }
                                            }
                                            intent.putExtra("direccion", direccion);
                                            intent.putExtra("kind_id", serviceJson.getInt("kind_id"));
                                            intent.putExtra("schedule_type", serviceJson.getInt("schedule_type"));
                                            intent.putExtra("name", serviceJson.getString("index_id"));

                                            intent.putExtra("pay_type", serviceJson.getString("pay_type"));
                                            intent.putExtra("user_id", serviceJson.getString("user_id"));
                                            intent.putExtra("user_card_reference", serviceJson.getString("user_card_reference"));
                                            intent.putExtra("user_email", serviceJson.getString("user_email"));


                                            // kill speak
                                            if (myTimerSpeak != null) {
                                                Log.v("wait_service", "stop myTimerSpeak");
                                                myTimerSpeak.cancel();
                                                myTimerSpeak.purge();
                                                myTimerSpeak = null;
                                            }

                                            if (tts != null) {
                                                Log.v("wait_service", "stop tts");
                                                tts.stop();
                                                tts.shutdown();
                                                tts = null;
                                            }

                                            startActivity(intent);
                                            Log.v("finish", "WaitServiceActivity sendConfirmation()");
                                            finish();
                                        }

                                        // servicio recuperado -
                                    } catch (Exception e2) {
                                        Log.v("VALIDATE_SERVICE4", "no tenia servicio en curso");
                                    }

                                }

                            } else {
                                err_enable();
                            }
                        } catch (Exception e) {
                            Log.v("VALIDATE_SERVICE1", "enable onSuccess catch " + e.toString());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String response = new String(responseBody);
                        Log.v("VALIDATE_SERVICE", "enable onFailure " + response);
                        Log.e("WaitServiceActivity", "onFailure" + response);
                        err_enable();
                    }

                    @Override
                    public void onFinish() {
                        try {
                            pDialog.dismiss();
                        } catch (Exception e) {
                        }
                        Log.e("WaitServiceActivity", "onFinish() buildView");
                        Log.v("VALIDATE_SERVICE", "enable onFinish ");
                    }

                });
    }

    private void err_enable() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(), getString(R.string.error_net),
                Toast.LENGTH_SHORT).show();
    }

    private void addNewService(final Servicio service) {

        boolean addNew = false;

        // check si servicio existe
        mServiceId = service.getIdServicio();
        Log.v("VALIDATE_SERVICE3", "CHECK service service_id " + mServiceId);
        mCursor = mySQLiteAdapter.filterService(mServiceId);
        if (mCursor.getCount() > 0) {
            mCursor.moveToPosition(0);
            int status = Integer.valueOf(mCursor.getString(mCursor.getColumnIndex(BDAdapter.SRV_STA)));

            Log.v("VALIDATE_SERVICE3", "NO ADD SERVICE (status) " + String.valueOf(status));

            mCursor.close();

        } else {

            final Calendar c = Calendar.getInstance();
            long actualDate = c.getTimeInMillis();

            Log.v("VALIDATE_SERVICE", "ADD SERVICE ");
            mVisibleServices.add(mServiceId);
            Log.v("VALIDATE_SERVICE3", "ADD SERVICE mVisibleServices " + mServiceId);

            addNew = true;

            int kid = service.getRand_id();


            mySQLiteAdapter.insertService(mServiceId, "1", "", "", driver_id, actualDate,kid);
        }


        if (addNew) {
            final ViewGroup newView = (ViewGroup) LayoutInflater.from(this)
                    .inflate(R.layout.item_service, mContainerView, false);

            //ImageView imagen = (ImageView) newView.findViewById(R.id.imgElement);
            //Button btnElement = (Button) newView.findViewById(R.id.btnElement);
            LinearLayout btnElement = (LinearLayout) newView.findViewById(R.id.cell_service);

            ImageView icon = (ImageView) newView.findViewById(R.id.icon_desti);

            TextView direccion = (TextView) newView.findViewById(R.id.txtElement);

            TextView txtBarrio = (TextView) newView.findViewById(R.id.txtBarrio);

            TextView txtDestino = (TextView) newView.findViewById(R.id.txtDestino);

            TextView nombre = (TextView) newView.findViewById(R.id.txtElement_dos);

            ImageView payType = (ImageView) newView.findViewById(R.id.payTypeImg);
            // TODO:
            int pt = service.getPayType();
            if (pt == 2) { // Tarjeta
                payType.setImageResource(R.drawable.ic_pay_tc);
            }
            else if  (pt == 3) {
                payType.setImageResource(R.drawable.ic_pay_vale);
            }
           // else {
             //   payType.setImageResource(R.drawable.ic_pay_cash);
           // }

            nombre.setText(service.getName());
            String destino = service.getDestino();
            //if (destino.length() > 0) {
            //    destino = "\n" + service.getDestino();
            //}
            String barrio = service.getBarrio();
            Log.v("SRV1", "     addNew - service " + service.getIndiceName() + " " + service.getObs());

            String tp = String.valueOf(service.getTypeagend());
            Log.v("SRV1", "     addNew - service typeAgend " + tp);

            if ((tp.equals("1")) || (tp.equals("2")) || (tp.equals("3"))) {
                String serviceDateTime = service.getHora();
                String substr = serviceDateTime.substring(11, 16);
                destino = destino + getString(R.string.waitservice_texto_hora) + substr;
                String dir_full = null;
                Log.v("SRV1", "     addNew - service 1,2,3 " + service.getDireccion() + " " + service.getObs());

                if (service.getDireccion() != null) { // nueva dirección
                    //dir_full = service.getDireccion() + getString(R.string.waitservice_texto_barrio) + service.getBarrio()
                    //        + destino;
                    String obs = service.getObs();
                    if (obs != null && obs != "") {
                        dir_full = service.getDireccion() + " - " + service.getObs();
                    }
                    else
                        dir_full = service.getDireccion();
                } else {
//                    dir_full = service.getIndiceName() + " - " + service.getComp1()
//                            + " # " + service.getComp2() + " - " + service.getNumero()
//                            + " - " + service.getObs() + getString(R.string.waitservice_texto_barrio) + service.getBarrio()
//                            + destino;
                    dir_full = service.getIndiceName() + " - " + service.getComp1()
                            + " # " + service.getComp2() + " - " + service.getNumero()
                            + " - " + service.getObs();
                }
                direccion.setText(dir_full);

                txtDestino.setText(destino);

            } else {
                Log.v("SRV1", "     service.getIndiceName() " + service.getIndiceName());
                String cad = service.getIndiceName();

                String dir_full = null;
                if (service.getDireccion() != null) { // nueva dirección
                    //dir_full = service.getDireccion() + getString(R.string.waitservice_texto_barrio) + service.getBarrio()
                    //        + destino;
                    Log.v("SRV1", "     addNew - full - obs " + service.getDireccion() + " |" + service.getObs() + "|");
                    String obs = service.getObs();
                    if (obs != null && obs != "" && (obs.length() > 0)) {
                        dir_full = service.getDireccion() + " - " + service.getObs();
                    }
                    else
                        dir_full = service.getDireccion();


                } else {
//                    dir_full = service.getIndiceName() + " - " + service.getComp1()
//                            + " # " + service.getComp2() + " - " + service.getNumero()
//                            + " - " + service.getObs() + getString(R.string.waitservice_texto_barrio) + service.getBarrio()
//                            + destino;
                    Log.v("SRV1", "     addNew - obs " + service.getDireccion() + " " + service.getObs());

                    dir_full = service.getIndiceName() + " - " + service.getComp1()
                            + " # " + service.getComp2() + " - " + service.getNumero()
                            + " - " + service.getObs();
                }

                direccion.setText(dir_full);

                txtDestino.setText(destino);

            }
            txtBarrio.setText(barrio);

            //imagen.setOnClickListener(new OnClickListener() {
            btnElement.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("SRV_ACCEPT", "ini");

                    // valida si tiene un servicio tomado

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        Log.v("SRV_ACCEPT", "< 2000");
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    String value = readPreferencias("servicieTomado");
                    if (value != null) {
                        Log.v("SRV_ACCEPT", "value not nul" + value);
                        if (value.equals("true")) {
                            Log.v("SRV_ACCEPT", "value , servicio tomado " + value);
                            // poner toast
                            Toast.makeText(getApplicationContext(), R.string.waitservice_no_permitido_otro_servicio, Toast.LENGTH_LONG).show();
                        } else {
                            sendConfirmation(service);
                        }
                    } else {
                        Log.v("SRV_ACCEPT", "value nul");
                        sendConfirmation(service);
                    }
                }
            });

            Log.e("WaitServiceActivity", "     service.getRand_id() " + String.valueOf(service.getRand_id()));
            Log.e("WaitServiceActivity", "     service.getTypeagend() " + String.valueOf(service.getTypeagend()));

            // String type = service.getSchedule().getString("schedule_type");

            if (service.getRand_id() == 3) {
                icon.setImageResource(R.drawable.operadora_over);
                icon.setVisibility(View.VISIBLE);

            } else if (service.getTypeagend() != 0) {
                // try {

    			/* 1: Aeropuerto, 2: Fuera de Bogota, 3: Mensanjeria, 4: Horas */

                // String type = service.getSchedule().getString("schedule_type");
                String type = String.valueOf(service.getTypeagend());

                Log.e("WaitServiceActivity", "     service.getSchedule() " + String.valueOf(type));

                if (type.equals("1")) {
                    icon.setImageResource(R.drawable.aero_over);
                } else if (type.equals("2")) {
                    icon.setImageResource(R.drawable.fuerab_over);
                } else if (type.equals("3")) {
                    icon.setImageResource(R.drawable.mensajeria_over);
                } else if (type.equals("4")) {
                    icon.setImageResource(R.drawable.horas_over);
                }
                // } catch (JSONException e){
                // icon.setImageResource(R.drawable.agen_activos);
                // }
                icon.setVisibility(View.VISIBLE);
            }

            String tmp = service.getComp2().toString().toLowerCase(loc);

            tmp = tmp.replace("s", getString(R.string.waitservice_texto_sur));

            String tmp_comp1 = service.getComp1();

            try {

                int index_number = Integer.parseInt(tmp_comp1);

                if (index_number <= 10 && index_number > 0) {
                    tmp_comp1 = mNumbers[index_number - 1];
                }

            } catch (Exception e) {
                Log.e("WaitServiceActivity", e.toString() + "");
            }

            String cad1 = null;

            if (service.getDireccion() != null) {
                full_dir = getString(R.string.waitservice_texto_servicio) + service.getDireccion() + getString(R.string.waitservice_texto_barrio2) + service.getBarrio().toString();
            } else {
                cad1 = service.getIndiceName().toString();
                if (cad1 != null && cad1 != "") {
                    full_dir = getString(R.string.waitservice_texto_servicio) + service.getIndiceName().toString() + " "
                            + tmp_comp1 + " # " + tmp + " "
                            + service.getNumero().toString() + service.getObs().toString()
                            + "   " + getString(R.string.waitservice_texto_barrio2) + service.getBarrio().toString();
                } else {
                    full_dir = getString(R.string.waitservice_texto_servicio) + service.getNumero().toString();
                }
            }

            pt = service.getPayType();

            speakOut(mServiceId, full_dir, pt);

            newView.setId(Integer.parseInt(service.getIdServicio()));

            services.add(newView);
            Log.v("addNewService", "     addNewService service_id " + service.getIdServicio());

            //mContainerView.addView(newView, 0);
            mContainerView.post(new Runnable() {
                public void run() {
                    Log.v("addNewService", " newView in mContainerView");
                    mContainerView.addView(newView, 0);
                }
            });
        }
    }

    private void deleteService(String service_id) {
        Log.v("VALIDATE_SERVICE3", "deleteService " + service_id);
        try {
            Log.v("VALIDATE_SERVICE3", "deleteService try" + service_id);
            Log.v("VALIDATE_SERVICE3", "deleteService try size " + String.valueOf(services.size()));
            for (int i = 0; i < services.size(); i++) {
                Log.v("VALIDATE_SERVICE3", "deleteService list " + service_id + " " + String.valueOf(services.get(i).getId()));
                if (Integer.parseInt(service_id) == services.get(i).getId()) {
                    Log.v("VALIDATE_SERVICE3", "deleteService por borrar " + service_id);
                    mContainerView.removeView(services.get(i));
                    break;
                }
            }
        } catch (Exception e) {
            Log.v("VALIDATE_SERVICE3", "deleteService catch" + service_id);
            Log.e("ERROR 4", e.toString() + "");
        }

    }

    private void clearAllServices() {
        Log.v("clearAllServices", "     clearAllServices ini");
        Log.v("VALIDATE_SERVICE", "clearAllServices");

        mContainerView.post(new Runnable() {
            public void run() {
                Log.v("clearAllServices", "     removeAllViews");

                mContainerView.removeAllViews();
            }
        });

        services = null;
        services = new ArrayList<ViewGroup>();
        Log.v("clearAllServices", "     clearAllServices fin");

        // remove of sqlite when status == 1
        mySQLiteAdapter.deleteServices();
    }

    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    private void sendConfirmation(final Servicio service) {
        this.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        this.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        isServiceInProgress = true;

        // add randome delay
        int delay = randInt(300,1500);

        // call function
        Log.v("SEND_CONFIRMATION","ini " + String.valueOf(new Date()));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v("SEND_CONFIRMATION","delay " + String.valueOf(delay));
        Log.v("SEND_CONFIRMATION","fin " + String.valueOf(new Date()));

        MiddleConnect.sendConfirmation(this, service.getIdServicio(),
                conf.getIdUser(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        try {
                            pDialog = new ProgressDialog(WaitServiceActivity.this);
                            pDialog.setMessage(getString(R.string.waitservice_titulo_envio_confirmacion));
                            pDialog.setIndeterminate(false);
                            pDialog.setCancelable(false);
                            pDialog.show();
                        } catch (Exception e) {
                            Log.e("ERROR1", e.toString() + "");
                            pDialog.dismiss();
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        Log.e("RESPONSE", response + "");

                        try {

                            JSONObject responsejson = new JSONObject(response);
                            Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - sendConfirmation " + response);


                            if (responsejson.getBoolean("success")) {
                                Log.v("SERVICE_CMS", "    WAI SEND_CONFIRMATION service_id= " + service.getIdServicio());

                                deleteService(service.getIdServicio());


                                int status_id = responsejson.getInt("status_id");
                                Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - success " + response);
                                Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - sendConfirmation 1 status " + String.valueOf(status_id));

                                if (status_id == 2) {
                                    Log.v("SERVICE_CMS", "    WAI SEND_CONFIRMATION confirmado service_id= " + service.getIdServicio() + " " + String.valueOf(status_id));

                                    savePreferencias("servicieTomado", "true");

                                    mySQLiteAdapter.updateStatusService(service.getIdServicio(), "2");

                                    // eliminar servicios en estado 1
                                    mySQLiteAdapter.deleteServices();

                                    Intent intent = new Intent(WaitServiceActivity.this, MapActivity.class);

                                    intent.putExtra("lng", Double.parseDouble(service.getLongitud()));
                                    intent.putExtra("lat", Double.parseDouble(service.getLatitud()));

                                    intent.putExtra("id_servicio", service.getIdServicio());

                                    conf.setServiceId(service.getIdServicio());

                                    String type = String.valueOf(service.getTypeagend());
                                    String direccion = "";
                                    // if ((type.equals("2")) || (type.equals("3")))
                                    // {
                                    if ((type.equals("1")) || (type.equals("2")) || (type.equals("3"))) {

                                        // 012345678901234567
                                        // yyyy-MM-dd hh:mm:ss"
                                        String serviceDateTime = service.getHora();
                                        String substr = serviceDateTime.substring(
                                                11, 16);
                                        if (service.getDireccion() != null) {
                                            direccion = service.getDireccion() + getString(R.string.waitservice_texto_barrio2)
                                                    + service.getBarrio() + "\n"
                                                    + service.getDestino() + getString(R.string.waitservice_texto_hora)
                                                    + substr;
                                        } else {
                                            direccion = service.getIndiceName() + " - "
                                                    + service.getComp1() + " # "
                                                    + service.getComp2() + " - "
                                                    + service.getNumero() + " "
                                                    + service.getObs() + getString(R.string.waitservice_texto_barrio2)
                                                    + service.getBarrio() + "\n"
                                                    + service.getDestino() + getString(R.string.waitservice_texto_hora)
                                                    + substr;
                                        }

                                    } else {
                                        if (service.getDireccion() != null) {
                                            direccion = service.getDireccion() + getString(R.string.waitservice_texto_barrio2)
                                                    + service.getBarrio();
                                            String obs = service.getObs();
                                            if (obs != null && obs != "" && (obs.length() > 0)) {
                                                direccion = direccion + " - " + obs;
                                            }

                                        } else {
                                            direccion = service.getIndiceName() + " - "
                                                    + service.getComp1() + " # "
                                                    + service.getComp2() + " - "
                                                    + service.getNumero() + " "
                                                    + service.getObs() + getString(R.string.waitservice_texto_barrio2)
                                                    + service.getBarrio();
                                        }
                                    }
                                    Log.v("VER_MAP","direccion = " + direccion);
                                    intent.putExtra("direccion", direccion);
                                    intent.putExtra("kind_id", service.getRand_id());
                                    intent.putExtra("schedule_type", service.getTypeagend());
                                    intent.putExtra("name", service.getName());


                                    intent.putExtra("pay_type", service.getPayType());
                                    intent.putExtra("user_id", service.getUserId());
                                    intent.putExtra("user_card_reference", service.getCardReference());
                                    intent.putExtra("user_email", service.getUserEmail());

                                    // remove kind
                                    // elimina todos los servicios de operadora almacenados
                                    mySQLiteAdapter.deleteServicesOperator();

                                    isServiceInProgress = false;
                                            if (myTimerSpeak != null) {
                                                Log.v("wait_service", "1 stop myTimerSpeak");
                                                myTimerSpeak.cancel();
                                                myTimerSpeak.purge();
                                                myTimerSpeak = null;
                                            }

                                            if (tts != null) {
                                                Log.v("wait_service", "1 stop tts");
                                                tts.stop();
                                                tts.shutdown();
                                                tts = null;
                                            }

                                    startActivity(intent);
                                    Log.v("finish", "WaitServiceActivity sendConfirmation()");
                                    finish();
                                } else {
                                    Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - sendConfirmation status " + String.valueOf(status_id));
                                    Log.v("SERVICE_CMS", "    WAI SEND_CONFIRMATION cancelado o error service_id= " + service.getIdServicio() + " " + String.valueOf(status_id));

                                    if (status_id == 6) {
                                        err_confirmation(2);
                                    }
                                }

                            } else {

                                int error = responsejson.getInt("error");
                                if (error == 1) { // por si ya tomo otro servicio en curso
                                    Log.v("SRV_ACCEPT", "No puede tomar otro servicio teniendo uno activo");
                                    Toast.makeText(getApplicationContext(), R.string.waitservice_no_permitido_otro_servicio, Toast.LENGTH_LONG).show();
                                    isServiceInProgress = false;
                                } else {
                                    deleteService(service.getIdServicio());

                                    Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - ERROR " + response);

                                    err_confirmation(1);
                                }
                            }

                        } catch (Exception e) {
                            Log.e("ERROR2", e.toString() + "");
                            Log.e("ERROR - 2", e.getCause() + "");
                            isServiceInProgress = false;
                            Log.v("TAXISTA_SRVCONF", "WaitServiceActivity - sendConfirmation catch " + e.toString());
                            pDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String response = new String(responseBody);
                        Log.e("RESPONSE1", response + "");
                        //deleteService(service.getIdServicio());
                        //err_confirmation(1);
                    }

                    @Override
                    public void onFinish() {
                        try {
                            pDialog.dismiss();
                        } catch (Exception e) {
                            Log.e("ERROR3", e.toString() + "");
                        }
                    }

                });


    }

    private void err_confirmation(int status) {
        isServiceInProgress = false;
        Log.v("TAXISTA", "error_confirmación " + String.valueOf(status));
        pDialog.dismiss();
        try {
            if (status == 1) {
                new Dialogos(this, R.string.error1);
            } else if (status == 2) {
                new Dialogos(this, R.string.error2);
            } else if (status == 3) {
                new Dialogos(this, R.string.error3);
            }
        } catch (Exception e) {
            Log.e("JSON Confirmation: ", "ERROR ALERTA CONFIRMACION");
        }

    }

    public void servicesToSpeek() {
        myTimerSpeak.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("SERVICE_SPEAK", "por reproducir");

                String addressSpeak = null;
                boolean isSpeak = false;
                int spoken = 0;
                String srvId = null;

                // trae servicio a reproducir
                mCursor = mySQLiteAdapter.filterServiceForSpeak("0");
                if (mCursor.getCount() > 0) {
                    mCursor.moveToPosition(0);
                    Log.v("SERVICE_SPEAK", "spoken 0");

                    srvId = mCursor.getString(mCursor.getColumnIndex(BDAdapter.SRV_IDS));
                    addressSpeak = mCursor.getString(mCursor.getColumnIndex(BDAdapter.SRV_ADR));
                    isSpeak = true;

                    mCursor.close();
                } else { // intenta con eestado en 1
                    spoken = 1;

                    mCursor = mySQLiteAdapter.filterServiceForSpeak("1");
                    if (mCursor.getCount() > 0) {
                        mCursor.moveToPosition(0);
                        Log.v("SERVICE_SPEAK", "spoken 1");

                        srvId = mCursor.getString(mCursor.getColumnIndex(BDAdapter.SRV_IDS));
                        addressSpeak = mCursor.getString(mCursor.getColumnIndex(BDAdapter.SRV_ADR));
                        isSpeak = true;

                        mCursor.close();
                    }

                }

                // intenta reproducirlo

                // si lo reproduce actualiza su contador

                try {
                    if (tts != null) {
                        if (!tts.isSpeaking()) {

                            if (isSpeak) {
                                // update spoken and speak
                                spoken++;

                                mySQLiteAdapter.updateSpokenService(srvId, String.valueOf(spoken));
                                Log.v("SERVICE_SPEAK", "service address speak " + String.valueOf(spoken));
                                tts.speak(addressSpeak, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }, 5000, 10000);
    }

    public void validateStatusDriver() {
        // monitorea si se cancela el servicio
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("VALIDATE_SERVICE", "validateStatusDriver run");
                Log.e("VALIDATE_STATUS_TIMER", "INICIANDO " + String.valueOf(recibe_push) + " " + String.valueOf(new Date()));
                recibe_push++;

                if (isServiceInProgress) {
                    Log.v("VALIDATE_SERVICE", "servicio en curso, no recibe nuevos servicios");
                } else {
                    Log.e("VALIDATE_STATUS_TIMER", "no se reciben push, reintenta activación " + String.valueOf(new Date()) + " " + driver_id + " " + uuid);
                    Log.v("VALIDATE_SERVICE", "before enable ");
                    enable(driver_id, uuid);
                }
                recibe_push = 0;

            }
        }, 5000, 15000); // 20000

    }

    public void savePreferencias(String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String readPreferencias(String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPref.getString(key, "");
        return value;
    }

    @Override
    public void onConnectivityQualityChecked(boolean Optimal) {
        displayConnectivityPanel(!Optimal);
    }

    @Override
    public void onNetworkConnectivityChange(boolean connected) {
        displayConnectivityPanel(!connected);
    }
}
