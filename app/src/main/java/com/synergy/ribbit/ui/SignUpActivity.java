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

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.synergy.ribbit.R;
import com.synergy.ribbit.RibbitApplication;


public class SignUpActivity extends ActionBarActivity implements View.OnClickListener {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected Button mSignUpButton;
    protected Button mCancelButton;
    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mUsername = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mEmail = (EditText) findViewById(R.id.emailField);
        mSignUpButton = (Button) findViewById(R.id.signupButton);
        mSignUpButton.setOnClickListener(this);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mSignUpButton.equals(v)) {
            String username = !TextUtils.isEmpty(mUsername.getText()) ? mUsername.getText().toString().trim() : "";
            String password = !TextUtils.isEmpty(mPassword.getText()) ? mPassword.getText().toString().trim() : "";
            String email = !TextUtils.isEmpty(mEmail.getText()) ? mEmail.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.signup_error_message))
                        .setTitle(getString(R.string.signup_error_title))
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(getString(R.string.message_processing));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                ParseUser newUser = new ParseUser();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setEmail(email);
                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        mProgressDialog.dismiss();
                        if (e == null) {
                            RibbitApplication.updateParseInstallation(ParseUser.getCurrentUser());

                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                            builder.setMessage(e.getMessage())
                                    .setTitle(getString(R.string.signup_error_title))
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        } else {
            finish();
        }

    }

}
