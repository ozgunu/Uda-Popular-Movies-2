package com.example.android.uda_popular_movies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.udacity_project_1.R;

/**
 * Created by Ozgun Ulusoy
 */

public class MainActivity extends ActionBarActivity
        implements MainFragment.OnItemSelectedListener {

    private final String MAIN_FRAGMENT_TAG = "Main_Fragment_Tag";
    private final String DETAIL_FRAGMENT_TAG = "Detail Fragment Tag";
    private final String KEY_FOR_MOVIE = "MOVIE_KEY_FOR_BUNDLE";
    private final String BACK_STACK = "BACK STACK";

    private DetailFragment detailFragment;
    private MainFragment mainFragment;

    private boolean mTwoPane;
    private boolean backExits;    // we will use this in onBackPressed() method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //SharedPreferences favorites = getSharedPreferences("uda_popular_movies_favorites", MODE_PRIVATE);
        //favorites.edit().clear().commit();


        // First we check if we are in two pane mode or single pane mode
        // We then save this info in the mTwoPane boolean variable
        if (findViewById(R.id.right_container) != null) mTwoPane = true;
        if (findViewById(R.id.right_container) == null) mTwoPane = false;


        // when we first run the app, there is no savedInstanceState
        if (savedInstanceState == null) {

            detailFragment = new DetailFragment();
            mainFragment = new MainFragment();

            // If in single pane mode, we place the Main Fragment to the left
            if (!mTwoPane) {
                getFragmentManager().beginTransaction()
                        .add(R.id.left_container, mainFragment, MAIN_FRAGMENT_TAG)
                        .commit();
            }

            // If in two pane mode, main fragment goes left, detail goes right
            if (mTwoPane) {
                getFragmentManager().beginTransaction()
                        .add(R.id.left_container, mainFragment, MAIN_FRAGMENT_TAG)
                        .add(R.id.right_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }

        // ok, this means the device is rotated and the activity is recreated. now we will find out the previous
        // state of the fragments and place them on screen in the new orientation.
        if (savedInstanceState != null) {

            // first we retrieve both fragments from their previous states
            mainFragment = (MainFragment) getFragmentManager().getFragment(savedInstanceState, "MAIN");
            detailFragment = (DetailFragment) getFragmentManager().getFragment(savedInstanceState, "DETAIL");

            // sometimes the above lines return null values. in this case we create new instances to prevent crashing.
            if (mainFragment == null) mainFragment = new MainFragment();
            if (detailFragment == null) detailFragment = new DetailFragment();



            // NOW PLACE THE FRAGMENTS ON SCREEN ACCORDING TO DIFFERENT SCENARIOS

            // (1) we go into single pane mode, and there is a detail activity started before.
            if (!mTwoPane && getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) != null
                          && detailFragment.getUserVisibleHint() == true) {

                // clear the backStack because orienatation has changed, we will start over
                getFragmentManager().popBackStackImmediate(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getFragmentManager().beginTransaction()     // before, DETAIL fragment was in right container. now we want to
                        .remove(detailFragment)             // put it in the left container. in order to avoid the ugly
                        .commit();                          // "IllegalStateException: Can't change container ID of fragment"
                                                            // warning we should first remove the fragment from its container.

                getFragmentManager().executePendingTransactions();          // we want to make sure it removes it right away!!

                getFragmentManager().beginTransaction()
                        .add(R.id.left_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();

                mainFragment.setUserVisibleHint(false);
                detailFragment.setUserVisibleHint(true);
            }


            // (2) we now go into single pane mode and THERE IS NO detail fragment created before.
            else if ((!mTwoPane && getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) == null) ||
                     (!mTwoPane && detailFragment.getUserVisibleHint() == false)) {

                // clear the backStack because orientation has changed, we will start over
                getFragmentManager().popBackStackImmediate(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                mainFragment.setUserVisibleHint(true);
                detailFragment.setUserVisibleHint(false);
            }


            // (3) while detail fragment was on screen, device rotated and now we are going to two pane mode.
            else if (mTwoPane && detailFragment.getUserVisibleHint() == true
                              && mainFragment.getUserVisibleHint() == false) {

                getFragmentManager().beginTransaction()     // before, DETAIL fragment was in the left container. now we want to
                        .remove(detailFragment)             // put it in the right container. in order to avoid the ugly
                        .commit();                          // "IllegalStateException: Can't change container ID of fragment"
                                                            // warning we should first remove the fragment from its container.

                getFragmentManager().executePendingTransactions();      // we want to make sure it removes it right away!!

                getFragmentManager().popBackStackImmediate(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getFragmentManager().beginTransaction()
                        //.add(R.id.left_container, mainFragment, MAIN_FRAGMENT_TAG)
                        .add(R.id.right_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();

                mainFragment.setUserVisibleHint(true);
                detailFragment.setUserVisibleHint(true);
            }


            // (4) we go to two pane mode and the main fragment (posters) was on screen
            else if ((mTwoPane && getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) == null) ||
                     (mTwoPane && detailFragment.getUserVisibleHint() == false)) {

                // clear the backstack because orienatation has changed, we will start over
                getFragmentManager().popBackStackImmediate(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getFragmentManager().beginTransaction()
                        .remove(detailFragment)
                        .commit();

                getFragmentManager().executePendingTransactions();

                getFragmentManager().beginTransaction()
                        .add(R.id.right_container, new DetailFragment())
                        .commit();

                mainFragment.setUserVisibleHint(true);
                detailFragment.setUserVisibleHint(false);
            }

            else {}
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    // This is the concrete method of the interface. We call this method from within
    // the fragment. The position, id AND the selected Movie object itself are passed
    // as the parameters. So now we know which item was selected by the user and we will
    // create and inflate the appropriate details screen..
    @Override
    public void onItemSelected(int position, long id, Movie movie) {

        // below we work on the detail fragment. we pass the Movie object to the fragment
        // and then put that fragment into the container.
        detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOR_MOVIE, movie);
        detailFragment.setArguments(bundle);
        backExits = false;    // a film is selected, this means the first Back button hit should not quit the app

        if (!mTwoPane) {

            getFragmentManager().beginTransaction()
                    .add(R.id.left_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .addToBackStack(BACK_STACK)
                    .commit();

            detailFragment.setUserVisibleHint(true);
            mainFragment.setUserVisibleHint(false);

        }

        if (mTwoPane) {
            getFragmentManager().beginTransaction()
                    .add(R.id.right_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .addToBackStack(BACK_STACK)
                    .commit();

            detailFragment.setUserVisibleHint(true);
            mainFragment.setUserVisibleHint(true);
        }


    }

    // By the help of this following callback method, when user hits back button
    // the screen changes to what it was before (as long as it exists in the backStack).
    @Override
    public void onBackPressed() {

        detailFragment.setUserVisibleHint(false);
        mainFragment.setUserVisibleHint(true);

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }

        else {

            if ((getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) == null) || (backExits)) {
                    finish();
            }

            else if (getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) != null) {

                    backExits = true;

                    if (!mTwoPane) {
                        getFragmentManager().beginTransaction()
                                .remove(detailFragment)
                                .commit();
                    }
            }
        }
    }


    // here we save the current states of the fragments into a bundle.
    // when the activity is recreated due to device rotation, we will
    // read this bundle and restore the fragments with their current states.

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG) != null) {
            getFragmentManager().putFragment(outState, "MAIN", getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG));
        }

        if (getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG) != null) {
            getFragmentManager().putFragment(outState, "DETAIL", getFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG));
        }
    }
}