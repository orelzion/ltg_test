package com.orel.ltg;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yirmy on 09/04/2015.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private OnFriendClickListener mListener;
    private List<FacebookUser> mFriendsList;

    public List<FacebookUser> getFriendsList() {
        return mFriendsList;
    }

    public void setFriendsList(List<FacebookUser> mFriendsList) {
        this.mFriendsList = mFriendsList;
        notifyDataSetChanged();
    }

    public void setListener(OnFriendClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_item, viewGroup, false);
        return new FriendsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder friendsViewHolder, final int i) {
        friendsViewHolder.textView.setText(mFriendsList.get(i).getName());
        friendsViewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.OnFriendClicked(mFriendsList.get(i).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFriendsList == null ? 0 : mFriendsList.size();
    }

    public interface OnFriendClickListener {
        public void OnFriendClicked(String friendID);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
