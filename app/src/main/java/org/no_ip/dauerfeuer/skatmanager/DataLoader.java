package org.no_ip.dauerfeuer.skatmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static final String TAG = "DataLoader";

    private final Context c;
    private RequestQueue mQueue;
    private final String mBaseUrl;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    public DataLoader(Context c) {
        this.c = c;
        mQueue = Volley.newRequestQueue(c);
        mBaseUrl = "http://10.0.2.2:8080/skat-api/v1";

        mRecyclerView = ((Activity) c).findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(mLayoutManager);
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

            if(online && ((MainActivity) c).isOnline) {
                StringRequest request = new StringRequest(Request.Method.GET, mBaseUrl + "/games", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonGames = new JSONArray(response);

                            for(int gameIndex = 0; gameIndex < jsonGames.length(); gameIndex++) {
                                JSONObject jsonGame = jsonGames.getJSONObject(gameIndex);
                                Game game = parseJsonGame(jsonGame);
                                games.add(game);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mAdapter.notifyDataSetChanged();
                        showHideNoGamesTextView(games);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "Volley: Error occured during request.\n");
                        error.printStackTrace();
                    }
                });

                mQueue.add(request);
            }


            return null;
        }

        private Game parseJsonGame(JSONObject jsonGame) throws JSONException {
            List<Player> players = new ArrayList<>();

            JSONArray jsonPlayers = jsonGame.getJSONArray("players");

            for(int playerIndex = 0; playerIndex < jsonPlayers.length(); playerIndex++) {
                JSONObject jsonPlayer = jsonPlayers.getJSONObject(playerIndex);
                Player player = new Player(jsonPlayer.getString("name"), jsonPlayer.getInt("points"));
                players.add(player);
            }

            int roundGames = 0;
            JSONArray jsonRounds = jsonGame.getJSONArray("rounds");
            for(int i = 0; i < jsonRounds.length(); i++) {
                JSONArray jsonRoundGames = jsonRounds.getJSONObject(i).getJSONArray("games");
                for(int j = 0; j < jsonRoundGames.length(); j++) {
                    if(jsonRoundGames.getJSONObject(j).has("value")) {
                        roundGames++;
                    }
                }
            }

            Game game = new Game(false, jsonGame.getString("name"), players, roundGames);
            return game;
        }

        private void showHideNoGamesTextView(List<Game> games) {
            final int gameCount = games.size();
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textView = ((Activity) c).findViewById(R.id.tv_no_games);
                    if(gameCount < 1) {
                        if(textView.getVisibility() != View.VISIBLE) textView.setVisibility(View.VISIBLE);
                    } else {
                        if(textView.getVisibility() != View.GONE) textView.setVisibility(View.GONE);
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
