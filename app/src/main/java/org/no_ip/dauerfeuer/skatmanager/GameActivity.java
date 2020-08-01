package org.no_ip.dauerfeuer.skatmanager;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {
    private Game mGame;

    private Map<Player, TextView> mPlayerPointViews;
    private TextView mCurrentRoundView, mGiverView;
    int mGiverIndex, mSuspenderIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        mGame = intent.getParcelableExtra("game");

        mPlayerPointViews = new HashMap<>();
        ViewGroup layout = findViewById(R.id.player_points_layout);
        for(Player p : mGame.getPlayerList()) {
            int mPlayerPoints = p.getPlayerPoints();
            String mPlayerName = p.getPlayerName();
            TextView tv = (TextView) View.inflate(this, R.layout.player_points_textview, null);
            tv.setText(String.format(getString(R.string.current_points_content), mPlayerName, mPlayerPoints));
            layout.addView(tv);
            mPlayerPointViews.put(p, tv);
        }

        mCurrentRoundView = findViewById(R.id.current_round_view);
        mCurrentRoundView.setText(String.format(getString(R.string.current_round), mGame.getGameRounds() + 1));
        mGiverView = findViewById(R.id.giver_view);

        mGiverIndex = mGame.getGameRounds() % mGame.getPlayerList().size();
        if(mGame.getPlayerList().size() <= 4) {
            mGiverView.setText(String.format(getString(R.string.player_giving), mGame.getPlayerList().get(mGiverIndex).getPlayerName()));
        } else {
            mSuspenderIndex = (mGiverIndex >= 1 ? mGiverIndex - 1 : 4);
            mGiverView.setText(String.format(getString(R.string.player_giving_5), mGame.getPlayerList().get(mGiverIndex).getPlayerName(), mGame.getPlayerList().get(mSuspenderIndex).getPlayerName()));
        }

        if(mGame.getIsDoppelkopf()) {
            loadForDoppelkopf();
        } else {
            loadForSkat();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGame.storeGame(this);
    }

    private void loadForSkat() {
        ViewGroup mainLayout = findViewById(R.id.ingame_layout);
        mainLayout.addView(View.inflate(this, R.layout.sublayout_skat, null));

        refreshSpinner();
    }

    private void refreshSpinner() {
        List<Player> playerPlayingList = new ArrayList<>(mGame.getPlayerList());
        if(mGame.getPlayerList().size() >= 4) playerPlayingList.remove(mGiverIndex);
        if(mGame.getPlayerList().size() >= 5) playerPlayingList.remove(mSuspenderIndex);
        String[] names = new String[playerPlayingList.size()];
        int i = 0;
        for(Player p : playerPlayingList) {
            names[i] = p.getPlayerName();
            i++;
        }

        Spinner playerSpinner = findViewById(R.id.spinner_players);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        playerSpinner.setAdapter(adapter);
    }

    private void loadForDoppelkopf() {

    }

    public void onInsert(View v) {
        Spinner playerSpinner = findViewById(R.id.spinner_players);
        EditText pointsEdit = findViewById(R.id.points_edit_text);
        RadioGroup radioGroup = findViewById(R.id.radiogroup_winloss);
        boolean won = radioGroup.getCheckedRadioButtonId() == R.id.radio_win;

        Player player = null;
        String selected = playerSpinner.getSelectedItem().toString();
        for(Player p : mGame.getPlayerList()) {
            String name = p.getPlayerName();
            if(selected.equals(name)) {
                player = p;
            }
        }
        if(player == null) return;

        if(won) {
            List<Player> playerList = mGame.getPlayerList();
            List<Player> pointGetterList = new ArrayList<>(mGame.getPlayerList());
            switch(playerList.size()) {
                case 3:
                    pointGetterList.remove(player);
                    break;
                case 4:
                    pointGetterList.remove(playerList.get(mGiverIndex));
                    pointGetterList.remove(player);
                    break;
                case 5:
                    pointGetterList.remove(player);
                    if(mGiverIndex >= 1) {
                        pointGetterList.remove(playerList.get(mGiverIndex - 1));
                    } else {
                        pointGetterList.remove(playerList.get(4));
                    }
                    pointGetterList.remove(playerList.get(mGiverIndex));
                    break;
            }

            Iterator<Player> it = pointGetterList.iterator();
            while(it.hasNext()) {
                Player p = it.next();
                p.setPlayerPoints(p.getPlayerPoints() + Integer.parseInt(pointsEdit.getText().toString()));
            }
        } else {
            player.setPlayerPoints(player.getPlayerPoints() + Integer.parseInt(pointsEdit.getText().toString()));
        }

        mGame.roundPlayed();
        refreshPoints();
    }

    public void refreshPoints() {
        for(Player p : mGame.getPlayerList()) {
            TextView tv = mPlayerPointViews.get(p);
            tv.setText(String.format(getString(R.string.current_points_content), p.getPlayerName(), p.getPlayerPoints()));
        }

        mCurrentRoundView.setText(String.format(getString(R.string.current_round), mGame.getGameRounds() + 1));
        mGiverIndex = mGame.getGameRounds() % mGame.getPlayerList().size();
        if(mGame.getPlayerList().size() <= 4) {
            mGiverView.setText(String.format(getString(R.string.player_giving), mGame.getPlayerList().get(mGiverIndex).getPlayerName()));
        } else {
            mSuspenderIndex = (mGiverIndex >= 1 ? mGiverIndex - 1 : 4);
            mGiverView.setText(String.format(getString(R.string.player_giving_5), mGame.getPlayerList().get(mGiverIndex).getPlayerName(), mGame.getPlayerList().get(mSuspenderIndex).getPlayerName()));
        }
        refreshSpinner();

        mGame.storeGame(this);
    }
}
