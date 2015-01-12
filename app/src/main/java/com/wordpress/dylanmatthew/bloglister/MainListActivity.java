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

    protected String[] mBlogPostTitles;
    public static final int NUM_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    public ListView listView;
    protected ProgressBar mProgressBar;

    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        listView = (ListView) findViewById(R.id.listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {

                    JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                    JSONObject jsonPost = jsonPosts.getJSONObject(position);
                    String blogUrl = jsonPost.getString("url");

                    Intent intent = new Intent(MainListActivity.this, BlogWebView.class);
                    intent.setData(Uri.parse(blogUrl));

                    startActivity(intent);

                } catch (JSONException e) {
                    logException(e);
                }
            }
        });

        if(isNetworkAvail()) {

            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();

        }
        else {
            Toast.makeText(MainListActivity.this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }

        //listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles));
    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception caught!", e);
    }


    private boolean isNetworkAvail() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvail = false;

        if(networkInfo != null && networkInfo.isConnected()) {
            isAvail = true;
        }

        return isAvail;

    }





    public void handleBlogResponse() {

        mProgressBar.setVisibility(View.INVISIBLE);

        if(mBlogData == null) {

            updateDisplayForError();
        }
        else {
            try {

                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                ArrayList<HashMap<String, String>> blogPosts = new ArrayList<HashMap<String, String>>();
                for(int i =0; i < jsonPosts.length(); i++) {
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    HashMap<String, String> blogPost = new HashMap<String, String>();
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_AUTHOR, author);

                    blogPosts.add(blogPost);
                }

                String[] keys = {KEY_TITLE, KEY_AUTHOR};
                int[] ids = {android.R.id.text1, android.R.id.text2};

                SimpleAdapter adapter =
                        new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);

                listView.setAdapter(adapter);

            }catch(Exception e){
                logException(e);
            }
        }




    }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage("There was an error getting Blog Data");
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {



        @Override
        protected JSONObject doInBackground(Object[] params) {

            int responseCode = -1;
            JSONObject jsonResponse = null;

            try {
                URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUM_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();
                responseCode = connection.getResponseCode();

                if(responseCode == HttpsURLConnection.HTTP_OK) {

                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
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
            mBlogData = results;
            handleBlogResponse();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
