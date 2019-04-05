package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.net.URL;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private ProgressBar mLoadingIndicator;
    private MenuItem mPopular;
    private MenuItem mToprated;
    private String mSortPath = "popular";
    private String jsonMovieResponse;
    private ArrayList<MovieDB> simpleJsonMovieData;
    private boolean OnlineStatus;
    private TextView Errormsg;
    private Button btnRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        Errormsg = (TextView) findViewById(R.id.tvError);
        SetButtonListener();
        FetchGridLayout fgl = new FetchGridLayout();
        fgl.execute();
    }


    public class FetchGridLayout extends AsyncTask<Void, Void, ArrayList<MovieDB>> implements MovieGridAdapter.ListItemClickListener {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<MovieDB> doInBackground(Void... voids) {

            //Check Connectivity before proceeding....if no connection show message letting user know.
            OnlineStatus = isOnline();

            if (OnlineStatus == true) {

                try {

                    URL MovieGridURL = NetworkUtils.buildUrl(mSortPath);
                    jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(MovieGridURL);

                    simpleJsonMovieData = parseMovieJson
                            .parseMovieJson(jsonMovieResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {

                return null;
            }

            return simpleJsonMovieData;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieDB> movieData) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (movieData == null || movieData.size() == 0) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        recyclerView.removeAllViewsInLayout();
                        Errormsg.setVisibility(View.VISIBLE);
                        btnRefresh.setVisibility(View.VISIBLE);
                        invalidateOptionsMenu();
                    }
                });

            } else {
                invalidateOptionsMenu();
                recyclerView = (RecyclerView) findViewById(R.id.rv_MovieGrid);
                recyclerView.setHasFixedSize(true);
                mAdapter = new MovieGridAdapter(movieData, getApplicationContext(), this);
                recyclerView.setAdapter(mAdapter);
                }
        }

        //Start intent once a movie poster is clicked on main screen
        @Override
        public void onListItemClick(int clickedItemIndex) {

            MovieDB intentdataholder = new MovieDB();
            intentdataholder = simpleJsonMovieData.get(clickedItemIndex);
            Context context = MainActivity.this;
            Class destinationActivity = DetailViewActivity.class;
            Intent intent = new Intent(context, destinationActivity);
            intent.putExtra("MovieDetails", intentdataholder);
            startActivity(intent);
        }
    }


    //Create menu and inflate in order to view
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mToprated = (MenuItem) menu.findItem(R.id.action_toprated);
        mPopular = (MenuItem) menu.findItem(R.id.action_popular);
        return true;
    }

    //Cases for when menu item is clicked.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case R.id.action_popular:
                mSortPath = "popular";
                FetchGridLayout PopularLayout = new FetchGridLayout();
                PopularLayout.execute();
                return true;

            case R.id.action_toprated:
                mSortPath = "top_rated";
                FetchGridLayout TopRatedLayout = new FetchGridLayout();
                TopRatedLayout.execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Checks whether there is a network connection

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

   public void SetButtonListener() {

        //Button appears on error when there is no network connection in order to refresh once connectivity is back.

        btnRefresh =(Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view){

                Errormsg.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.GONE);
                FetchGridLayout RetryFetch = new FetchGridLayout();
                RetryFetch.execute();
        }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //Hides menu options if connectivity issue

        if (OnlineStatus == false) {
            return false;
        }
        if (mSortPath == "top_rated"){
            mToprated.setChecked(true);
            mPopular.setChecked(false);
        }
            return true;
    }


}






