package org.no_ip.dauerfeuer.skatmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SkatManager";

    protected boolean isOnline;

    private int spanz;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private DataLoader mDataLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DataLoader.registerNetworkCallback(this);

        mDataLoader = new DataLoader(this);
        mDataLoader.refreshMainActivity();

        // new LoadGames().execute(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDataLoader.refreshMainActivity();
        // new LoadGames().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.main_activity_menu_prefs) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*public class LoadGames extends AsyncTask<Context, Void, List<Game>> {
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
    }*/
}
