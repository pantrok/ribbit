package com.synergy.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;
import com.synergy.ribbit.utils.ParseConstants;

/**
 * Created by daniel on 19/03/15.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "X01py6hmG3sWr0ZfOlSaIfSuJwCM9w4maJv2ZD0O", "Huf0bWUWdzZ541Xi30I1OsFPfnBm6S6JJhU2rN73");

    }

    public static void updateParseInstallation (ParseUser user) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }

}
