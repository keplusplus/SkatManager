package org.no_ip.dauerfeuer.skatmanager;

import android.app.AlertDialog;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<Game> mItemList;

    private Context mContext;

    public MyAdapter(List<Game> l, Context c) {
        mItemList = l;
        this.mContext = c;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_main_game, null);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Game game = mItemList.get(position);

        //Setting text view title

        // String gameName = Html.fromHtml(game.getGameName());
        if(game.getStartDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            holder.mTextView.setText(String.format("%s (%s)", game.getGameName(), dateFormat.format(game.getStartDate())));
        } else {
            holder.mTextView.setText(game.getGameName());
        }

        holder.mOnlineTextView.setText(game.getIsOnline() ? R.string.card_online_game : R.string.card_local_game);

        List<Player> mPlayers = game.getPlayerList();
        StringBuilder builder = new StringBuilder(mContext.getString(R.string.players) + ": ");
        int i = 0;
        for(Player p : mPlayers) {
            builder.append(p.getPlayerName());
            builder.append(" (");
            builder.append(p.getPlayerPoints());
            builder.append(")");
            if(i < (mPlayers.size() - 1)) builder.append(", ");
            i++;
        }
        holder.mPlayersTextView.setText(builder.toString());
        holder.mRoundsTextView.setText(String.format(mContext.getString(R.string.played_rounds), game.getRoundGames()));

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.delete_game);
                builder.setMessage(R.string.confirm_delete_game);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        game.removeGame(mContext);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        holder.mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    // Subclass ViewHolder

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTextView;
        protected TextView mOnlineTextView;
        protected TextView mPlayersTextView;
        protected TextView mRoundsTextView;
        protected Button mDeleteButton;
        protected Button mStartButton;

        public MyViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.my_card_title);
            mOnlineTextView = v.findViewById(R.id.my_card_is_online);
            mPlayersTextView =  v.findViewById(R.id.my_card_players);
            mRoundsTextView = v.findViewById(R.id.my_card_rounds);
            mDeleteButton = v.findViewById(R.id.my_card_remove_button);
            mStartButton = v.findViewById(R.id.my_card_start_button);
        }
    }
}