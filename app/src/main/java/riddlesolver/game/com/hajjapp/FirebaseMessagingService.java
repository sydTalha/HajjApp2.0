package riddlesolver.game.com.hajjapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.e("notif", remoteMessage.getData().toString());

        String type = remoteMessage.getData().get("type");

        if (type.equals("crowd")) {
            String ids = remoteMessage.getData().get("ids");
            String message = remoteMessage.getData().get("message");

            String myId = getUserId();

            if (containsMyId(ids, myId)) {
                showNotifications("Crowd Alert", message, 0, 0, type);
            }
        } else if (type.equals("lost")) {
            String name = remoteMessage.getData().get("lost_name");
            String lost_id = remoteMessage.getData().get("lost_id");
            String message = remoteMessage.getData().get("message");
            String group_id = remoteMessage.getData().get("group_id");

            String location[] = remoteMessage.getData().get("location").split(",");

            double lat = Double.parseDouble(location[0].trim());
            double lng = Double.parseDouble(location[1].trim());

            Log.e("notif", remoteMessage.getData().toString());

            if ((Integer.parseInt(getGroupId()) == Integer.parseInt(group_id)
                    && Integer.parseInt(lost_id) != Integer.parseInt(getUserId()))) {
                showNotifications(name, message, lat, lng, type);
            }
        }
    }
    private boolean containsMyId(String ids, String myId){
        if(ids.split(",").length == 0)
            return false;
        for(String id : ids.split(",")){
            if(Integer.parseInt(id.trim()) == Integer.parseInt(myId))
                return true;
        }
        return false;
    }

    private String getGroupId() {
        try {
            JSONObject userData = new JSONObject(prefs.getString(Constants.USERS_DATA, ""));
            JSONArray array = userData.getJSONArray("my_data");

            Log.e("Error", userData.toString());
            JSONObject object = array.getJSONObject(0);
            return object.getString("group_id");
        } catch (Exception e) {

        }
        return null;
    }

    private String getUserId() {
        try {
            JSONObject userData = new JSONObject(prefs.getString(Constants.USERS_DATA, ""));
            JSONArray array = userData.getJSONArray("my_data");

            Log.e("Error", userData.toString());
            JSONObject object = array.getJSONObject(0);
            return object.getString("id");
        } catch (Exception e) {

        }
        return null;
    }

    private void showNotifications(String name, String message, double lat, double lng, String type) {


        Log.e("Error", "Notification recieved");
        // String text = "";
        String intentUri = "http://maps.google.com/maps?daddr=" + lat + "," + lng + " (" + "Where the party is at" + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentUri));
        intent.setPackage("com.google.android.apps.maps");


        PendingIntent contentIntent = PendingIntent.getActivity(this,
                151, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Resources res = getResources();
        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "w01", appTitle = "Hajj Hackathon";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String desc = "Ride Available";

            NotificationChannel channel = new NotificationChannel(id, appTitle, importance);
            channel.setDescription(desc);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(this, id);
        } else {
            builder = new Notification.Builder(this);
        }
        //builder.build().flags |= Notification.FLAG_AUTO_CANCEL;


        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if(type.equals("lost")){
            builder.setContentIntent(contentIntent);
        }

        builder.setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.icon))
                .setTicker(getBaseContext().getResources().getString(R.string.app_name))
                .setSound(uri)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(name)
                .setContentText(message);


        builder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        Notification n = builder.build();

        nm.notify(151, n);
    }
}
