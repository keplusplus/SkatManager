package org.no_ip.dauerfeuer.skatmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Game implements Parcelable {
    private List<Player> mPlayerList;
    private String mGameName;
    private boolean mGameIsDoppelkopf;
    private int mPlayedRoundGames;
    private boolean mIsOnline;
    private byte[] mGameId;

    private Date mStartDate;

    public Game(Boolean gameIsDoppelkopf, String name, List<Player> players, int rounds, boolean isOnline) {
        this.mGameName = name;
        this.mPlayerList = players;
        this.mGameIsDoppelkopf = gameIsDoppelkopf;
        this.mPlayedRoundGames = rounds;
        this.mIsOnline = isOnline;
        mGameId = new byte[12];
        new Random().nextBytes(mGameId);
    }

    public Game(byte[] gameId, Boolean gameIsDoppelkopf, String name, List<Player> players, int rounds, boolean isOnline) {
        this(gameIsDoppelkopf, name, players, rounds, isOnline);
        this.mGameId = gameId;
    }

    public Game(Parcel in) {
        this.mGameName = in.readString();
        this.mPlayerList = new ArrayList<Player>();
        in.readList(mPlayerList, Player.class.getClassLoader());
        this.mPlayedRoundGames = in.readInt();
        this.mGameIsDoppelkopf = in.readByte() != 0;
        this.mIsOnline = in.readByte() != 0;
        in.readByteArray(mGameId);
    }

    public List<Player> getPlayerList() {
        return mPlayerList;
    }

    public String getGameName() {
        return mGameName;
    }

    public int getRoundGames() {
        return mPlayedRoundGames;
    }

    public void roundGamePlayed() {
        this.mPlayedRoundGames++;
    }

    public boolean getIsDoppelkopf() {
        return mGameIsDoppelkopf;
    }

    public boolean getIsOnline() {
        return mIsOnline;
    }

    public byte[] getGameId() {
        return mGameId;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        this.mStartDate = startDate;
    }

    public void storeGame(Context context) {
        try {
            JSONObject parent = new JSONObject();
            parent.put("game_is_doppelkopf", mGameIsDoppelkopf);
            parent.put("game_name", mGameName);
            parent.put("game_rounds", mPlayedRoundGames);
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
        if(getIsOnline()) {
            ((MainActivity) context).getDataLoader().deleteOnlineGame(this);
        } else {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String mExistingGames = sharedPreferences.getString("games", "");
                JSONArray mOldGamesJSON;
                if (!mExistingGames.equals("")) {
                    mOldGamesJSON = new JSONArray(mExistingGames);
                    for (int i = 0; i < mOldGamesJSON.length(); i++) {
                        JSONObject object = (JSONObject) mOldGamesJSON.get(i);
                        if (object.getString("game_name").equals(mGameName)) {
                            mOldGamesJSON.remove(i);
                            i = mOldGamesJSON.length();
                        }
                    }
                    sharedPreferences.edit().putString("games", mOldGamesJSON.toString()).apply();
                }

                ((MainActivity) context).getDataLoader().refreshMainActivity();
            } catch (JSONException e) {
                Log.d(MainActivity.TAG, e.getLocalizedMessage());
            }
        }
    }

    public static List<Game> readGames(Context context) {
        List<Game> gameList = new ArrayList<>();
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String mGames = sharedPreferences.getString("games", "");
            if(mGames.equals("")) return gameList;

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

                Game game;
                if(object.has("id")) {
                    String sId = object.getString("id");

                    game = new Game(hexStringToByteArray(object.getString("id")), gameIsDoppelkopf, gameName, gamePlayers, gameRounds, false);
                }
                else game = new Game(gameIsDoppelkopf, gameName, gamePlayers, gameRounds, false);
                gameList.add(game);
            }
        } catch (JSONException e) {
            Log.w(MainActivity.TAG, e.getLocalizedMessage());
            return null;
        }
        return gameList;
    }

    public static List<Game> parseServerResponse(JSONArray response) {
        List<Game> games = new ArrayList<>();

        try {
            JSONArray jsonGames = response;
            for (int gameIndex = 0; gameIndex < jsonGames.length(); gameIndex++) {
                JSONObject jsonGame = jsonGames.getJSONObject(gameIndex);

                List<Player> players = new ArrayList<>();

                JSONArray jsonPlayers = jsonGame.getJSONArray("players");

                for (int playerIndex = 0; playerIndex < jsonPlayers.length(); playerIndex++) {
                    JSONObject jsonPlayer = jsonPlayers.getJSONObject(playerIndex);
                    Player player = new Player(jsonPlayer.getString("name"), jsonPlayer.getInt("points"));
                    players.add(player);
                }

                int roundGames = 0;
                JSONArray jsonRounds = jsonGame.getJSONArray("rounds");
                for (int i = 0; i < jsonRounds.length(); i++) {
                    JSONArray jsonRoundGames = jsonRounds.getJSONObject(i).getJSONArray("games");
                    for (int j = 0; j < jsonRoundGames.length(); j++) {
                        if (jsonRoundGames.getJSONObject(j).has("value")) {
                            roundGames++;
                        }
                    }
                }

                Game game = new Game(hexStringToByteArray(jsonGame.getString("_id")), false, jsonGame.getString("name"), players, roundGames, true);

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                try {
                    Date d = isoFormat.parse(jsonGame.getString("created"));
                    game.setStartDate(d);

                } catch (ParseException e) {
                    Log.w(MainActivity.TAG, e.getLocalizedMessage());
                }

                games.add(game);
            }
        } catch (JSONException e) {
            Log.w(MainActivity.TAG, e.getLocalizedMessage());
        }

        return games;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        byte[] hexArray = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        byte[] hexChars = new byte[bytes.length * 2];
        for(int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
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
        dest.writeInt(mPlayedRoundGames);
        dest.writeByte((byte) (mGameIsDoppelkopf ? 1 : 0));
        dest.writeByte((byte) (mIsOnline ? 1 : 0));
        dest.writeByteArray(mGameId);
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