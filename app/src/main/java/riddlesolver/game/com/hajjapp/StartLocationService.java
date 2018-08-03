package riddlesolver.game.com.hajjapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartLocationService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context,UpdateLocationService.class);
        context.startService(newIntent);
    }
}
