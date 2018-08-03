package riddlesolver.game.com.hajjapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class UpdateLocationService extends Service {

    //private FusedLocationProviderClient mFusedLocationClient;
    SharedPreferences prefs;
    static Location location;
    boolean isBusy = false;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);


        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        updateLocation(prefs.getString(Constants.USER_ID, ""));
    }

    private void updateLocation(String id) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/update_location?id=" + id + "" +
                    "&location=" + location.getLatitude() + "," + location.getLongitude();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        url = url.replaceAll(" ", "%20");
        Log.e("Error", url);
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                isBusy = true;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"


                try {
                    JSONObject result = new JSONObject(new String(response));

                    Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }

            @Override
            public void onFinish() {
                isBusy = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 0f;

    private class LocationListener implements android.location.LocationListener {
        //Location mLastLocation;
        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            //mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location loc) {
            //Log.e("Error", "onLocationChanged: " + loc);
            //mLastLocation.set(location);
            location = loc;

            if (!isBusy)
                updateLocation(prefs.getString(Constants.USER_ID, ""));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(getBaseContext().LOCATION_SERVICE);
        }
    }
}