package com.imaginamos.taxisya.taxista.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
//import android.widget.CompoundButton;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.imaginamos.taxisya.taxista.R;
import com.imaginamos.taxisya.taxista.io.ApiConstants;
import com.imaginamos.taxisya.taxista.io.Connectivity;
import com.imaginamos.taxisya.taxista.io.MiddleConnect;
import com.imaginamos.taxisya.taxista.io.MyService;
import com.imaginamos.taxisya.taxista.io.UpdateReceiver;
import com.imaginamos.taxisya.taxista.model.Actions;
import com.imaginamos.taxisya.taxista.model.Conf;
import com.imaginamos.taxisya.taxista.utils.BDAdapter;
import com.imaginamos.taxisya.taxista.utils.Dialogos;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.paymentez.androidsdk.PaymentezSDKClient;
import com.paymentez.androidsdk.models.DebitCardResponseHandler;
import com.paymentez.androidsdk.models.PaymentezDebitParameters;
import com.paymentez.androidsdk.models.PaymentezResponseDebitCard;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

//public class MapActivity extends FragmentActivity implements OnClickListener, LocationListener {
    public class MapActivity extends Activity implements OnClickListener, LocationListener, UpdateReceiver.UpdateReceiverListener, Connectivity.ConnectivityQualityCheckListener
, OnMapReadyCallback {

    PaymentezSDKClient paymentezsdk;

    private TextView view_direccion, nombre;
    private ImageView icon_radio_ope;
    private ProgressDialog pDialog;

    private Button btnLlegada;
    private Button btnCancelar;
    private Button btnFinalizar;
    private Button btnPay;

    private EditText mUnits;
    private CheckBox mCheck1;
    private CheckBox mCheck2;
    private CheckBox mCheck3;
    private TextView mTotValue;

    private GoogleMap map;
    private ArrayList<LatLng> markerPoints;
    private double latitud, longitud = 0;
    private String driver_id, direccion;
    private String service_id;

    private String mTotUnits = "0";
    private String mTotCharge1 = "0";
    private String mTotCharge2 = "0";
    private String mTotCharge3 = "0";
    private String mTotCharge4 = "0";
    private String mTotService = "0";
    private String mTransactionId = "";


    private BroadcastReceiver mReceiver;

    private UpdateReceiver mNetworkMonitor;
    private ImageView mConnectivityLoaderImage;
    private RelativeLayout mNoConnectivityPanel;
    private Connectivity connectivityChecker = new Connectivity(this);


    private Timer myTimer = new Timer();
    private int reintento = 0;
    private int status = 0;

    private Conf conf;

    private int rand_id = -1;
    private int type_agend = -1;
    private int status_service = 0;

    private String mServiceId;
    private int mStatusOld;

    private BDAdapter mySQLiteAdapter;
    private Cursor mCursor;
    private boolean isFinished = true;
    private LatLng mCliente;

    private int mPayType = 1;
    private String mUserId;
    private String mUserCardReference;
    private String mUserEmail;
    private LinearLayout mLinear1;

    private long mTotalTrip = 0;

    @Override
    public void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.hold, R.anim.pull_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("onCreate", "MapActivity");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);


        //Desarrollo true
        //Producción false
        paymentezsdk = new PaymentezSDKClient(this, ApiConstants.api_env, ApiConstants.app_code, ApiConstants.app_secret_key);


        try {

            overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        } catch (Exception e) {
        }

        conf = new Conf(this);

        mySQLiteAdapter = new BDAdapter(this);
        mySQLiteAdapter.openToWrite();

        if (!Connectivity.isConnected(this)) {
            new Dialogos(MapActivity.this, R.string.error_net);
        }

        btnLlegada = (Button) findViewById(R.id.btnLlegada);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);
        btnFinalizar = (Button) findViewById(R.id.btnFinalizar);
        btnPay = (Button) findViewById(R.id.btnPay);

        mUnits = (EditText) findViewById(R.id.totUnits);

        mCheck1 = (CheckBox) findViewById(R.id.chkRecargo1);
        mCheck2 = (CheckBox) findViewById(R.id.chkRecargo2);
        mCheck3 = (CheckBox) findViewById(R.id.chkRecargo3);

        mTotValue = (TextView) findViewById(R.id.totViaje);


      /* mCheck1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });

        mCheck2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });

        mCheck3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });   */


        mNoConnectivityPanel = (RelativeLayout) findViewById(R.id.layout_no_connectivity);
        mConnectivityLoaderImage = (ImageView) findViewById(R.id.loader_icon);

        btnLlegada.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnFinalizar.setOnClickListener(this);
        btnPay.setOnClickListener(this);


        mLinear1 = (LinearLayout) findViewById(R.id.layout_pay);


        icon_radio_ope = (ImageView) findViewById(R.id.icon_radio_ope);

        view_direccion = (TextView) findViewById(R.id.direccion_cliente);

        nombre = (TextView) findViewById(R.id.nombre_cliente);

        markerPoints = new ArrayList<LatLng>();

        Bundle reicieveParams = getIntent().getExtras();

        latitud = reicieveParams.getDouble("lat");

        longitud = reicieveParams.getDouble("lng");

        service_id = reicieveParams.getString("id_servicio");

        if (latitud == 0 || longitud == 0) {
            latitud = 4.283435;
            longitud = -74.22404;
        }
        Log.v("MapActivity", "latitud = " + String.valueOf(latitud) + " longitud = " + String.valueOf(longitud));

        direccion = reicieveParams.getString("direccion");

        view_direccion.setText(getString(R.string.mapa_titulo_direccion) + direccion);

        rand_id = reicieveParams.getInt("kind_id");
        type_agend = reicieveParams.getInt("schedule_type");
        status_service = reicieveParams.getInt("status_service");
        mPayType = reicieveParams.getInt("pay_type");

        mUserId = reicieveParams.getString("user_id");
        mUserCardReference = reicieveParams.getString("user_card_reference");
        mUserEmail = reicieveParams.getString("user_email");


//        if (mPayType == 2) {
//            mLinear1.setVisibility(View.VISIBLE);
//        }
        if (rand_id == 3) {
            icon_radio_ope.setVisibility(View.VISIBLE);
            //yallege.setImageDrawable(getResources().getDrawable(R.drawable.onboard));
            //yallege.setVisibility(View.GONE);
            //yallege.setEnabled(false);
            //fin.setVisibility(View.VISIBLE);
        } else if (type_agend != 0) {
            if (type_agend == 1) {
                icon_radio_ope.setImageResource(R.drawable.aero_over);
            } else if (type_agend == 2) {
                icon_radio_ope.setImageResource(R.drawable.fuerab_over);
            } else if (type_agend == 3) {
                icon_radio_ope.setImageResource(R.drawable.mensajeria_over);
            } else if (type_agend == 4) {
                icon_radio_ope.setImageResource(R.drawable.horas_over);
            }
            icon_radio_ope.setVisibility(View.VISIBLE);
        }

        if (status_service == 4) {
            btnLlegada.setVisibility(View.GONE);
            btnCancelar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.VISIBLE);
        }

        nombre.setText(reicieveParams.getString("name"));

        driver_id = conf.getIdUser();

//        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);


        //map = fm.getMap();
        fm.getMapAsync(this);

//        map.setMyLocationEnabled(true);
//
//        mCliente = new LatLng(latitud, longitud);
//
//        markerPoints.add(cliente);
//
//        MarkerOptions options = new MarkerOptions();
//        Log.v("MapActivity", "driver_id = " + String.valueOf(driver_id) + " cliente =" + cliente.toString());
//        Log.v("MapActivity", "option.position(cliente) = " + String.valueOf(latitud) + " , " + String.valueOf(longitud));
//        Log.v("MapActivity", "MyService = " + String.valueOf(MyService.latitud) + " , " + String.valueOf(MyService.longitud));
//
//        options.position(cliente);
//
//        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.us_uno));
//
//        map.addMarker(options);
//
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cliente, 19.0f));
//
//
//        //Criteria crit = new Criteria();
//        //Location loc = locMan.getLastKnownLocation(locMan.getBestProvider(crit, false));
//        CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(
//                MyService.latitud,
//                MyService.longitud)).zoom(19.0f).build();
//        CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
//        map.moveCamera(camUpdate);


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria(); // object to retrieve provider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //criteria.setAltitudeRequired(false);
        //criteria.setBearingRequired(false);
        //criteria.setSpeedRequired(false);
        //criteria.setCostAllowed(true);
        //criteria.setPowerRequirement(Criteria.POWER_HIGH);

        String provider = locationManager.getBestProvider(criteria, true);

///        locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 1000, 2f, (LocationListener) this);
        //locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 200, 0, (LocationListener) this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, (LocationListener) this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, (LocationListener) this);

        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Actions.ACTION_USER_CANCELED_SERVICE);
        intentfilter.addAction(Actions.ACTION_OPE_CANCELED_SERVICER);
        intentfilter.addAction(Actions.ACTION_DRIVER_CLOSE_SESSION);
        intentfilter.addAction(Actions.ACTION_MESSAGE_MASSIVE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Actions.ACTION_USER_CANCELED_SERVICE)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error2), Toast.LENGTH_LONG).show();

                    toFinish();

                } else if (intent.getAction().equals(Actions.ACTION_OPE_CANCELED_SERVICER)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.oper_cancel), Toast.LENGTH_LONG).show();

                    toFinish();

                } else if (intent.getAction().equals(Actions.ACTION_DRIVER_CLOSE_SESSION)) {
                    Log.v("DRIVER_CLOSE_SESSION", "in MapActivity");
                    Toast.makeText(getApplicationContext(), R.string.login_deshabilito_login_otro_dispositivo, Toast.LENGTH_LONG).show();

                    loggedInOtherDevie();

                } else if (intent.getAction().equals(Actions.ACTION_MESSAGE_MASSIVE)) {

                    Log.v("MESSAGE_MASSIVE", "mensaje global recibido");
                    String message = intent.getExtras().getString("message");
                    mostrarMensaje(message);

                }

            }

        };

        registerReceiver(mReceiver, intentfilter);

        //validateService();


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mUnits.length() > 0) {
                    calcValor();
                }

            }
        };
        mUnits.addTextChangedListener(textWatcher);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        mCliente = new LatLng(latitud, longitud);

        map.setMyLocationEnabled(true);

        markerPoints.add(mCliente);

        MarkerOptions options = new MarkerOptions();
        Log.v("MapActivity", "driver_id = " + String.valueOf(driver_id) + " cliente =" + mCliente.toString());
        Log.v("MapActivity", "option.position(cliente) = " + String.valueOf(latitud) + " , " + String.valueOf(longitud));
        Log.v("MapActivity", "MyService = " + String.valueOf(MyService.latitud) + " , " + String.valueOf(MyService.longitud));

        options.position(mCliente);

        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.us_uno));

        map.addMarker(options);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mCliente, 19.0f));

        //Criteria crit = new Criteria();
        //Location loc = locMan.getLastKnownLocation(locMan.getBestProvider(crit, false));
        CameraPosition camPos = new CameraPosition.Builder().target(new LatLng(
                MyService.latitud,
                MyService.longitud)).zoom(19.0f).build();
        CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
        map.moveCamera(camUpdate);

    }

    //    @Override
//    public void onStart() {
//        Log.v("TAXISTA_SRVCONF1","onStart");
//
//        super.onStart();
//
//        validateService();
//
//    }

    /*
    private OnMapLongClickListener onLongClickMapSettings() {
        // TODO Auto-generated method stub
        return new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub
                Log.i(arg0.toString(), "User long clicked");
            }
        };
   }
    */

    @Override
    protected void onResume() {

        mCheck1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });

        mCheck2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });

        mCheck3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                calcValor();
            }
        });



        Log.v("TAXISTA_SRVCONF1","ini");
        validateService();


        super.onResume();



        displayConnectivityPanel(!Connectivity.isConnected(this) && !connectivityChecker.getConnectivityCheckResult());
        connectivityChecker.startConnectivityMonitor();
        mNetworkMonitor = new UpdateReceiver(this);
        registerReceiver(mNetworkMonitor, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Log.v("TAXISTA_SRVCONF1","end");

    }

    void mostrarMensaje(final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.login_titulo_dialogo_mensaje);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.login_aceptar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.e("PUSH", "mensaje: " + message);
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    void mostrarAviso(final String mensaje) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("Información");
                builder.setMessage(mensaje);
                builder.setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (myTimer != null) myTimer.cancel();
                        toFinish();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();

            }
        });

    }

    public void validateService() {
        // monitorea si se cancela el servicio
        reintento = 0;
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e("TIMER_EJECUTANDO1", "CARGANDO TAXISTA INI EJECUCIÓN *** " + String.valueOf(reintento));
                reintento++;

                try {
                    Log.e("TIMER_EJECUTANDO1", "checkService()");
                    checkService();
                } catch (JSONException je) {

                }

                Log.e("TIMER EJECUTNDO", "CONFIRMATION cerrando activity status " + String.valueOf(status));
//                if (status >= 6) {
////                      myTimer.cancel();
//                    Log.e("TIMER EJECUTNDO", "CONFIRMATION cerrando activity status " + String.valueOf(status));
//                    String msg;
//                    if (status == 8) {
//                        msg = "Servicio cancelado";
//                    } else {
//                        msg = "Servicio cancelado";
//                    }
//                    myTimer.cancel();
//                    finish();
//
//                }

                //if (reintento >= 3) {
                Log.e("TIMER_EJECUTANDO1", "CARGANDO TAXISTA FIN EJECUTANDO *** ");
                //puente.sendEmptyMessage(2000);
                //myTimer.cancel();
                //}
            }
        }, 5000, 10000); // 20000

    }


    public boolean checkService() throws JSONException {

        //service_id = null; //conf.getServiceId();

        Log.e("TIMER_EJECUTANDO1", "checkService() ini ");

        if (service_id != null && !service_id.isEmpty()) {
            Log.v("checkService", "driver_id=" + driver_id + " service_id=" + service_id);
        } else {
            Log.v("checkService", "driver_id=" + driver_id + " service_id=" + "NULO");
        }

        MiddleConnect.checkStatusService(this, driver_id, service_id, "uuid", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.v("checkService", "onStart");
                Log.e("TIMER_EJECUTANDO1", "checkService() onStart ");

            }

            @Override
            //public void onSuccess(String response) {
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String response = new String(responseBody);
                Log.e("TIMER_EJECUTANDO1", "checkService() response " + response);


                try {
                    //Log.v("checkService", "SUCCES: "+response);
                    JSONObject responsejson = new JSONObject(response);
                    //if (responsejson.getInt("status_id"))
                    //{
                    int status_service = responsejson.getInt("status_id");
                    Log.v("checkService", "status_id: " + String.valueOf(status_service));
                    Log.e("TIMER_EJECUTANDO1", "checkService() status " + String.valueOf(status_service));

                    // si hay un servicio asignado lo recupera
                    if (status_service >= 6) {
                        myTimer.cancel();
                        //finish();
                        toFinish();
                    }

                } catch (Exception e) {
                    Log.e("TIMER_EJECUTANDO1", "checkService() response catch 1 ");
                    Log.v("checkService", "Problema json" + e.toString());
                }

                try {
                    JSONObject rj = new JSONObject(response);
                    int error = rj.getInt("error");
                    Log.e("TIMER_EJECUTANDO1", "checkService() rj error 1");

                    if (error == 1) {
                        Log.e("TIMER_EJECUTANDO1", "checkService() rj error 2");

                        mostrarAviso(getString(R.string.mapview_servicio_cancelado));

                    }
                } catch (Exception e2) {

                }
            }

            @Override
            //public void onFailure(Throwable e, String response) {
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                String response = new String(responseBody);
                Log.e("TIMER_EJECUTANDO1", "checkService() onFailure response " + response);

                Log.v("checkService", "onFailure");

            }

            @Override
            public void onFinish() {
                Log.e("TIMER_EJECUTANDO1", "onFinish() ");

                Log.v("checkService", "onFinish");

            }

        });
        return true;

    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        Log.v("onLocationChanged", "posicion");
        LatLng latlng = new LatLng(MyService.latitud, MyService.longitud);
        map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(latlng)
//                  .zoom(19)
                .zoom(map.getCameraPosition().zoom)
                .bearing(0)
                .build();
//     map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),10, null);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void arrivedService(final int action) {
        Log.v("arrivedService" ," a params: " + driver_id + " " + service_id);
        if (service_id == null && service_id.isEmpty()) {
            service_id = conf.getServiceId();
        }
        Log.v("arrivedService" ," b params: " + driver_id + " " + service_id);

        //MiddleConnect.arrived(this, driver_id, new AsyncHttpResponseHandler() {
        MiddleConnect.arrived2(this, driver_id, service_id, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

                try {
                    pDialog = new ProgressDialog(MapActivity.this);
                    pDialog.setMessage(getString(R.string.map_enviando_aviso_cliente));
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                } catch (Exception e) {

                }
            }

            @Override
            //public void onSuccess(String response) {
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String response = new String(responseBody);
                    Log.e("RESPOSNE", response + "");
                try {

                    JSONObject responsejson = new JSONObject(response);

                    if (responsejson != null && responsejson.length() > 0) {

                        if (responsejson.getBoolean("success")) {
                            Log.v("SERVICE_CMS", "    MAP ARRIVED ok service_id= " + service_id );


                            // actualizar estado del servicio
                            Log.v("VALIDATE_SERVICE", "arrived service_id " + service_id);

                            mySQLiteAdapter.updateStatusService(service_id, "4");

                            Toast.makeText(getApplicationContext(), getString(R.string.the_user_arrived), Toast.LENGTH_SHORT).show();

                            if (action == 2) {
                                toFinish();
                            } else {
                                btnLlegada.setVisibility(View.GONE);
                                btnCancelar.setVisibility(View.GONE);
                                btnFinalizar.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Log.v("SERVICE_CMS", "    MAP ARRIVED bad service_id= " + service_id );

                            err_arrived();
                        }

                    } else {
                        err_arrived();
                    }

                } catch (Exception e) {
                    err_arrived();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            //public void onFailure(Throwable e, String response) {
                String response = new String(responseBody);
                Log.e("RESPOSNE", response + "");
                err_arrived();
            }

            @Override
            public void onFinish() {
                Log.v("SERVICE_CMS", "    MAP finish service_id= " + service_id );

                try {
                    pDialog.dismiss();
                } catch (Exception e) {

                }
            }
        });

    }

    private void err_arrived() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(), getString(R.string.error_arrived), Toast.LENGTH_SHORT).show();
    }

    // finish service +
    private void finishService() {
        Log.v("FINISH2","finishService() ini ");
        Log.v("FINISH2","finishService() driver_id=" + driver_id + " service_id=" + service_id );


        MiddleConnect.finishService(this, driver_id, service_id, MyService.latitud, MyService.longitud, mTotUnits, mTotCharge1, mTotCharge2, mTotCharge3, mTotCharge4, mTotService, mTransactionId, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

                try {
                    pDialog = new ProgressDialog(MapActivity.this);
                    pDialog.setMessage(getString(R.string.mapa_finalizando_servicio));
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                } catch (Exception e) {

                }
            }

            @Override
            //public void onSuccess(String response) {
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String response = new String(responseBody);
                Log.v("FINISH2","finishService() onSuccess() - " + response);
                Log.v("SERVICE_CMS", "    MAP FINISHED success service_id= " + service_id + " response " + response);
                Log.e("RESPOSNE", response + "");
                try {
                    JSONObject responsejson = new JSONObject(response);
                    savePreferencias("servicieTomado", "false");
                    Log.v("VALIDATE_SERVICE", "finished service_id " + service_id);

                    mySQLiteAdapter.updateStatusService(service_id, "5");

                    if (responsejson != null && responsejson.length() > 0) {
                        String result = responsejson.getString("error");
                        if (Integer.valueOf(result) == 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.servicio_finalizado), Toast.LENGTH_SHORT).show();
                        } else {
                            err_finish_service();
                        }

                    } else {
                        err_finish_service();
                    }
                } catch (Exception e) {
                    Log.v("FINISH2","finishService() onSuccess() exception - e" + e.toString());

                    err_finish_service();
                }
            }

            @Override
            //public void onFailure(Throwable e, String response) {
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                Log.v("FINISH2","finishService() onFailure() - " + response);

                Log.v("SERVICE_CMS", "    MAP FINISHED failure service_id= " + service_id + " response " + response);

                Log.e("RESPOSNE", response + "");
                err_finish_service();
            }

            @Override
            public void onFinish() {
                try {
                    pDialog.dismiss();
                } catch (Exception e) {

                }
                Log.v("FINISH2","finishService() onFinish()");

                if (isFinished)
                   toFinish();
            }
        });

    }

    private void err_finish_service() {
        isFinished = false;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(), getString(R.string.error_arrived), Toast.LENGTH_SHORT).show();
    }
    // finish service -

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnLlegada:
                Log.v("BTN1", "solictaxi");
                arrivedService(1);
                break;

            case R.id.btnCancelar:
                Log.v("BTN1", "btn_volver");
                setModalCancelSerivce();
                final CharSequence[] items = {"Trafico pesado", "Varado", "Dirección Errada"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Razon cancelar servicio");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Haz elegido la opcion: " + items[item] , Toast.LENGTH_SHORT);
                        toast.show();
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;

            case R.id.btnFinalizar:
                Log.v("BTN1", "finalizar_action");
                Log.v("FINISH2","call finishService()");
                if ((mPayType == 1) || (mPayType == 2)||(type_agend== 1)||(type_agend== 2)||(type_agend== 3)||(type_agend== 4)) {
                    mLinear1.setVisibility(View.VISIBLE);
                    btnFinalizar.setVisibility(View.GONE);
                    btnPay.setVisibility(View.VISIBLE);
                }
                else {
                    finishService();
                //toFinish();
                }
                break;


            case R.id.btnPay:


                prepareReceipt(String.valueOf(mTotalTrip));
                //finishService();

                break;
        }

    }

    public void calcValor() {
        Log.v("CAL_VAL","ini");
        int c1 = 0;
        int c2 = 0;
        int c3 = 0;
        int p = 700;
        int val = 0;
        int valp = 0;

        int roundTo = 100;

        // get unidades
        int units = 0;
        if(!TextUtils.isEmpty(mUnits.getText().toString())) {
            //if (mUnits.getText().toString() != null)
            units = Integer.valueOf(mUnits.getText().toString());
        }
        if (mCheck1.isChecked()) {
            c1 = 4100; mTotCharge1 = "4100";
        }
        if (mCheck2.isChecked()) {
            c2 = 2000; mTotCharge2 = "2000";
        }
        if (mCheck3.isChecked()) {
            c3 = 5000; mTotCharge3 = "5000";
        }

        val = 82 * units;
        val = roundTo * Math.round(val / roundTo);
        //val = p + (82 * units)  + c1 + c2 + c3;
        val = p + val + c1 + c2 + c3;
        valp = val - p;

        // round
        

        mTotValue.setText("Total: " + String.valueOf(valp) + "+" + String.valueOf(p) + "= $ " + String.valueOf(val));

        mTotalTrip = Integer.valueOf(val);

        mTotUnits = String.valueOf(units);
        mTotService = String.valueOf(val);

    }

    private void setModalCancelSerivce() {
        AlertDialog.Builder mdialog = new AlertDialog.Builder(this);

        mdialog.setTitle(getString(R.string.important));
        mdialog.setMessage(getString(R.string.confim_cancel_service));
        mdialog.setCancelable(false);
        mdialog.setPositiveButton(R.string.mapa_respuesta_si,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogo1, int id) {
                        cancelarService();
                    }
                });
        mdialog.setNegativeButton(R.string.mapa_respuesta_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        dialogo1.cancel();
                    }
                });
        mdialog.show();
    }

    private void cancelarService() {

        MiddleConnect.cancelService(this, driver_id, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

                try {
                    pDialog = new ProgressDialog(MapActivity.this);
                    pDialog.setMessage(getString(R.string.mapa_cancelando_servicio));
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                } catch (Exception e) {

                }
            }

            @Override
            //public void onSuccess(String response) {
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String response = new String(responseBody);
                Log.e("RESPOSNE", response + "");
                try {
                    JSONObject responsejson = new JSONObject(response);

                    savePreferencias("servicieTomado", "false");

                    if (responsejson != null && response.length() > 0) {

                        if (responsejson.getBoolean("success")) {
                            toFinish();

                        } else {
                            err_cancel();
                        }

                    }

                } catch (Exception e) {
                    err_cancel();
                }
            }

            @Override
            //public void onFailure(Throwable e, String response) {
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                String response = new String(responseBody);
                Log.e("RESPOSNE", response + "");
                err_cancel();
            }

            @Override
            public void onFinish() {
                try {
                    savePreferencias("servicieTomado", "false");

                    pDialog.dismiss();
                } catch (Exception e) {

                }
            }

        });

    }

    private void err_cancel() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Toast.makeText(getApplicationContext(), getString(R.string.error_cancel_service), Toast.LENGTH_LONG).show();
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
    protected void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy", "MapActivity");

        if (mySQLiteAdapter != null) {
            mySQLiteAdapter.close();
        }

/*
        if( mReceiver != null ) {
            unregisterReceiver(mReceiver);
        }
*/
        if (myTimer != null) {
            myTimer.cancel();
            myTimer.purge();
            myTimer = null;
        }
    }

    private void toFinish() {
        Log.v("FINISH2","finishService() toFinish()");

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("service", "ended");
        editor.commit();

        // activar service
        Log.v("finish", "MapActivity toFinish()");
        savePreferencias("servicieTomado", "false");
        finish();
    }

    private void loggedInOtherDevie() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("service", "ended");
        editor.commit();
        Log.v("finish", "MapActivity loggedInOtherDevie()");
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectivityChecker.stopConnectivityMonitor();
        unregisterReceiver(mNetworkMonitor);
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    public void savePreferencias(String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void displayConnectivityPanel(boolean display){
        mNoConnectivityPanel.setVisibility(display ? View.VISIBLE:View.GONE);
        if(display)
            mConnectivityLoaderImage.startAnimation(AnimationUtils.loadAnimation(this,R.anim.connection_loader));
    }

    @Override
    public void onNetworkConnectivityChange(boolean connected) {
        displayConnectivityPanel(!connected);
    }

    @Override
    public void onConnectivityQualityChecked(boolean Optimal) {
        displayConnectivityPanel(!Optimal);
    }

    public boolean typePayment() {
        // TODO: Ver type pago
        if (mPayType == 2) return true;
        return false;

    }

    public void prepareReceipt(String value) {
        PaymentezDebitParameters debitParameters = new PaymentezDebitParameters();

        Log.v("prepareReceipt", "s value " + value);


        double v1 = Double.valueOf(value);
//        double money=Double.valueOf(value);
//        String str=String.valueOf(money);
//        str.replace('.',',');
//        double v1 = Double.valueOf(str);

        debitParameters.setUid(mUserId);
        debitParameters.setEmail(mUserEmail);
        debitParameters.setCardReference(mUserCardReference);
//        debitParameters.setProductAmount(Integer.valueOf(value));
//        debitParameters.setProductAmount(Double.valueOf(value));
        debitParameters.setProductAmount(v1);


        debitParameters.setProductDescription("Servicio de transporte");
        debitParameters.setDevReference("Prueba cobro servicio");


        Log.v("prepareReceipt", "ini ");
        Log.v("prepareReceipt", "mUserId " + mUserId);
        Log.v("prepareReceipt", "mUserEmail " + mUserEmail);
        Log.v("prepareReceipt", "mUserReference " + mUserCardReference);
        Log.v("prepareReceipt", "val " + v1);


        if (mPayType == 1) {
            mTransactionId = "EFECTIVO";
            finishService();

        }
        else if (((type_agend == 1) || (type_agend == 2) || (type_agend == 3) || (type_agend == 4))) {

            mTransactionId = "AGENDAMIENTO";
            finishService();
        }
            else {


                final ProgressDialog pd = new ProgressDialog(MapActivity.this);
                pd.setMessage("");
                pd.show();

                paymentezsdk.debitCard(debitParameters, new DebitCardResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, PaymentezResponseDebitCard paymentezResponse) {
                        pd.dismiss();
                        Log.v("prepareReceipt", "success " + paymentezResponse.toString());

                        if (!paymentezResponse.isSuccess()) {
                            android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MapActivity.this);

                            Log.v("prepareReceipt", "paymentezResponse " + paymentezResponse.getTransactionId());


                            builder1.setMessage("Error: " + paymentezResponse.getErrorMessage());

                            builder1.setCancelable(false);
                            builder1.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            finishService();
                                        }
                                    });
                            android.support.v7.app.AlertDialog alert11 = builder1.create();
                            alert11.show();

                        } else {
                            Log.v("prepareReceipt", "v2 ");
                            if (paymentezResponse.getStatus().equals("failure") && paymentezResponse.shouldVerify()) {
                                android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MapActivity.this);

                                //String message = "You must verify the transaction_id: " + paymentezResponse.getTransactionId();
                                String message = "Se presento un problema al realizar la transacción. Su código de transacción es: " + paymentezResponse.getTransactionId();

                                mTransactionId = paymentezResponse.getTransactionId();

                                builder1.setMessage(message);

                                builder1.setCancelable(false);
                                builder1.setPositiveButton(getString(R.string.btn_ok_text),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                finishService();
                                            }
                                        });
                                android.support.v7.app.AlertDialog alert11 = builder1.create();
                                alert11.show();
                            } else {
                                android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MapActivity.this);

                                mTransactionId = paymentezResponse.getTransactionId();

//                        builder1.setMessage("status: " + paymentezResponse.getStatus() +
//                                "\nstatus_detail: " + paymentezResponse.getStatusDetail() +
//                                "\nshouldVerify: " + paymentezResponse.shouldVerify() +
//                                "\ntransaction_id:" + paymentezResponse.getTransactionId());
                                builder1.setMessage("Transacción exitosa. Su código de transacción es: " + paymentezResponse.getTransactionId());

                                builder1.setCancelable(false);
                                builder1.setPositiveButton(getString(R.string.btn_ok_text),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                finishService();
                                            }
                                        });
                                android.support.v7.app.AlertDialog alert11 = builder1.create();
                                alert11.show();


                                System.out.println("TRANSACTION INFO");
                                System.out.println(paymentezResponse.getStatus());
                                System.out.println(paymentezResponse.getPaymentDate());
                                System.out.println(paymentezResponse.getAmount());
                                System.out.println(paymentezResponse.getTransactionId());
                                System.out.println(paymentezResponse.getStatusDetail());

                                System.out.println("TRANSACTION card_data");
                                System.out.println(paymentezResponse.getCardData().getAccountType());
                                System.out.println(paymentezResponse.getCardData().getType());
                                System.out.println(paymentezResponse.getCardData().getNumber());
                                System.out.println(paymentezResponse.getCardData().getQuotas());

                                System.out.println("TRANSACTION carrier_data");
                                System.out.println(paymentezResponse.getCarrierData().getAuthorizationCode());
                                System.out.println(paymentezResponse.getCarrierData().getAcquirerId());
                                System.out.println(paymentezResponse.getCarrierData().getTerminalCode());
                                System.out.println(paymentezResponse.getCarrierData().getUniqueCode());
                            }
                        }
                    }


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        pd.dismiss();
                        Log.v("prepareReceipt", "fail " + responseString);
                        System.out.println("Failure: " + responseString);
                    }


                });
            }

        }


    private double s(double d){
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        twoDForm.setDecimalFormatSymbols(dfs);

        String doubleString =  displayNumberAmount(twoDForm.format(d));
        return Double.valueOf(doubleString);
    }

    public static String displayNumberAmount(String amount) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.CANADA_FRENCH);

        Number number = 0;

        try {
            number = numberFormat.parse(amount);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return String.format(Locale.US, "%1$,.2f", number);
    }

}
