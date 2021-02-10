package org.no_ip.dauerfeuer.skatmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SkatManager";

    private int spanz;

    public List<Game> activeGames;

    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerViewOnline;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mLayoutManagerOnline;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mAdapterOnline;

    private TextView mTextViewOnline;
    private TextView mTextViewOnlineEmptyList;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerViewOnline = findViewById(R.id.my_online_recycler_view);
        mLayoutManagerOnline = new LinearLayoutManager(this);
        mRecyclerViewOnline.setLayoutManager(mLayoutManagerOnline);

        mTextViewOnline = findViewById(R.id.tv2_online);
        mTextViewOnlineEmptyList = findViewById(R.id.tv_no_online_games);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadGames().execute(MainActivity.this);
            }
        });

        spanz = 3;

        new LoadGames().execute(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadGames().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mainactivity_menu_preferences:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addPlayer(View view) {
        Button b = (Button) view;
        Button b2 = findViewById(R.id.button_remove);
        b2.setEnabled(true);

        spanz++;
        switch (spanz) {
            case 4:
                EditText et4 = findViewById(getResources().getIdentifier("edit_player4", "id", getPackageName()));
                et4.setVisibility(View.VISIBLE);
                break;
            case 5:
                EditText et5 = findViewById(getResources().getIdentifier("edit_player5", "id", getPackageName()));
                et5.setVisibility(View.VISIBLE);
                break;
            case 6:
                b.setEnabled(false);
                EditText et6 = findViewById(getResources().getIdentifier("edit_player6", "id", getPackageName()));
                et6.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void removePlayer(View view) {
        Button b = (Button) view;
        Button b2 = findViewById(R.id.button_add);
        b2.setEnabled(true);
        spanz--;
        switch (spanz) {
            case 3:
                b.setEnabled(false);
                EditText et4 = findViewById(getResources().getIdentifier("edit_player4", "id", getPackageName()));
                et4.setText("");
                et4.setVisibility(View.GONE);
                break;
            case 4:
                RadioGroup group = findViewById(R.id.game_type);
                if(group.getCheckedRadioButtonId() == R.id.radio_doppelkopf) {
                    b.setEnabled(false);
                }
                EditText et5 = findViewById(getResources().getIdentifier("edit_player5", "id", getPackageName()));
                et5.setText("");
                et5.setVisibility(View.GONE);
                break;
            case 5:
                EditText et6 = findViewById(getResources().getIdentifier("edit_player6", "id", getPackageName()));
                et6.setText("");
                et6.setVisibility(View.GONE);
                break;
        }
    }

    public void onSkat(View view) {
        spanz = 3;
        EditText et1 = findViewById(R.id.edit_player4);
        et1.setVisibility(View.GONE);
        et1.setText("");
        EditText et2 = findViewById(R.id.edit_player5);
        et2.setVisibility(View.GONE);
        et2.setText("");
        EditText et3 = findViewById(R.id.edit_player6);
        et3.setVisibility(View.GONE);
        et3.setText("");
        findViewById(R.id.button_remove).setEnabled(false);
        findViewById(R.id.button_add).setEnabled(true);
    }

    public void onDoppelkopf(View view) {
        spanz = 4;
        EditText et1 = findViewById(R.id.edit_player4);
        et1.setVisibility(View.VISIBLE);
        EditText et2 = findViewById(R.id.edit_player5);
        et2.setVisibility(View.GONE);
        et2.setText("");
        EditText et3 = findViewById(R.id.edit_player6);
        et3.setVisibility(View.GONE);
        et3.setText("");
        findViewById(R.id.button_remove).setEnabled(false);
        findViewById(R.id.button_add).setEnabled(true);
    }

    public void addGame(View v) {
        Game mNewGame;
        List<Player> mPlayerList = new ArrayList<>();
        String mGameDescription;
        boolean gameIsDoppelkopf;

        EditText mEditDescription = findViewById(R.id.edit_description);
        EditText[] mEditPlayers = new EditText[spanz];
        int i = 0;
        for(EditText mEditPlayer : mEditPlayers) {
            i++;
            mEditPlayer = findViewById(getResources().getIdentifier("edit_player" + i, "id", getPackageName()));
            mEditPlayers[i - 1] = mEditPlayer;
            if(mEditPlayer.getText().toString().trim().length() < 1) {
                errorDialog(getString(R.string.player_names_missing));
                return;
            }
            mPlayerList.add(new Player(mEditPlayer.getText().toString().trim()));
        }
        for(EditText mEditPlayer : mEditPlayers) {
            mEditPlayer.setText("");
        }

        gameIsDoppelkopf = ((RadioGroup) findViewById(R.id.game_type)).getCheckedRadioButtonId() == R.id.radio_doppelkopf;

        if(mEditDescription.getText().toString().trim().length() < 1) {
            if(gameIsDoppelkopf) {
                mGameDescription = getString(R.string.doppelkopf);
            } else {
                mGameDescription = getString(R.string.skat);
            }
        } else {
            mGameDescription = mEditDescription.getText().toString().trim();
            mEditDescription.setText("");
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        mGameDescription = mGameDescription.concat(" (" + format.format(date) + ")");

        mNewGame = new Game(gameIsDoppelkopf, mGameDescription, mPlayerList, 0);
        mNewGame.storeGame(this);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("game", mNewGame);
        startActivity(intent);
    }

    public void errorDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error);
        builder.setMessage(text);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public class LoadGames extends AsyncTask<Context, Void, List<Game>> {
        @Override
        protected List<Game> doInBackground(Context... c) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c[0]);
            if(sharedPreferences.getBoolean("use_network", false)) {
                new LoadOnlineGames().execute(c[0]);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mTextViewOnline.getVisibility() != View.GONE) mTextViewOnline.setVisibility(View.GONE);
                        if(mTextViewOnlineEmptyList.getVisibility() != View.GONE) mTextViewOnlineEmptyList.setVisibility(View.GONE);
                    }
                });
            }

            List<Game> gameList = Game.readGames(c[0]);
            if(gameList == null || gameList.size() < 1) runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.tv_no_games).setVisibility(View.VISIBLE);
                }
            });
            else runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.tv_no_games).setVisibility(View.GONE);
                }
            });
            return gameList;
        }

        @Override
        protected void onPostExecute(List<Game> games) {
            mAdapter = new MyAdapter(games, MainActivity.this);
            ((MyAdapter) mAdapter).setDeleteClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(final Game item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.delete_game);
                    builder.setMessage(R.string.confirm_delete_game);
                    builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            item.removeGame(MainActivity.this);
                            new LoadGames().execute(MainActivity.this);
                        }
                    });
                    builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            ((MyAdapter) mAdapter).setStartClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(Game item) {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("game", item);
                    MainActivity.this.startActivity(intent);
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public class LoadOnlineGames extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... c) {
            final String baseUrl = "http://10.0.2.2:8080/skat-api/v1";

            RequestQueue queue = Volley.newRequestQueue(c[0]);
            StringRequest request = new StringRequest(Request.Method.GET, baseUrl + "/games", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        List<Game> games = new ArrayList<>();
                        JSONArray jsonGames = new JSONArray(response);
                        for(int i = 0; i < jsonGames.length(); i++) {
                            JSONObject jsonGame = jsonGames.getJSONObject(i);
                            List<Player> players = new ArrayList<>();
                            JSONArray jsonPlayers = jsonGame.getJSONArray("players");
                            for(int ii = 0; ii < jsonPlayers.length(); ii++) {
                                JSONObject jsonPlayer = jsonPlayers.getJSONObject(ii);
                                Player player = new Player(jsonPlayer.getString("name"), jsonPlayer.getInt("points"));
                                players.add(player);
                            }

                            JSONArray jsonRounds = jsonGame.getJSONArray("rounds");
                            int roundGames = 0;
                            for(int ii = 0; ii < jsonRounds.length(); ii++) {
                                JSONArray jsonRoundGames = jsonRounds.getJSONObject(ii).getJSONArray("games");
                                for(int iii = 0; iii < jsonRoundGames.length(); iii++) {
                                    if(jsonRoundGames.getJSONObject(iii).has("value")) {
                                        roundGames++;
                                    }
                                }
                            }

                            Game game = new Game(false, jsonGame.getString("name"), players, roundGames);
                            games.add(game);
                        }

                        if(games == null | games.size() < 1) runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewOnlineEmptyList.setVisibility(View.VISIBLE);
                            }
                        });
                        else runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewOnlineEmptyList.setVisibility(View.GONE);
                            }
                        });

                        mAdapterOnline = new MyAdapter(games, MainActivity.this);
                        mRecyclerViewOnline.setAdapter(mAdapterOnline);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mTextViewOnline.getVisibility() != View.VISIBLE) mTextViewOnline.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch(JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if(mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            queue.add(request);

            return null;
        }
    }
}
