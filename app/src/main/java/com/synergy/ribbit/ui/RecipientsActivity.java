package com.synergy.ribbit.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.synergy.ribbit.R;
import com.synergy.ribbit.adapters.UserAdapter;
import com.synergy.ribbit.utils.FileHelper;
import com.synergy.ribbit.utils.ParseConstants;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private final String TAG = RecipientsActivity.class.getSimpleName();

    private GridView mGridView;
    private ProgressBar mProgressBar;
    private MenuItem mSendMenuItem;
    private Uri mMediaUri;

    private ParseRelation<ParseUser> mFriendsRelation;
    private ParseUser mCurrentUser;
    private List<ParseUser> mFriends;
    private String mFileType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);


        mProgressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        mGridView.setOnItemClickListener(this);
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

    }


    @Override
    protected void onResume() {
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
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    } else {
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(getString(R.string.error_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send:
                ParseObject message = createMessage();
                if (message == null) {
                    // error
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(getString(R.string.error_selecting_file))
                            .setTitle(getString(R.string.error_selecting_file_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    send(message);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(RecipientsActivity.this, getString(R.string.success_message), Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(getString(R.string.error_sending_message))
                            .setTitle(getString(R.string.error_selecting_file_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void sendPushNotifications() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipiensIds());

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, mCurrentUser.getUsername());
        message.put(ParseConstants.KEY_RECIPIENTS_IDS, getRecipiensIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        if (fileBytes == null) {
            return null;
        } else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }
            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);
            return message;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
        if (mGridView.getCheckedItemCount() > 0){
            mSendMenuItem.setVisible(true);
        } else {
            mSendMenuItem.setVisible(false);
        }
        if (mGridView.isItemChecked(position)) {
            checkImageView.setVisibility(View.VISIBLE);
        } else {
            checkImageView.setVisibility(View.INVISIBLE);
        }
    }

    public ArrayList<String> getRecipiensIds() {
        ArrayList<String> recipientsIds = new ArrayList<>();
        for (int i = 0; i < mGridView.getCount(); i++) {
            if (mGridView.isItemChecked(i)) {
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientsIds;
    }
}
