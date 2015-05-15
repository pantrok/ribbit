package com.synergy.ribbit.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.synergy.ribbit.R;

import java.util.Timer;
import java.util.TimerTask;


public class ViewImageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBarViewImage);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Uri imageUri = getIntent().getData();
        mProgressBar.setVisibility(View.VISIBLE);
        Picasso.with(this).load(imageUri.toString()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(ViewImageActivity.this,getString(R.string.image_succes_message), Toast.LENGTH_LONG).show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 10*1000);
            }
            @Override
            public void onError() {
                mProgressBar.setVisibility(View.GONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewImageActivity.this);
                builder.setMessage(getString(R.string.error_loading_image))
                        .setTitle(getString(R.string.error_loading_image_title))
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
