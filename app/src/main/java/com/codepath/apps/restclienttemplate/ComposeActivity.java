package com.codepath.apps.restclienttemplate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    TwitterClient client;
    public static final int MAX_LEN = 280;
    public static final String TAG = "ComposeActivity";
    EditText etCompose;
    TextView tvCount;
    Button btnTweet;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = pref.getString("tweetDraft", "");

        client = TwitterApp.getRestClient(this);
        etCompose = findViewById(R.id.etCompose);
        tvCount = findViewById(R.id.tvCount);
        if (s != "") {
            etCompose.setText(s);
            int num = 280 - s.length();
            tvCount.setText(String.valueOf(num) + getResources().getString(R.string.out_of));
        }
        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fires right as the text is being changed (even supplies the range of text)
                int num = 280 - s.length();
                if (num < 0) {
                    tvCount.setText(getResources().getString(R.string.too_long));
                    tvCount.setTextColor(Color.RED);
                } else {
                    tvCount.setText(String.valueOf(num) + getResources().getString(R.string.out_of));
                    tvCount.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        btnTweet = findViewById(R.id.btnTweet);

        //Set click listener
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, the tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tweetContent.length() > MAX_LEN) {
                    Toast.makeText(ComposeActivity.this, "Tweet must be 280 char or less", Toast.LENGTH_SHORT).show();
                    return;
                }
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess published tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "tweet data is: " + tweet);
                            Intent i = new Intent();
                            i.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, i);
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("tweetDraft", "");
                            edit.commit();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
                return;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        final String s = etCompose.getText().toString();
        if (s.length() != 0) {

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("Text Exists");
            ad.setMessage("Do you wish to save this draft? ");

            ad.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("tweetDraft", s);
                            edit.commit();
                            finish();
                        }
                    });

            ad.setNeutralButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("tweetDraft", "");
                            edit.commit();
                            finish();
                        }
                    });

            ad.show();
        } else {
            finish();
        }
        return false;
    }

}