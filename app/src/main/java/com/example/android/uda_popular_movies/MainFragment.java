package com.example.android.uda_popular_movies;

import android.app.Activity;
import android.app.Fragment;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.android.udacity_project_1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment responsible for showing the list of movies to the user
 * so they can make a choice.
 *
 * Created by Ozgun Ulusoy on 20.08.2015. *
 */

public class MainFragment extends Fragment {

    // Constructor
    public MainFragment(){}

    private MovieAdapter myArrayAdapter;
    private Button buttonSortByPopularity;
    private Button buttonSortByRating;
    private Button buttonSortFavorites;
    private GridView gridView;
    private String sortMethod;
    private Context context;
    private Map<String, ?> favoriteMovieIDsMap;
    private ArrayList<Movie> moviesInGrid;
    private final String KEY_FOR_ARRAYLIST = "This is the key for ArrayList";

    public void setmCallback(OnItemSelectedListener mCallback) {
        this.mCallback = mCallback;
    }

    // Instance of our interface will be used to pass data to activity
    private OnItemSelectedListener mCallback;


    // Container Activity must implement this interface
    // This is used to share data (the position number of the
    // selected item with the Main Activity and then
    // with the other fragments..
    public interface OnItemSelectedListener {
        public void onItemSelected(int position, long id, Movie movie);
    }


    // Here we initialize mCallback (instance of our interface)
    // and make sure that the container activity has implemented the callback interface.
    // If not, it throws an exception.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }




    // When device is rotated, we want to save the arrayList moviesInGrid's current contents
    // so we will be able to restore it without connecting to the network and fetching data from there
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_FOR_ARRAYLIST, moviesInGrid);
        super.onSaveInstanceState(outState);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        context = getActivity();
        moviesInGrid = new ArrayList<Movie>();

        myArrayAdapter = new MovieAdapter(
                getActivity(),                      // context
                R.layout.grid_image_item,           // layout for single item
                new ArrayList<Movie>()
        );

        // Assign the adapter to the gridView
        gridView = (GridView) rootView.findViewById(R.id.grid_view);
        gridView.setAdapter(myArrayAdapter);

        // Set the clickListener on the gridView.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Below we call the method of our onItemSelected interface. We pass the position information
                // and the Movie object corresponding to the user's selection are passed as the method's parameters.
                // This way we pass the data to the main activity because our main activity is implementing
                // our onListItemSelected interface and actually the instance of the interface mCallback is
                // directly attached to the activity!
                Movie movie = myArrayAdapter.getItem(position);
                mCallback.onItemSelected(position, id, movie);
            }
        });


        // if we have a savedInstanceState we can recover the fragment from that.
        // we don't need to connect to server and download movie posters for grid again!
        if (savedInstanceState != null) {
            moviesInGrid = savedInstanceState.getParcelableArrayList(KEY_FOR_ARRAYLIST);   // restore the ArrayList
            for (Movie movie : moviesInGrid) { myArrayAdapter.add(movie);}                 // add each movie to the adapter
        }


        // we don't have savedInstanceState, so we fetch data from network
        if (savedInstanceState == null) {
            // run background thread to fetch movie posters
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            if (sortMethod == null) { fetchMoviesTask.execute("popularity.desc"); }
            else { fetchMoviesTask.execute(sortMethod); }
        }

        return rootView;
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // this is an important method. helps the fragment remember its attribute values
        // when it is reattached to another container etc.. this way every time we rotate
        // the device it can remember what the sortMethod attribute is set to... So user
        // continues to see the same movie posters.
        setRetainInstance(true);

        // Instantiate the buttons
        buttonSortByPopularity = (Button) view.findViewById(R.id.button_sort_popularity);
        buttonSortByRating = (Button) view.findViewById(R.id.button_sort_rating);
        buttonSortFavorites = (Button) view.findViewById(R.id.button_sort_favorites);

        // Click handler for "Sort By Popularity" button
        buttonSortByPopularity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
                fetchMoviesTask.execute("popularity.desc");
                sortMethod = "popularity.desc";
            }
        });

        // Click handler for "Sort By Rating" button
        buttonSortByRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
                fetchMoviesTask.execute("vote_average.desc");
                sortMethod = "vote_average.desc";
            }
        });

        // Click handler for "Sort Favorites" button
        buttonSortFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // find the sharedPreferences file for favorites
                SharedPreferences favorites = context.getSharedPreferences("uda_popular_movies_favorites", context.MODE_PRIVATE);

                if (favorites != null) {
                    // Get everything in sharedPrefs file and put into the Map favoriteMovieIDsMap
                    favoriteMovieIDsMap = favorites.getAll();
                    FetchFavoritesTask fetchFavoritesTask= new FetchFavoritesTask();
                    fetchFavoritesTask.execute(favoriteMovieIDsMap);
                }
            }
        });

    }



    // An inner class for the background thread. The purpose of this class is to
    // access theMovieDb.org website, fetch the top movies sorted by either popularity
    // or user vote.

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            HttpConnectionHelper httpConnectionHelper = new HttpConnectionHelper();
            httpConnectionHelper.buildUrlPopularOrRating(params[0]);
            String myJsonString = httpConnectionHelper.makeConnectionAndGetData();

            if (myJsonString != null) {
                return getMovieListFromJSon(myJsonString);
            } else return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            myArrayAdapter.clear();

            if (movies != null) {
                moviesInGrid = movies;          // we store this list in a class level list, for recovering from savedInstanceState
                for (Movie movie : movies) {
                    myArrayAdapter.add(movie);  // add each movie in the list to the adapter, so they will appear on screen
                }
            }
        }
    } // end of inner class FetchMoviesTask



    // An inner class for the background thread. The purpose of this class is to
    // access theMovieDb.org website, fetch the movies in the favorites list, one by one

    public class FetchFavoritesTask extends AsyncTask<Map, Void, ArrayList<Movie>> {

        private String LOG_TAG = "Inner Class -> FetchFavoritesTask";
        private ArrayList<Movie> favoriteMoviesArrayList = new ArrayList<>();

        @Override
        protected ArrayList<Movie> doInBackground(Map... params) {

            HttpConnectionHelper httpConnectionHelper = new HttpConnectionHelper();

            for (Map.Entry<String, ?> item : favoriteMovieIDsMap.entrySet()) {

                httpConnectionHelper.buildUrlFavorites(item.getKey());
                String myJsonString = httpConnectionHelper.makeConnectionAndGetData();

                if (myJsonString != null) {
                    Movie movie = getSingleMovieFromJson(myJsonString);
                    favoriteMoviesArrayList.add(movie);
                }
            }
            return favoriteMoviesArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            myArrayAdapter.clear();
            if (movies != null) {
                for (Movie movie : movies) {
                    myArrayAdapter.add(movie);
                }
            }
        }
    } // end inner class FetchFavoritesTask


    // Reads String with JSON data, creates Movie instances and returns an ArrayList of Movies
    public ArrayList<Movie> getMovieListFromJSon(String string) {

        final String LOG_TAG = "getMovieListFromJSON(String string) in MainFragment";
        ArrayList<Movie> movies = new ArrayList<Movie>();

        final String KEY_ID = "id";
        final String KEY_TITLE = "title";
        final String KEY_ORIGINAL_TITLE = "original_title";
        final String KEY_OVERVIEW = "overview";
        final String KEY_RELEASE_DATE = "release_date";
        final String KEY_POSTER_PATH = "poster_path";
        final String KEY_POPULARITY = "popularity";
        final String KEY_VIDEO = "video";
        final String KEY_VOTE_AVERAGE = "vote_average";
        final String KEY_VOTE_COUNT = "vote_count";

        try {
            JSONObject moviesJSONObject = new JSONObject(string);
            JSONArray resultsArray = moviesJSONObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {

                // For each iteration we create a new Movie object
                Movie movie = new Movie();

                // We set the attributes of this new Movie object with the data from JSON
                movie.setId(resultsArray.getJSONObject(i).getString(KEY_ID));
                movie.setTitle(resultsArray.getJSONObject(i).getString(KEY_TITLE));
                movie.setOriginalTitle(resultsArray.getJSONObject(i).getString(KEY_ORIGINAL_TITLE));
                movie.setOverview(resultsArray.getJSONObject(i).getString(KEY_OVERVIEW));
                movie.setPopularity(resultsArray.getJSONObject(i).getDouble(KEY_POPULARITY));
                movie.setPosterPath(resultsArray.getJSONObject(i).getString(KEY_POSTER_PATH));
                movie.setReleaseDate(resultsArray.getJSONObject(i).getString(KEY_RELEASE_DATE));
                movie.setVideoStatus(resultsArray.getJSONObject(i).getString(KEY_VIDEO));
                movie.setVoteAverage(resultsArray.getJSONObject(i).getDouble(KEY_VOTE_AVERAGE));
                movie.setVoteCount(resultsArray.getJSONObject(i).getInt(KEY_VOTE_COUNT));

                // We add the new movie object into our 'movies' list
                movies.add(movie);
            } // end for
        } // end try

        catch (JSONException ex) {
            Log.e(LOG_TAG, "JSON Exception --> ", ex);
        }

        // Return the populated ArrayList
        return movies;

    } // end of method fetchMovieListFromJSon()




    // Reads String with JSON data, creates a Movie instance and returns it
    public Movie getSingleMovieFromJson(String string) {

        Movie movie = new Movie();

        final String LOG_TAG = "getSingleMovieFromJSON(String string) in MainFragment";
        final String KEY_ID = "id";
        final String KEY_TITLE = "title";
        final String KEY_ORIGINAL_TITLE = "original_title";
        final String KEY_OVERVIEW = "overview";
        final String KEY_RELEASE_DATE = "release_date";
        final String KEY_POSTER_PATH = "poster_path";
        final String KEY_POPULARITY = "popularity";
        final String KEY_VIDEO = "video";
        final String KEY_VOTE_AVERAGE = "vote_average";
        final String KEY_VOTE_COUNT = "vote_count";

        try {
            JSONObject movieJSONObject = new JSONObject(string);

            // We set the attributes of this new Movie object with the data from JSON
            movie.setId(movieJSONObject.getString(KEY_ID));
            movie.setTitle(movieJSONObject.getString(KEY_TITLE));
            movie.setOriginalTitle(movieJSONObject.getString(KEY_ORIGINAL_TITLE));
            movie.setOverview(movieJSONObject.getString(KEY_OVERVIEW));
            movie.setPopularity(movieJSONObject.getDouble(KEY_POPULARITY));
            movie.setPosterPath(movieJSONObject.getString(KEY_POSTER_PATH));
            movie.setReleaseDate(movieJSONObject.getString(KEY_RELEASE_DATE));
            movie.setVideoStatus(movieJSONObject.getString(KEY_VIDEO));
            movie.setVoteAverage(movieJSONObject.getDouble(KEY_VOTE_AVERAGE));
            movie.setVoteCount(movieJSONObject.getInt(KEY_VOTE_COUNT));

        } catch (JSONException ex) {
            Log.e(LOG_TAG, "JSON Exception --> ", ex);
        }

        // Return the Movie object
        return movie;

    } // end of method getSingleMovieFromJson()
}
