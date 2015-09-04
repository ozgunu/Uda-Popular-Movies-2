package com.example.android.uda_popular_movies;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * This class is used to build the url, make the connection using HttpUrlConnection,
 * retrieve the data and finally return it in a variable type of String.
 *
 * THE API KEY INFORMATION SHOULD BE ASSIGNED TO THE CONSTANT API_KEY BELOW.
 *
 * Created by Ozgun Ulusoy on 02.09.2015.
 */

public class HttpConnectionHelper {

    final String BASE_URL = "http://api.themoviedb.org/3";
    final String API_KEY_PARAM = "api_key";
    final String DISCOVER_PARAM = "discover";
    final String MOVIE_PARAM = "movie";
    final String SORT_PARAM = "sort_by";

    final String API_KEY = "***************** YOUR API KEY FOR themoviedb.org GOES HERE *****************";
    final String LOG_TAG = "HttpConnectionHelper";
    private URL url = null;

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    private HttpURLConnection urlConnection = null;
    private BufferedReader reader = null;

    public HttpConnectionHelper() {
        Log.d(null, "===== CONNECTION ===== CONNECTION ===== CONNECTION =====");
    }


    // building the url for fetching data on a single movie
    public void buildUrlFavorites(String id){

        // Building the final URL from the above elements using Uri Class
        // A complete url will look like:
        // http://api.themoviedb.org/3/movie/211672?api_key=YOUR_API_KEY
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(MOVIE_PARAM)
                .appendPath(id)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "MalformedURLException ----> " + ex);
        }
    } // end method buildUrlFavorites()


    // building the url for fetching movies by popularity or rating
    public void buildUrlPopularOrRating(String sortMethod){

        // Building the final URL from the above elements using Uri Class
        // A complete url will look like:
        // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=YOUR_API_KEY
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(DISCOVER_PARAM)
                .appendPath(MOVIE_PARAM)
                .appendQueryParameter(SORT_PARAM, sortMethod)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "MalformedURLException ----> " + ex);
        }
    } // end method buildUrlPopularOrRating()



    // building the url for fetching reviews on a single movie
    public void buildUrlReview(String id){

        // Building the final URL from the above elements using Uri Class
        // Full url will look like this:
        // http://api.themoviedb.org/3/movie/102899/reviews?api_key=YOUR_API_KEY
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(MOVIE_PARAM)
                .appendPath(id)
                .appendPath("reviews")
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "MalformedURLException ----> " + ex);
        }
    } // end method buildUrlReview()




    // building the url for fetching video trailer data on a single movie
    public void buildUrlTrailer(String id){

        // Building the final URL from the above elements using Uri Class
        // Full url will look like this:
        // http://api.themoviedb.org/3/movie/102899/reviews?api_key=YOUR_API_KEY
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(MOVIE_PARAM)
                .appendPath(id)
                .appendPath("videos")
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "MalformedURLException ----> " + ex);
        }
    } // end method buildUrlReview()




    // creating connection, fetching data and returning a String
    public String makeConnectionAndGetData() {

        // Will contain the raw JSON response as a string.
        String myJsonString = null;

        try {

            // Creating the request to The Movie Database, and opening the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  There is no point in parsing.
                return null;
            }

            // Saving the information to our moviesJsonString variable
            myJsonString = buffer.toString();
        }

        catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception --> ", e);
        }

        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return myJsonString;

    } // end makeConnectionAndGetData()

}
