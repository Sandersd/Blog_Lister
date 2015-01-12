package com.wordpress.dylanmatthew.bloglister;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class BlogWebView extends ActionBarActivity {

    //Member Varaible to hold Url
    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_web_view);

        //Get Intent with Url from list
        Intent intent = getIntent();
        Uri blogUri = intent.getData();

        //Set up WebView
        WebView webView = (WebView) findViewById(R.id.webView);

        //Load Url in WebView
        mUrl = blogUri.toString();
        webView.loadUrl(mUrl);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blog_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Check to see if Share button is clicked

        if (id == R.id.action_share) {

            sharePost();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    //Method to share Url with a Chooser
    private void sharePost() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        //Set type
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);

        //Creates Chooser
        startActivity(Intent.createChooser(shareIntent, "How do you want to share?"));
    }
}
