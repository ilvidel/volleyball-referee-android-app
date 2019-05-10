package com.tonkar.volleyballreferee.ui.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.api.ApiFriendsAndRequests;
import com.tonkar.volleyballreferee.business.data.StoredUser;
import com.tonkar.volleyballreferee.interfaces.data.AsyncFriendRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ColleaguesListAdapter extends ArrayAdapter<ColleagueItem> {

    private final LayoutInflater             mLayoutInflater;
    private final StoredUserService          mStoredUserService;
    private final AsyncFriendRequestListener mListener;
    private final List<ColleagueItem>        mColleagueItems;
    private final List<ColleagueItem>        mFilteredColleagueItems;
    private final NameFilter                 mNameFilter;

    ColleaguesListAdapter(Context context, LayoutInflater layoutInflater, AsyncFriendRequestListener listener) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<>());
        mLayoutInflater = layoutInflater;
        mStoredUserService = new StoredUser(context);
        mListener = listener;
        mColleagueItems = new ArrayList<>();
        mFilteredColleagueItems = new ArrayList<>();
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredColleagueItems.size();
    }

    @Override
    public ColleagueItem getItem(int index) {
        return mFilteredColleagueItems.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        ColleagueItem colleagueItem = mFilteredColleagueItems.get(index);

        switch (colleagueItem.getItemType()) {
            case FRIEND:
                view = createFriendItem(colleagueItem);
                break;
            case RECEIVED:
                view = createReceivedFriendRequestItem(colleagueItem);
                break;
            case SENT:
                view = createSentFriendRequestItem(colleagueItem);
                break;
            default:
                break;
        }

        return view;
    }

    private View createFriendItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.friend_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(colleagueItem.getFriend().getPseudo());

        Button removeFriendButton = view.findViewById(R.id.remove_friend_button);
        removeFriendButton.setOnClickListener(button -> {
            mStoredUserService.removeFriend(colleagueItem.getFriend(), mListener);
        });

        return view;
    }

    private View createReceivedFriendRequestItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.received_friend_request_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(String.format(Locale.getDefault(), getContext().getString(R.string.received_colleague_request), colleagueItem.getFriendRequest().getSenderPseudo()));

        Button acceptFriendButton = view.findViewById(R.id.accept_friend_button);
        acceptFriendButton.setOnClickListener(button -> {
            mStoredUserService.acceptFriendRequest(colleagueItem.getFriendRequest(), mListener);
        });

        Button rejectFriendButton = view.findViewById(R.id.reject_friend_button);
        rejectFriendButton.setOnClickListener(button -> {
            mStoredUserService.rejectFriendRequest(colleagueItem.getFriendRequest(), mListener);
        });

        return view;
    }

    private View createSentFriendRequestItem(ColleagueItem colleagueItem) {
        View view = mLayoutInflater.inflate(R.layout.sent_friend_request_item, null);

        TextView text = view.findViewById(R.id.friend_text);
        text.setText(String.format(Locale.getDefault(), getContext().getString(R.string.sent_colleague_request), colleagueItem.getFriendRequest().getReceiverPseudo()));

        return view;
    }

    @Override
    public @NonNull Filter getFilter() {
        return mNameFilter;
    }

    void updateFriendsAndRequests(ApiFriendsAndRequests friendsAndRequests) {
        mColleagueItems.clear();
        mFilteredColleagueItems.clear();

        for (ApiFriendRequest friendRequest : friendsAndRequests.getReceivedFriendRequests()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.RECEIVED, friendRequest));
        }

        for (ApiFriendRequest friendRequest : friendsAndRequests.getSentFriendRequests()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.SENT, friendRequest));
        }

        for (ApiFriend friend : friendsAndRequests.getFriends()) {
            mColleagueItems.add(new ColleagueItem(ColleagueItem.ItemType.FRIEND, friend));
        }

        mFilteredColleagueItems.addAll(mColleagueItems);
        notifyDataSetChanged();
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = mColleagueItems;
                results.count = mColleagueItems.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ColleagueItem> matchValues = new ArrayList<>();

                for (ColleagueItem colleagueItem : mColleagueItems) {
                    if (lowerCaseText.isEmpty()) {
                        matchValues.add(colleagueItem);
                    } else {
                        switch (colleagueItem.getItemType()) {
                            case FRIEND:
                                if (colleagueItem.getFriend().getPseudo().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            case SENT:
                                if (colleagueItem.getFriendRequest().getReceiverPseudo().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            case RECEIVED:
                                if (colleagueItem.getFriendRequest().getSenderPseudo().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                                    matchValues.add(colleagueItem);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredColleagueItems.clear();

            if (results.values != null) {
                mFilteredColleagueItems.clear();
                mFilteredColleagueItems.addAll((Collection<ColleagueItem>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}