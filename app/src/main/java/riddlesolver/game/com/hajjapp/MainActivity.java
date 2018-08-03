package riddlesolver.game.com.hajjapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("loaderpk");
        FirebaseInstanceId.getInstance().getToken();

        progressBar = findViewById(R.id.progress_bar);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


        if(!prefs.getBoolean(Constants.FIRST_TIME, true)){
            startActivity(new Intent(MainActivity.this, UsersList.class));
            finish();
        }

    }

    public void showMacAddress(View view) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        getUserList(macAddress, ((EditText) findViewById(R.id.passport_number)).getText().toString());
        Log.e("Error", macAddress);
    }

    private void getUserList(String macAddress, String passportNumber) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/assign?mac=" + macAddress + "&passport=" + passportNumber;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        url = url.replaceAll(" ", "%20");
        Log.e("Error", url);
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"


                try {
                    JSONObject result = new JSONObject(new String(response));

                    Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {
                        Log.e("Error", result.toString());

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Constants.USERS_DATA, result.toString());
                        editor.putBoolean(Constants.FIRST_TIME, false);
                        editor.apply();

                        startActivity(new Intent(MainActivity.this, UsersList.class));
                        finish();

                    } else {
                        Toast.makeText(MainActivity.this, "unable to connect to server..", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
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
}
