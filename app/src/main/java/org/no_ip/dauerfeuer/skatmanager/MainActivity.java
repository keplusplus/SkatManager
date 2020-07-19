package org.no_ip.dauerfeuer.skatmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SkatManager";

    private int spanz;

    public List<Game> activeGames;
    private SharedPreferences sharedPrefs;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        spanz = 3;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        new LoadGames().execute(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadGames().execute(this);
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
}
