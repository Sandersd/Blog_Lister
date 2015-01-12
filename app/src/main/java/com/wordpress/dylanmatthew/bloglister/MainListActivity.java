package com.wordpress.dylanmatthew.bloglister;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


public class MainListActivity extends ActionBarActivity {

    //Member Variables
    public static final int NUM_POSTS = 20;

    //TAG
    public static final String TAG = MainListActivity.class.getSimpleName();

    //JSONObject member variable to hold the Data
    protected JSONObject mBlogData;

    //View
    public ListView listView;
    protected ProgressBar mProgressBar;

    //Keys for HashMap
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        //Sets up View attributes
        listView = (ListView) findViewById(R.id.listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //ItemClickListener for ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {

                    //Grab Array of Posts
                    JSONArray jsonPosts = mBlogData.getJSONArray("posts");

                    //Get individual post at selected position
                    JSONObject jsonPost = jsonPosts.getJSONObject(position);

                    //Grab Url from post
                    String blogUrl = jsonPost.getString("url");

                    //Create new Intent that passes Url to a WebView
                    Intent intent = new Intent(MainListActivity.this, BlogWebView.class);
                    intent.setData(Uri.parse(blogUrl));

                    startActivity(intent);

                } catch (JSONException e) {
                    logException(e);
                }
            }
        });

        if(isNetworkAvail()) {

            //Start ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);

            //Start AsyncTask
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();

        }
        else {
            Toast.makeText(MainListActivity.this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }


    }

    //Keep Logs consistent
    private void logException(Exception e) {
        Log.e(TAG, "Exception caught!", e);
    }


    //Check Availability
    private boolean isNetworkAvail() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvail = false;

        if(networkInfo != null && networkInfo.isConnected()) {
            isAvail = true;
        }

        return isAvail;

    }


    //Update UI with info from AsyncTask
    public void handleBlogResponse() {

        //Stop ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);

        if(mBlogData == null) {

            updateDisplayForError();
        }
        else {
            try {

                //Get Array
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");

                ArrayList<HashMap<String, String>> blogPosts = new ArrayList<HashMap<String, String>>();

                //Cycle through array to get each individual post
                for(int i =0; i < jsonPosts.length(); i++) {

                    //Post
                    JSONObject post = jsonPosts.getJSONObject(i);

                    //Grab individual titles and authors and format them
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    //Create a HashMap of the titles and authors
                    HashMap<String, String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_AUTHOR, author);

                    //Add HashMap to ArrayList
                    blogPosts.add(blogPost);
                }

                //Arrays for SimpleAdapter
                String[] keys = {KEY_TITLE, KEY_AUTHOR};
                int[] ids = {android.R.id.text1, android.R.id.text2};

                //SimpleAdapter to show titles and authors
                SimpleAdapter adapter =
                        new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);

                listView.setAdapter(adapter);

            }catch(Exception e){
                logException(e);
            }
        }




    }

    //Display Error popup is Data is null after AsyncTask
    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage("There was an error getting Blog Data");
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //AsyncTask
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {



        @Override
        protected JSONObject doInBackground(Object[] params) {

            int responseCode = -1;
            JSONObject jsonResponse = null;

            try {

                //Blog API Url
                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUM_POSTS);

                //Connecting
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();

                if(responseCode == HttpsURLConnection.HTTP_OK) {

                    //Creates InputStream and Reader
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();

                    //Grabs data
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);

                    Log.v(TAG, "Response Data: " + responseData);

                    jsonResponse = new JSONObject(responseData);

                }
                else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }

                Log.i(TAG, "Code: " + responseCode);
            }catch(MalformedURLException e){
                logException(e);
            }
            catch(IOException e) {
                logException(e);
            }
            catch(Exception e){
                logException(e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject results) {
            //Sets member variable to data
            mBlogData = results;
            handleBlogResponse();
        }

    }


}
