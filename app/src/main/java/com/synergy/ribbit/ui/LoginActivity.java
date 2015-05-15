package com.synergy.ribbit.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.synergy.ribbit.R;
import com.synergy.ribbit.RibbitApplication;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener{

    protected TextView mSignUpTextView;
    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mUsername = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(this);
        mSignUpTextView = (TextView) findViewById(R.id.signUpText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onClick(View v) {
        String username = !TextUtils.isEmpty(mUsername.getText()) ? mUsername.getText().toString().trim() : "";
        String password = !TextUtils.isEmpty(mPassword.getText()) ? mPassword.getText().toString().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.login_error_message))
                    .setTitle(getString(R.string.login_error_title))
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(getString(R.string.message_processing));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            ParseUser.logInInBackground(username,password,new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    mProgressDialog.dismiss();
                    if (e == null) {
                        RibbitApplication.updateParseInstallation(parseUser);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(e.getMessage())
                                .setTitle(getString(R.string.login_error_title))
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }
}
