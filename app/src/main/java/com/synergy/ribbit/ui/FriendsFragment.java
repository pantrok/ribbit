package com.synergy.ribbit.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.synergy.ribbit.adapters.UserAdapter;
import com.synergy.ribbit.utils.ParseConstants;
import com.synergy.ribbit.R;

import java.util.List;

/**
 * Created by daniel on 20/03/15.
 */
public class FriendsFragment extends Fragment {

    private final String TAG = FriendsFragment.class.getSimpleName();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private List<ParseUser> mFriends;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_grid, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pbHeaderProgress);
        mGridView = (GridView) rootView.findViewById(R.id.friendsGrid);
        TextView emptyTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                mProgressBar.setVisibility(View.GONE);
                if (e == null) {
                    mFriends = parseUsers;
                    String[] usernames = new String[mFriends.size()];
                    int index = 0;
                    for (ParseUser user : mFriends) {
                        usernames[index++] = user.getUsername();
                    }
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(e.getMessage())
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

}
