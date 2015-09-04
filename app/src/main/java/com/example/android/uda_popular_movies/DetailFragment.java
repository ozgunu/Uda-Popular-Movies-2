package com.example.android.uda_popular_movies;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.udacity_project_1.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The fragment is responsible for showing the data on a selected single movie.
 *
 * Created by Ozgun Ulusoy on 25.08.2015.
 */

public class DetailFragment extends Fragment {

    private final String KEY_FOR_MOVIE = "MOVIE_KEY_FOR_BUNDLE";
    private final String KEY_FOR_REVIEWS = "KEY_FOR_REVIEWS";
    private final String KEY_FOR_TRAILERS = "KEY_FOR_TRAILERS";

    private ImageView imageView;
    private ImageView favButton;
    private String posterURL;
    private Movie movie;
    private Context context;
    private SharedPreferences favorites;

    private LinearLayout rowTitle;
    private LinearLayout rowOriginalTitle;
    private LinearLayout rowReleaseDate;
    private LinearLayout rowPopularity;
    private LinearLayout rowUserRating;
    private LinearLayout rowOverview;
    private LinearLayout rowAddToFavorites;
    private LinearLayout containerForReviews;
    private LinearLayout containerForTrailers;
    private LinearLayout rootViewForDetailFragment;
    private ScrollView scrollView;
    private ImageView warningSign;

    private TextView movieTitleField;
    private TextView releaseDateField;
    private TextView originalTitleField;
    private TextView popularityField;
    private TextView userRatingField;
    private TextView overviewField;
    private TextView addRemoveFavsTV;

    private LayoutInflater inflater;
    private Boolean isFavorite;

    private Bundle recoveredStateBundle;

    ArrayList<Review> reviewsForThisMovie = new ArrayList<>();
    ArrayList<Trailer> trailersForThisMovie = new ArrayList<>();

    ArrayList<LinearLayout> visibleRows = new ArrayList<>();     // All visible rows will be added to this, in order.
                                                                 // Later we can assign a light background color to
                                                                 // the first one, and a dark one to the next, and a
                                                                 // light one to the next etc..

    // Constructor
    public DetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        this.inflater = inflater;
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // if we have a savedInstanceState let's retrieve everything from it
        // we don't need to connect to server and download data again!
        if (savedInstanceState != null) {
            recoveredStateBundle = savedInstanceState;
            movie = savedInstanceState.getParcelable(KEY_FOR_MOVIE);
        }

        // if there's a bundle, let's retrieve the Movie object from it
        Bundle bundle = getArguments();
        if (bundle != null) movie = bundle.getParcelable(KEY_FOR_MOVIE);

        // Set up the context for later use
        context = view.getContext();

        // Let's instantiate all attributes (Views etc...)
        instantiateEverything(view);

        if (movie == null) {                           // no movie is selected yet but detailFragment is visible
            scrollView.setVisibility(View.GONE);       // hide the scroolView, it includes EVERYTHING !
            warningSign.setVisibility(View.VISIBLE);   // show the warning sign
        }

        if (movie != null) {                        // if the movie is not null place its info on the screen
            warningSign.setVisibility(View.GONE);   // hide the warning sign
            setUpScreen(view);                      // we know what movie to show, so let's set up everything!
        }

    } // end onViewCreated()



    // instantiate all fields and views. it's good to keep things tidy!

    public void instantiateEverything(View view) {

        visibleRows = new ArrayList<>();
        rowTitle = (LinearLayout) view.findViewById(R.id.row_title);
        rowOriginalTitle = (LinearLayout) view.findViewById(R.id.row_original_title);
        rowReleaseDate = (LinearLayout) view.findViewById(R.id.row_release_date);
        rowPopularity = (LinearLayout) view.findViewById(R.id.row_popularity);
        rowUserRating = (LinearLayout) view.findViewById(R.id.row_user_rating);
        rowOverview = (LinearLayout) view.findViewById(R.id.row_overview);
        rowAddToFavorites = (LinearLayout) view.findViewById(R.id.row_add_to_favorites);
        containerForReviews = (LinearLayout) view.findViewById(R.id.container_for_all_reviews);
        containerForTrailers = (LinearLayout) view.findViewById(R.id.container_for_all_trailers);
        rootViewForDetailFragment = (LinearLayout) view.findViewById(R.id.root_view_for_detail_fragment);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        warningSign = (ImageView) view.findViewById(R.id.image_view_for_no_movie_sign);
        movieTitleField = (TextView) view.findViewById(R.id.textView_movieTitle_field);
        originalTitleField = (TextView) view.findViewById(R.id.textView_originalTitle_field);
        releaseDateField = (TextView) view.findViewById(R.id.textView_releaseDate_field);
        popularityField = (TextView) view.findViewById(R.id.textView_popularity_field);
        userRatingField = (TextView) view.findViewById(R.id.textView_userRating_field);
        overviewField = (TextView) view.findViewById(R.id.textView_overView_field);
        addRemoveFavsTV = (TextView) view.findViewById(R.id.add_remove_favorites_text_view);
        favButton = (ImageView) view.findViewById(R.id.image_view_for_star);
        favorites = context.getSharedPreferences("uda_popular_movies_favorites", context.MODE_PRIVATE);

    } // end of instantiateEverything()




    // ------------------ Form and fill the screen --------------------

    public void setUpScreen(View view) {

        // Determine if the movie is already in favorites
        if (favorites.getBoolean(movie.getId(), false)) {
            favButton.setImageResource(R.drawable.remove);
            addRemoveFavsTV.setText("Remove from favorites");
            isFavorite = true;
        } else if (!favorites.getBoolean(movie.getId(), false)) {
            isFavorite = false;
        }

        // Set up the click handler for the FAV button
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // movie is NOT in the list
                if (!isFavorite) {
                    favorites.edit().putBoolean(movie.getId(), true).commit(); // add movie to sharedPreferences
                    isFavorite = true;
                    favButton.setImageResource(R.drawable.saved);              // change the button image and message
                    addRemoveFavsTV.setText("Saved to favorites");             // change the text

                    // after 3 seconds we will change the image and message again
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            favButton.setImageResource(R.drawable.remove);
                            addRemoveFavsTV.setText("Remove from favorites");
                        }
                    }, 3000);
                }

                // movie IS in the list
                else if (isFavorite) {
                    favorites.edit().remove(movie.getId()).commit();    // remove movie from sharedPreferences
                    isFavorite = false;
                    favButton.setImageResource(R.drawable.saved);       // change the button image and message
                    addRemoveFavsTV.setText("Removed from favorites");  // change the text

                    // after 3 seconds we will change the image and message again
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            favButton.setImageResource(R.drawable.star);
                            addRemoveFavsTV.setText("Add to favorites");
                        }
                    }, 3000);
                }
            }
        }); // end of fav button click listener


        // Building the poster url for an image with size 500 and placing the image to the imageView

        posterURL = "http://image.tmdb.org/t/p/w500/" + movie.getPosterPath();
        imageView = (ImageView) view.findViewById(R.id.detail_fragment_image_view);
        Picasso.with(context).load(posterURL).into(imageView);

        // show the movie data on screen
        showMovieDataOnScreen();


        // RETRIEVING REVIEWS:
        // If savedInstanceState exists, we will retrieve reviews from there
        if (recoveredStateBundle != null) {
            reviewsForThisMovie = recoveredStateBundle.getParcelableArrayList(KEY_FOR_REVIEWS);
            showReviewsOnScreen();
        }
        // savedInstanceState does not exist. We retrieve the reviews via network
        else {
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
            fetchReviewsTask.execute(movie.getId());
        }


        // RETRIEVING TRAILERS:
        // If savedInstanceState exists, we will retrieve trailers from there
        if (recoveredStateBundle != null) {
            trailersForThisMovie = recoveredStateBundle.getParcelableArrayList(KEY_FOR_TRAILERS);
            showTrailersOnScreen();
        }
        // savedInstanceState does not exist. We retrieve the trailers via network
        else {
            FetchTrailersTask fetchTrailersTask = new FetchTrailersTask();
            fetchTrailersTask.execute(movie.getId());
        }

    } // end of setUpScreenFromMovieObject()



    // Method that paints rows
    protected void paintRows() {
        Boolean darkBackground = true;
        for (LinearLayout l : visibleRows) {
            if (darkBackground) {
                l.setBackgroundColor(getResources().getColor(R.color.box_background_dark));
                darkBackground = false;
            } else {
                l.setBackgroundColor(getResources().getColor(R.color.box_background_light));
                darkBackground = true;
            }
        }
    } // end paintRows()



    // An inner class for the background thread. The purpose of this class is to
    // access theMovieDb.org website, fetch the reviews for the current movie

    protected class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {

        @Override
        protected ArrayList<Review> doInBackground(String... params) {

            HttpConnectionHelper httpConnectionHelper = new HttpConnectionHelper();
            httpConnectionHelper.buildUrlReview(params[0]);
            String myJsonString = httpConnectionHelper.makeConnectionAndGetData();

            if (myJsonString != null) {
                return getReviewDataFromJSon(myJsonString);
            } else return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            reviewsForThisMovie = reviews;
            showReviewsOnScreen();
        }
    } // End inner class FetchReviewTask



    // read data stored in JSON string and create Review objects
    public ArrayList<Review> getReviewDataFromJSon(String string) {

        String LOG_TAG = "getReviewDataFromJSon(String string) method in DetailFragment";
        ArrayList<Review> reviews = new ArrayList<Review>();

        final String KEY_ID = "id";
        final String KEY_AUTHOR = "author";
        final String KEY_CONTENT = "content";
        final String KEY_URL = "url";

        try {
            JSONObject reviewsJSONObject = new JSONObject(string);
            JSONArray resultsArray = reviewsJSONObject.getJSONArray("results");

            // iterate through the resultsArray and create new Review object for each iteration
            for (int i = 0; i < resultsArray.length(); i++) {

                Review review = new Review();

                // set the attributes of this new Review object with the data from JSON
                review.setId(resultsArray.getJSONObject(i).getString(KEY_ID));
                review.setAuthor(resultsArray.getJSONObject(i).getString(KEY_AUTHOR));
                review.setContent(resultsArray.getJSONObject(i).getString(KEY_CONTENT));
                review.setUrl(resultsArray.getJSONObject(i).getString(KEY_URL));

                // We add the new movie object into our 'movies' list
                reviews.add(review);
            } // end for
        } // end try

        catch (JSONException ex) { Log.e(LOG_TAG, "JSON Exception --> ", ex); }
        return reviews;

    } // End getDataFromJson()



    // An inner class for the background thread. The purpose of this class is to
    // access theMovieDb.org website, fetch the trailer info for the current movie

    protected class FetchTrailersTask extends AsyncTask<String, Void, ArrayList<Trailer>> {

        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {

            HttpConnectionHelper httpConnectionHelper = new HttpConnectionHelper();
            httpConnectionHelper.buildUrlTrailer(params[0]);
            String myJsonString = httpConnectionHelper.makeConnectionAndGetData();

            if (myJsonString != null) {
                return getTrailerDataFromJSon(myJsonString);
            } else return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Trailer> trailers) {
            trailersForThisMovie = trailers;
            showTrailersOnScreen();
        }
    } // end FetchTrailersTask Class


    // read data stored in JSON string and create Trailer objects
    public ArrayList<Trailer> getTrailerDataFromJSon(String string) {

        String LOG_TAG = "getTrailerDataFromJSon(String string) method in DetailFragment";
        ArrayList<Trailer> trailers = new ArrayList<Trailer>();

        final String KEY_VIDEO_ID = "id";
        final String KEY_URL_KEY = "key";
        final String KEY_NAME = "name";
        final String KEY_SITE = "site";
        final String KEY_SIZE = "size";

        try {
            JSONObject reviewsJSONObject = new JSONObject(string);
            JSONArray resultsArray = reviewsJSONObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {

                // For each iteration we create a new Movie object
                Trailer trailer = new Trailer();

                // Set the attributes of this new Review object with the data from JSON
                trailer.setVideoId(resultsArray.getJSONObject(i).getString(KEY_VIDEO_ID));
                trailer.setUrlKey(resultsArray.getJSONObject(i).getString(KEY_URL_KEY));
                trailer.setName(resultsArray.getJSONObject(i).getString(KEY_NAME));
                trailer.setSize(resultsArray.getJSONObject(i).getString(KEY_SIZE));
                trailer.setSite(resultsArray.getJSONObject(i).getString(KEY_SITE));

                // Add the new movie object into our 'movies' list
                trailers.add(trailer);
            }
        }

        catch (JSONException ex) { Log.e(LOG_TAG, "JSON Exception --> ", ex); }
        return trailers;
    } // End getDataFromJson()




    // ----------- Put the movie data on screen (Except reviews and trailers) ------------

    public void showMovieDataOnScreen() {

        if (movie.getTitle().equals("null")) {
            movieTitleField.setText(getString(R.string.unknown));
        } else movieTitleField.setText(movie.getTitle());

        // If the original title is same with title, we hide the whole row
        if (movie.getOriginalTitle().equals(movie.getOriginalTitle()) ||
                movie.getOriginalTitle().equals("null")) {
            rowOriginalTitle.setVisibility(View.GONE);
        } else originalTitleField.setText(movie.getOriginalTitle());

        if (movie.getReleaseDate().equals("null") ||
                movie.getReleaseDate().equals(null)) {
            releaseDateField.setText(getString(R.string.unknown));
        } else releaseDateField.setText(movie.getReleaseDate());

        if (Double.toString(movie.getPopularity()).equals(null)) {
            popularityField.setText(R.string.unknown);
        } else popularityField.setText(Double.toString(movie.getPopularity()));

        if (Double.toString(movie.getVoteAverage()).equals(null)) {
            userRatingField.setText(R.string.unknown);
        } else userRatingField.setText(Double.toString(movie.getVoteAverage()));

        if (movie.getOverview().equals("null") || movie.getOverview().equals(null)) {
            overviewField.setText(R.string.unknown);
        } else overviewField.setText(movie.getOverview());

        // If a row is VISIBLE add it to the arrayList so later we can
        // assign background colors to each consecutive row by iterating through the ArrayList.

        visibleRows.clear();   // Clear the ArrayList, in case
        if (rowTitle.getVisibility() != View.GONE) {visibleRows.add(rowTitle);}
        if (rowOriginalTitle.getVisibility() != View.GONE) {visibleRows.add(rowOriginalTitle);}
        if (rowReleaseDate.getVisibility() != View.GONE) {visibleRows.add(rowReleaseDate);}
        if (rowPopularity.getVisibility() != View.GONE) {visibleRows.add(rowPopularity);}
        if (rowUserRating.getVisibility() != View.GONE) {visibleRows.add(rowUserRating);}
        if (rowOverview.getVisibility() != View.GONE) {visibleRows.add(rowOverview);}

        paintRows();   // paint all rows in the ArrayList

    }


    // -------------------------- Put the reviews on screen -------------------------------

    public void showReviewsOnScreen () {

        if (reviewsForThisMovie.size() > 0) {

            for (int i = 0; i < reviewsForThisMovie.size(); i++) {

                LinearLayout singleReview = (LinearLayout) inflater.inflate(R.layout.layout_for_single_review, null);
                TextView reviewAuthorField = (TextView) singleReview.findViewById(R.id.textView_review_author_field);
                TextView reviewContentField = (TextView) singleReview.findViewById(R.id.textView_review_content_field);
                reviewAuthorField.setText(" " + reviewsForThisMovie.get(i).getAuthor() + ":");
                reviewContentField.setText(reviewsForThisMovie.get(i).getContent());

                // add the review row into its container
                containerForReviews.addView(singleReview);
                // append this row to the list for `rows to be painted`
                visibleRows.add(singleReview);
            }

        } else if (reviewsForThisMovie.size() == 0) {
            containerForReviews.setVisibility(View.GONE);
        }

        paintRows();

    } // end of showReviewsOnScreen()



    // -------------------------- Put the trailers on screen ------------------------------

    public void showTrailersOnScreen () {

        if (trailersForThisMovie.size() > 0) {

            for (int i = 0; i < trailersForThisMovie.size(); i++) {

                LinearLayout singleTrailer = (LinearLayout) inflater.inflate(R.layout.layout_for_single_trailer, null);
                TextView trailerNameField = (TextView) singleTrailer.findViewById(R.id.textView_trailer);
                trailerNameField.setText("Watch Video Trailer #" + Integer.toString(i + 1));
                ImageView trailerImage = (ImageView) singleTrailer.findViewById(R.id.image_view_for_trailer_symbol);

                final int final_i = i;   // we do this in order to be able to reach the value of i from the listener below

                // set click handler for viewing the trailer
                trailerImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (trailersForThisMovie.get(final_i).getSite().equalsIgnoreCase("YouTube")) {

                            // build the url using the value from trailer object
                            Uri url = Uri.parse("http://www.youtube.com/watch").buildUpon()
                                    .appendQueryParameter("v", trailersForThisMovie.get(final_i).getUrlKey())
                                    .build();

                            // launch the implicit intent
                            Intent intent = new Intent(Intent.ACTION_VIEW, url);
                            startActivity(intent);
                        }
                    }
                });

                // add this review row into its container
                containerForTrailers.addView(singleTrailer);

                // append this row to the list for `rows to be painted`
                visibleRows.add(singleTrailer);

                // now that our arrayList visibleRows is ready we can paint the rows
                paintRows();
            }
        } // end if

        else if (trailersForThisMovie.size() == 0) {
            containerForTrailers.setVisibility(View.GONE);
        }

    } // end of showTrailersOnScreen()


    // When device is rotated etc we want to save the arrayList moviesInGrid's current contents
    // so we will be able to restore it without connecting to the network and fetching data from there

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_FOR_REVIEWS, reviewsForThisMovie);
        outState.putParcelableArrayList(KEY_FOR_TRAILERS, trailersForThisMovie);
        outState.putParcelable(KEY_FOR_MOVIE, movie);
        super.onSaveInstanceState(outState);
    }


}