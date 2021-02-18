package org.no_ip.dauerfeuer.skatmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.skydoves.androidveil.VeilRecyclerFrameView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static final String TAG = "DataLoader";

    private final Context c;
    private RequestQueue mQueue;
    private final String mBaseUrl;

    private final List<Game> mGames;

    private TextView mNoGamesTextView;

    private final VeilRecyclerFrameView mRecyclerView;
    private final RecyclerView.Adapter mAdapter;

    public DataLoader(Context c) {
        this.c = c;
        mQueue = Volley.newRequestQueue(c);
        mBaseUrl = "http://10.0.2.2:8080/skat-api/v1";

        mGames = new ArrayList<>();

        mNoGamesTextView = ((Activity) c).findViewById(R.id.tv_no_games);

        mRecyclerView = ((Activity) c).findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        mAdapter = new MyAdapter(mGames, c);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addVeiledItems(3); // TODO: Veiled items impact scrolling range, even when invisible
        mRecyclerView.unVeil();
    }

    public void refreshMainActivity() {
        mGames.clear();
        mRecyclerView.veil();
        ((Activity) c).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) c).findViewById(R.id.tv_no_games).setVisibility(View.GONE);
            }
        });
        MainActivityRefresher refresher = new MainActivityRefresher();
        refresher.execute();
    }

    public void deleteOnlineGame(Game game) {
        mRecyclerView.veil();
        StringRequest request = new StringRequest(Request.Method.DELETE, String.format("%s/games/%s", mBaseUrl, Game.byteArrayToHexString(game.getGameId())), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                refreshMainActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshMainActivity();
            }
        });

        mQueue.add(request);
    }

    private class MainActivityRefresher extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
            boolean online = sharedPreferences.getBoolean("useOnline", false);

            mGames.addAll(Game.readGames(c));

            if(online && ((MainActivity) c).isOnline) {
                JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, mBaseUrl + "/games", null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mGames.addAll(Game.parseServerResponse(response));
                        mAdapter.notifyDataSetChanged();
                        showHideNoGamesTextView(mGames.size());
                        finishSwipeRefresh();
                        mRecyclerView.unVeil();

                        Log.v(TAG, String.format("Request succeeded, list has now %d entries.", mGames.size()));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "Volley: Error occurred during request.\n");

                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle(R.string.server_error);
                        builder.setMessage(R.string.server_error_details);
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();

                        error.printStackTrace();
                        mAdapter.notifyDataSetChanged();
                        showHideNoGamesTextView(mGames.size());
                        finishSwipeRefresh();
                        mRecyclerView.unVeil();
                    }
                });

                mQueue.add(request);
            } else if(online && !((MainActivity) c).isOnline) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle(R.string.not_connected);
                builder.setMessage(R.string.not_connected_details);
                builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                ((MainActivity) c).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        builder.create().show();
                    }
                });

                mAdapter.notifyDataSetChanged();
                showHideNoGamesTextView(mGames.size());
                finishSwipeRefresh();
                mRecyclerView.unVeil();
            } else {
                mAdapter.notifyDataSetChanged();
                showHideNoGamesTextView(mGames.size());
                finishSwipeRefresh();
                mRecyclerView.unVeil();
            }

            return null;
        }

        private void showHideNoGamesTextView(final int gameCount) {
            Log.v(TAG, "showHideNoGamesTextView called with gameCount " + gameCount);
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(gameCount < 1) {
                        if(mNoGamesTextView.getVisibility() != View.VISIBLE) mNoGamesTextView.setVisibility(View.VISIBLE);
                    } else {
                        // TODO: Something is still wrong here, more testing needed...
                        if(mNoGamesTextView.getVisibility() != View.GONE) mNoGamesTextView.setVisibility(View.GONE);
                    }
                }
            });
        }

        private void finishSwipeRefresh() {
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((SwipeRefreshLayout) ((Activity) c).findViewById(R.id.swipe_refresh_layout)).setRefreshing(false);
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
