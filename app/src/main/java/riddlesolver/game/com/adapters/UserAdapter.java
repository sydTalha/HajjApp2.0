package riddlesolver.game.com.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import riddlesolver.game.com.hajjapp.R;
import riddlesolver.game.com.hajjapp.UsersList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {


    Context context;
    ArrayList<User> data;

    TrackPerson listener;

    public UserAdapter(Context context, ArrayList<User> data) {
        this.context = context;
        this.data = data;
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new Holder(inflater.inflate(R.layout.user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        final User user = data.get(position);
        holder.name.setText(user.getName());
        holder.destination.setText(user.getDestination());
        holder.applicationNumber.setText("ApplicationNumber: " + user.getApplicationNumber());

        holder.track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTrackPerson(user.getId());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView track;
        TextView applicationNumber;
        TextView destination;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            track = v.findViewById(R.id.track);
            destination = v.findViewById(R.id.destination);
            applicationNumber = v.findViewById(R.id.application_number);
        }
    }

    public void setOnTrackPerson(TrackPerson trackPerson) {
        this.listener = trackPerson;
    }
}
