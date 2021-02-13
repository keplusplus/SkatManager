package org.no_ip.dauerfeuer.skatmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.skydoves.androidveil.VeilRecyclerFrameView;

import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static final String TAG = "DataLoader";

    private final Context c;
    private RequestQueue mQueue;
    private final String mBaseUrl;

    private TextView mNoGamesTextView;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private VeilRecyclerFrameView mVeilRecyclverView;

    public DataLoader(Context c) {
        this.c = c;
        mQueue = Volley.newRequestQueue(c);
        mBaseUrl = "http://10.0.2.2:8080/skat-api/v1";

        mNoGamesTextView = ((Activity) c).findViewById(R.id.tv_no_games);

        mRecyclerView = ((Activity) c).findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mVeilRecyclverView = ((Activity) c).findViewById(R.id.my_veil_recycler);
        mVeilRecyclverView.setLayoutManager(new LinearLayoutManager(c));
    }

    public void refreshMainActivity() {
        MainActivityRefresher refresher = new MainActivityRefresher();
        refresher.execute();
    }

    private class MainActivityRefresher extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            final List<Game> games = new ArrayList<>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
            boolean online = sharedPreferences.getBoolean("useOnline", false);
            final RecyclerView.Adapter mAdapter = new MyAdapter(games, c);
            mRecyclerView.setAdapter(mAdapter);

            games.addAll(Game.readGames(c));

            if(online && ((MainActivity) c).isOnline) {
                StringRequest request = new StringRequest(Request.Method.GET, mBaseUrl + "/games", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        games.addAll(Game.parseServerResponse(response));
                        mAdapter.notifyDataSetChanged();
                        Log.v(TAG, String.format("Request succeded, list has now %d entries.", games.size()));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "Volley: Error occured during request.\n");
                        error.printStackTrace();
                        mAdapter.notifyDataSetChanged();
                    }
                });

                mQueue.add(request);
            } else {
                mAdapter.notifyDataSetChanged();
                showHideNoGamesTextView(games.size());
            }


            return null;
        }

        private void showHideNoGamesTextView(final int gameCount) {
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(gameCount < 1) {
                        if(mNoGamesTextView.getVisibility() != View.VISIBLE) mNoGamesTextView.setVisibility(View.VISIBLE);
                    } else {
                        if(mNoGamesTextView.getVisibility() != View.GONE) mNoGamesTextView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public static void registerNetworkCallback(final Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                ((MainActivity) c).isOnline = true;
            }

            @Override
            public void onLost(@NonNull Network network) {
                ((MainActivity) c).isOnline = false;
            }
        });
    }
}
