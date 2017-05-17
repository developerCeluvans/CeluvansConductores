package com.imaginamos.taxisya.taxista.io;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesClient;
//import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.imaginamos.taxisya.taxista.GcmKeepAlive;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import cz.msebera.android.httpclient.Header;

//public class MyService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , LocationListener {

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5; //1; //5;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private GcmKeepAlive gcmKeepAlive;

    //private LocationManager locationManager;

    public static double latitud, longitud;
    public static int meters = 2000; // 5000;

    private String driver_id;
    private Timer timer;

    private boolean currentlyProcessingLocation = false;

    private String TAG = "SERVICE_SEND_POSTION";

//    private LocationRequest mLocationRequest;
//    private LocationClient mLocationClient;
//    private android.location.LocationListener loca;



    private LocationManager locationManager;

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private final int TIME_OUT = 60 * 1000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private boolean mSendPosition = false;


/*
    Handler han = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		Log.e(TAG, "*** run *****");

	    	try {
	    		mLocationClient.removeLocationUpdates(MyService.this);
			} catch (Exception e) {
				Log.e(TAG, "" + e.toString());
			}

	    	try {
	    		locationManager.removeUpdates(loca);
			} catch (Exception e) {
				Log.e(TAG, "" + e.toString());
			}
	    	activeLocation();
    	};
    };
*/
    public static int getMeters() {
        Log.v("MyService","getMeters");

        if (isTimeBetweenTwoTime()) {
            Log.v("MyService","getMeters esta entre 22:00 y 05:00");
            return 5000;
        }
        else {
            Log.v("MyService","getMeters no esta entre 22:00 y 05:00");
            return 2000;
        }

    }

    private static boolean isTimeBetweenTwoTime() {
        try {
            String string1 = "22:00:00";
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = "05:00:00";
            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);

            Calendar calendar3 = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String curDate = sdf.format(calendar3.getTime());
            Date time3 = new SimpleDateFormat("HH:mm:ss").parse(curDate);
            calendar3.setTime(time3);
            Date x = calendar3.getTime();

            Log.v("MyService","isTimeBetweenTwoTime date = " + x.toString());
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                //checkes whether the current time is between 05:00:00 and 22:00:00.
                Log.v("MyService","isTimeBetweenTwoTime true");
                return true;
            }
            else {
                Log.v("MyService","isTimeBetweenTwoTime false");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.v("MyService","isTimeBetweenTwoTime false (default)");
        return false;
    }

    private void activeLocation() {
        /*
        try {Log.e(TAG, "*** activeLocation *****");

			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();

			mLocationRequest = LocationRequest.create();
			mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
			mLocationRequest.setInterval(FASTEST_INTERVAL);

			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			loca = new android.location.LocationListener() {

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLocationChanged(Location location) {

					Log.e(TAG, "LAT :"+location.getProvider()+"->"+location.getLatitude()+" LOG"+location.getLongitude());
					longitud = location.getLongitude();
					latitud  = location.getLatitude();
				}
			};
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FASTEST_INTERVAL, 0, loca);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FASTEST_INTERVAL, 0, loca);


		} catch (Exception e) {
			Log.e(TAG, "***3 activeLocation *****");
			Log.e(TAG, "" + e.toString());
		}
        */
//        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
//            mLocationClient = new LocationClient(this, this, this);

//            if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
//                mLocationClient.connect();
//            }
//        } else {
//            Log.e(TAG, "unable to connect to google play services.");
//        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        gcmKeepAlive = new GcmKeepAlive(this);

//
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(MyService.this)
//                    .addConnectionCallbacks(MyService.this)
//                    .addOnConnectionFailedListener(MyService.this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }

        locationManager =
                (android.location.LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.startActivity(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient = new GoogleApiClient.Builder(MyService.this)
                    .addConnectionCallbacks(MyService.this)
                    .addOnConnectionFailedListener(MyService.this)
                    .addApi(LocationServices.API)
                    .build();
        /*
        Bundle extras = intent.getExtras();

		if (extras != null) {

			activeLocation();

			try {
				timer = new Timer();

				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						han.sendEmptyMessage(0);
                        Log.e(TAG,"han.sendEmptyMessage");
					}

				}, 1200000,1200000);

			    this.driver_id = extras.getString("driver_id");
			} catch (Exception e) {
				Log.e(TAG, "" + e.toString());
			}

		}
		return START_NOT_STICKY;
        */
        Bundle extras = intent.getExtras();
        if (extras != null) {
            this.driver_id = extras.getString("driver_id");
        } else {
            Log.e(TAG, "intent.getExtras() == null");
        }

/*
        if (intent.getExtras() == null) {
           Log.e(TAG,"inten.getExtras() == null");
        }
        else {
            Bundle extras = intent.getExtras();
            if (extras != null)
                this.driver_id = extras.getString("driver_id");
        }
*/


        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }


        //setUpLocationClientIfNeeded();
//        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting())
//        {
//            mGoogleApiClient.connect();
//        }

        return START_STICKY;

//
//        if (!currentlyProcessingLocation) {
//            currentlyProcessingLocation = true;
//            activeLocation();
//        }
//        //return START_STICKY;
//        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "*** onDestroy *****");
        try {
            //mLocationClient.removeLocationUpdates(this);

        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }

//        try {
//            locationManager.removeUpdates(loca);
//        } catch (Exception e) {
//            Log.e(TAG, "" + e.toString());
//        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "LAT -> :" + location.getProvider() + "->" + location.getLatitude() + " LOG" + location.getLongitude());
            longitud = location.getLongitude();
            latitud = location.getLatitude();

//            if (location.getAccuracy() < 100.0f) {
//                stopLocationUpdates();
            Log.e(TAG, "sendMyPosition");
            MiddleConnect.sendMyPosition(this, driver_id, String.valueOf(latitud), String.valueOf(longitud), new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.e(TAG, "onStart");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.e(TAG, "onSuccess" + response);

                    gcmKeepAlive.broadcastIntents();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String response = new String(responseBody);
                    Log.e(TAG, "onFailure" + response);
                }

                @Override
                public void onFinish() {
                    Log.e(TAG, "onFinish");
                }
            });
//            }
        }
    }

//    private void stopLocationUpdates() {
//        if (mLocationClient != null && mLocationClient.isConnected()) {
//            //mLocationClient.removeLocationUpdates(this);
//            mLocationClient.disconnect();
//        }
//    }

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.v("RouteActivity", "location " + latitude + ", " + longitude);

        } else {

            Log.v("RouteActivity", "location Couldn't get the location. Make sure location is enabled on the device");
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient == null) {
            Log.i("RouteActivity", "client is null");
        }
        if (mLocationRequest == null) {
            Log.i("RouteActivity", "location request is null");
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("onConnectionFailed", "onConnectionFailed");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("onConnected", "onConnected");

//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(FASTEST_INTERVAL); // milliseconds
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        //mLocationClient.requestLocationUpdates(mLocationRequest, this);
//        mLocationRequest = LocationServices.FusedLocationApi.getLastLocation(
//                mLocationClient);


        createLocationRequest();
        startLocationUpdates();

        // Once connected with google api, get the location
        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    //    @Override
//    public void onDisconnected() {
//        Log.e("onDisconnected", "onDisconnected");
//    }
//
//    protected void startLocationUpdates() {
//        if (mGoogleApiClient == null) {
//            Log.i("RouteActivity", "client is null");
//        }
//        if (mLocationRequest == null) {
//            Log.i("RouteActivity", "location request is null");
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
//    }
}
