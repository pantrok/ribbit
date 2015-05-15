package com.synergy.ribbit.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.synergy.ribbit.adapters.MessageAdapter;
import com.synergy.ribbit.utils.ParseConstants;
import com.synergy.ribbit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 20/03/15.
 */
public class InboxFragment extends ListFragment {

    private final String TAG = InboxFragment.class.getSimpleName();
    private ProgressBar mProgressBar;
    private List<ParseObject> mMessages;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBarInbox);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.swipeRefresh1,R.color.swipeRefresh2,
                R.color.swipeRefresh3,R.color.swipeRefresh4);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveMessages();

    }

    private void retrieveMessages() {
        mProgressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_IDS, ParseUser.getCurrentUser().getObjectId());
        query.orderByAscending(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                mProgressBar.setVisibility(View.GONE);
                if (e == null) {
                    //We found messages!
                    mMessages = parseObjects;
                    String[] usernames = new String[mMessages.size()];
                    int index = 0;
                    for (ParseObject message : mMessages) {
                        usernames[index++] = message.getString(ParseConstants.KEY_SENDER_NAME);
                    }
                    if (getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mMessages);
                        setListAdapter(adapter);
                    } else {
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);
                    }
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        Uri fileUri = Uri.parse(file.getUrl());

        if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
            // view the image
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        } else {
            // view the video
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri,"video/*");
            startActivity(intent);
        }

        // Delete it
        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENTS_IDS);
        if (ids.size() == 1) {
            // Only one recipient - delete the whole thing!
            message.deleteInBackground();
        } else {
            // remove the recipient and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());
            ArrayList<String> idsToRemove = new ArrayList<>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
            message.removeAll(ParseConstants.KEY_RECIPIENTS_IDS, idsToRemove);
            message.saveInBackground();
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
        }
    };
}
