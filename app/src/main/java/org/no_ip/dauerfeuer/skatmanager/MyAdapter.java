package org.no_ip.dauerfeuer.skatmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<Game> mItemList;

    private OnItemClickListener mRemoveClickListener;
    private OnItemClickListener mStartClickListener;
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
        holder.mTextView.setText(Html.fromHtml(game.getGameName()));
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
        holder.mRoundsTextView.setText(String.format(mContext.getString(R.string.played_rounds), game.getGameRounds()));

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRemoveClickListener.onItemClick(game);
            }
        });
        holder.mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartClickListener.onItemClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    public OnItemClickListener getDeleteClickListener() {
        return mRemoveClickListener;
    }

    public void setDeleteClickListener(OnItemClickListener cl) {
        this.mRemoveClickListener = cl;
    }

    public OnItemClickListener getStartClickListener() {
        return mStartClickListener;
    }

    public void setStartClickListener(OnItemClickListener cl) {
        this.mStartClickListener = cl;
    }

    // Subclass ViewHolder

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTextView;
        protected TextView mPlayersTextView;
        protected TextView mRoundsTextView;
        protected Button mDeleteButton;
        protected Button mStartButton;

        public MyViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.my_card_title);
            mPlayersTextView =  v.findViewById(R.id.my_card_players);
            mRoundsTextView = v.findViewById(R.id.my_card_rounds);
            mDeleteButton = v.findViewById(R.id.my_card_remove_button);
            mStartButton = v.findViewById(R.id.my_card_start_button);
        }
    }
}