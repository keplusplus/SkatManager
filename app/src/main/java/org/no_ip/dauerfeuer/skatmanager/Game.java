package org.no_ip.dauerfeuer.skatmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game implements Parcelable {
    private List<Player> mPlayerList;
    private String mGameName;
    private boolean mGameIsDoppelkopf;
    private int mGameRounds;

    public Game(Boolean gameIsDoppelkopf, String name, List<Player> players, int rounds) {
        this.mGameName = name;
        this.mPlayerList = players;
        this.mGameIsDoppelkopf = gameIsDoppelkopf;
        this.mGameRounds = rounds;
    }

    public Game(Parcel in) {
        this.mGameName = in.readString();
        this.mPlayerList = new ArrayList<Player>();
        in.readList(mPlayerList, Player.class.getClassLoader());
        this.mGameRounds = in.readInt();
        this.mGameIsDoppelkopf = in.readByte() != 0;
    }

    public List<Player> getPlayerList() {
        return mPlayerList;
    }

    public String getGameName() {
        return mGameName;
    }

    public int getGameRounds() {
        return mGameRounds;
    }

    public void roundPlayed() {
        this.mGameRounds++;
    }

    public boolean getIsDoppelkopf() {
        return mGameIsDoppelkopf;
    }

    public void storeGame(Context context) {
        try {
            JSONObject parent = new JSONObject();
            parent.put("game_is_doppelkopf", mGameIsDoppelkopf);
            parent.put("game_name", mGameName);
            parent.put("game_rounds", mGameRounds);
            JSONArray playerArray = new JSONArray();
            for(Player p : mPlayerList) {
                playerArray.put(new JSONObject().put("player_name", p.getPlayerName()).put("player_points", p.getPlayerPoints()));
            }
            parent.put("players", playerArray);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String mExistingGames = sharedPreferences.getString("games", "");
            JSONArray mOldGamesJSON;
            if(!mExistingGames.equals("")) {
                mOldGamesJSON = new JSONArray(mExistingGames);
                for(int i = 0; i < mOldGamesJSON.length(); i++) {
                    JSONObject object = (JSONObject) mOldGamesJSON.get(i);
                    if(object.getString("game_name").equals(mGameName)) {
                        mOldGamesJSON.remove(i);
                        i = mOldGamesJSON.length();
                    }
                }
                mOldGamesJSON.put(parent);
            } else {
                mOldGamesJSON = new JSONArray();
                mOldGamesJSON.put(parent);
            }
            sharedPreferences.edit().putString("games", mOldGamesJSON.toString()).apply();
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.getLocalizedMessage());
        }
    }

    public void removeGame(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String mExistingGames = sharedPreferences.getString("games", "");
            JSONArray mOldGamesJSON;
            if(!mExistingGames.equals("")) {
                 mOldGamesJSON = new JSONArray(mExistingGames);
                 for(int i = 0; i < mOldGamesJSON.length(); i++) {
                     JSONObject object = (JSONObject) mOldGamesJSON.get(i);
                     if(object.getString("game_name").equals(mGameName)) {
                         mOldGamesJSON.remove(i);
                         i = mOldGamesJSON.length();
                     }
                 }
                 sharedPreferences.edit().putString("games", mOldGamesJSON.toString()).apply();
            }
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.getLocalizedMessage());
        }
    }

    public static List<Game> readGames(Context context) {
        List<Game> gameList = new ArrayList<>();
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String mGames = sharedPreferences.getString("games", "");
            if(mGames.equals("")) return null;

            JSONArray games = new JSONArray(mGames);
            for(int i = 0; i < games.length(); i++) {
                JSONObject object = (JSONObject) games.get(i);
                boolean gameIsDoppelkopf = object.getBoolean("game_is_doppelkopf");
                String gameName = object.getString("game_name");
                int gameRounds = object.getInt("game_rounds");
                List<Player> gamePlayers = new ArrayList<>();
                JSONArray players = (JSONArray) object.get("players");
                for(int j = 0; j < players.length(); j++) {
                    JSONObject playerObject = (JSONObject) players.get(j);
                    Player player = new Player(playerObject.getString("player_name"), playerObject.getInt("player_points"));
                    gamePlayers.add(player);
                }

                Game game = new Game(gameIsDoppelkopf, gameName, gamePlayers, gameRounds);
                gameList.add(game);
            }
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, e.getLocalizedMessage());
            return null;
        }
        return gameList;
    }



    //Parcelable part

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGameName);
        dest.writeList(mPlayerList);
        dest.writeInt(mGameRounds);
        dest.writeByte((byte) (mGameIsDoppelkopf ? 1 : 0));
    }

    public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };
}