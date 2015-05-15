package com.synergy.ribbit.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.synergy.ribbit.R;
import com.synergy.ribbit.adapters.UserAdapter;
import com.synergy.ribbit.utils.ParseConstants;

import java.util.List;


public class EditFriendsActivity extends ActionBarActivity {

    private final String TAG = EditFriendsActivity.class.getSimpleName();
    private List<ParseUser> mUsers;
    private ProgressBar mProgressBar;
    private GridView mGridView;
    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
                if (mGridView.isItemChecked(position)) {
                    //Adding friends relation
                    mFriendsRelation.add(mUsers.get(position));
                    checkImageView.setVisibility(View.VISIBLE);
                } else {
                    //Removing friends relation
                    mFriendsRelation.remove(mUsers.get(position));
                    checkImageView.setVisibility(View.INVISIBLE);
                }
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        mProgressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                mProgressBar.setVisibility(View.GONE);
                if (e == null) {
                    //Success
                    mUsers = parseUsers;
                    String[] usernames = new String[mUsers.size()];
                    int index = 0;
                    for (ParseUser user : mUsers) {
                        usernames[index++] = user.getUsername();
                    }
                    if (mGridView.getAdapter() == null) {
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                    addFriendsCheckmarks();
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void addFriendsCheckmarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    // list returned - look for a match
                    for (int index = 0; index < mUsers.size(); index++) {
                        ParseUser user = mUsers.get(index);
                        for (ParseUser friend : parseUsers) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                mGridView.setItemChecked(index, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

}
