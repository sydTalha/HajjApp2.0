package riddlesolver.game.com.hajjapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bean.User;
import cz.msebera.android.httpclient.Header;
import riddlesolver.game.com.TrackPerson;
import riddlesolver.game.com.adapters.UserAdapter;

public class UsersList extends AppCompatActivity {


    ArrayList<User> data;
    RecyclerView list;
    UserAdapter adapter;

    ArrayList<String> temp;
    SharedPreferences prefs;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        data = new ArrayList<>();
        temp = new ArrayList<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);


        getData();


        list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        adapter = new UserAdapter(getBaseContext(), data);


        adapter.setOnTrackPerson(new TrackPerson() {
            @Override
            public void onTrackPerson(String id) {
                getLocation(id);
            }
        });
        list.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startService("");
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 567);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 567) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService("");
            } else {
                // Permission was denied. Display an error message.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 567);
            }
        }
    }

    private void startService(String id) {
        Intent i = new Intent(getBaseContext(), UpdateLocationService.class);
        i.putExtra(Constants.USER_ID, prefs.getString(Constants.USER_ID, ""));
        startService(i);
    }

    private void getData() {
        String data = prefs.getString(Constants.USERS_DATA, " ");
        try {
            JSONObject object = new JSONObject(data);
            Log.e("Error", data);
            JSONArray users = object.getJSONArray("group_data");

            for (int i = 0; i < users.length(); i++) {
                JSONObject obj = users.getJSONObject(i);
                User user = new User();
                user.setName(obj.getString("name"));
                user.setId(obj.getString("id"));
                user.setApplicationNumber(obj.getString("app_no"));
                user.setDestination(obj.getString("destination"));
                user.setGroupId(obj.getString("group_id"));
                user.setMacAddress(obj.getString("mac_address"));
                this.data.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trackGroup(View view) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/get_location?group_id=" + getGroupId();
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

                    //Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {
                        JSONArray array = result.getJSONArray("grouplocations");
                        Intent intent = new Intent(UsersList.this, MapsActivity.class);
                        intent.putExtra("data", result.toString());
                        startActivity(intent);
                        Log.e("Error", result.toString());
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

    private String getGroupId() {
        try {
            JSONObject userData = new JSONObject(prefs.getString(Constants.USERS_DATA, ""));
            JSONArray array = userData.getJSONArray("my_data");

            // Log.e("Error", userData.toString());
            JSONObject object = array.getJSONObject(0);
            return object.getString("group_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUserId() {
        try {
            JSONObject userData = new JSONObject(prefs.getString(Constants.USERS_DATA, ""));
            JSONArray array = userData.getJSONArray("my_data");

            // Log.e("Error", userData.toString());
            JSONObject object = array.getJSONObject(0);
            return object.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alert:
                alertGroup();
                break;
            case R.id.vehicle_tracking:
                trackVehicles();
                break;
            case R.id.crowd_warning:
                warnCrowd();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void warnCrowd() {
        //http://hackathon.intrasols.com/api/crowd_notification?location=33.738045,73.084488&id=2

        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/crowd_notification?location=" +
                    UpdateLocationService.location.getLatitude() + "," + UpdateLocationService.location.getLongitude() + "&id=" + getUserId();
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

                /*
                try {
                    JSONObject result = new JSONObject(new String(response));

                    Log.e("notif", result.toString());
                    if (result.getInt("response") == 101) {

                    } else {

                    }
                } catch (JSONException e) {
                    Snackbar.make(findViewById(R.id.container), "" +
                            "Authorities Informed. Stay Safe", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (Exception e) {

                    Snackbar.make(findViewById(R.id.container), "" +
                            "Authorities Informed. Stay Safe", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
                */

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

    private void trackVehicles() {
        //http://hackathon.intrasols.com/api/vehicles

        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/vehicles";
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

                    //Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {

                        Intent intent = new Intent(UsersList.this, MapsActivity.class);
                        intent.putExtra("isVehicle", true);
                        intent.putExtra("data", result.toString());
                        startActivity(intent);
                        Log.e("Error", result.toString());
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

    private void alertGroup() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/im_lost?group_id=" + getGroupId() + "&lost_id=" + getUserId();
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

                    //Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {
                        Snackbar.make(findViewById(R.id.container), "Your Group Members" +
                                " have been informed", Snackbar.LENGTH_LONG).show();
                        Log.e("Error", result.toString());
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

    public void getLocation(String id) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = null;
        try {
            url = "http://hackathon.intrasols.com/api/get_location?haji_id=" + id;
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

                    //Log.e("Error", result.toString());
                    if (result.getInt("response") == 101) {
                        JSONArray array = result.getJSONArray("location");
                        JSONObject object = array.getJSONObject(0);
                        String lat = object.getString("location").split(",")[0];
                        String lang = object.getString("location").split(",")[1];

                        showDirections(lat, lang);
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

    private void showDirections(String lat, String lang) {
        String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lang + " (" + "Where the party is at" + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
}
