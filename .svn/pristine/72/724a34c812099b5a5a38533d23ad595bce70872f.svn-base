package com.example.michaelcolomb.clipscroller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This class runs the main activity for the application. It coordinates the recycler view
 * with the Firebase Database and Storage to retrieve submitted videos, while allowing users
 * to watch and interact with the clips, which can be viewed in order of trend or recent
 * submission. Users may also continue onto the submission activity.
 * @author colomb2
 */
public class MainActivity extends AppCompatActivity {

    private EditText mTitleField;
    private RecyclerView mRecyclerView;
    private DatabaseReference mEntriesReference;
    private ClipAdapter clipAdapter;
    private Menu mMenu;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final long TWO_SECONDS = 2000;
    private static final String TRENDING = "trending";
    private static final String NOT_TRENDING = "not_trending";
    protected static final String CLIP_ADDRESS = "colomb2/clips";

    /**
     * This method is called when the main activity is created. It asks for permission
     * if required. It sets up the recycler view with a new adapter to retrieve and display
     * videos from each stored clip.
     * @param savedInstanceState past state of live main activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mEntriesReference = mDatabase.getReference(CLIP_ADDRESS);

        mTitleField = (EditText) findViewById(R.id.title);

        managePermissions();

        clipAdapter = ClipAdapter.create(this, mEntriesReference);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_clips);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(clipAdapter);
    }

    /**
     * This method inflates the action bar menu. It allows users to decide how to
     * rank videos and when to refresh.
     * @param menu to inflate
     * @return true upon successful inflation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);mMenu = menu;
        return true;
    }

    /**
     * This helper function checks if the READ_EXTERNAL_STORAGE permission is approved
     * yet. If not, it requests permission.
     * https://developer.android.com/training/permissions/requesting.html
     */
    private void managePermissions() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * This method is activated after the user has inputted their permissions. If permission
     * was not granted, an informational toast is displayed and the app closes.
     * @param requestCode  int identifier for storage permission
     * @param permissions  list of permissions with IDs
     * @param grantResults list of whether permissions have been granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length <= 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.deniedPermissions), Toast.LENGTH_LONG);
                try {
                    Thread.sleep(TWO_SECONDS);
                } catch (InterruptedException e) {
                    System.exit(1);
                }
                System.exit(0);
            }
        }
    }

    /**
     * This method is called when the user clicks the submission button. It launches the
     * submission activity intent.
     * @param view of the submission button
     */
    public void launchSubmit(View view) {
        Intent intent = new Intent(this, SubmissionActivity.class);
        startActivity(intent);
    }

    /**
     * This method responds to a click on a menu item. If the ranking button is selected, its
     * state is toggled, changing appropriate UI elements and updating the ClipRank class.
     * After either menu item is selected, the recycler view will update.
     * @param item MenuItem that was selected
     * @return true upon successful completion
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean trendingSelected = item.getItemId() == R.id.trending;
        boolean refreshSelected = item.getItemId() == R.id.refresh;

        if (trendingSelected) {
            MenuItem trendingItem = mMenu.findItem(R.id.trending);

            if (trendingItem.getTitle() == TRENDING) {
                Drawable selectedTrending = getDrawable(R.drawable.ic_trending_unselected_24px);

                ClipRank.setRankByTrending(false);
                trendingItem.setTitle(NOT_TRENDING);
                trendingItem.setIcon(selectedTrending);

                Toast.makeText(this, getString(R.string.notTrendingMessage).toString(),
                        Toast.LENGTH_SHORT).show();
            } else {
                Drawable unselectedTrending = getDrawable(R.drawable.ic_trending_selected_24px);

                ClipRank.setRankByTrending(true);
                trendingItem.setTitle(TRENDING);
                trendingItem.setIcon(unselectedTrending);

                Toast.makeText(this, getString(R.string.trendingMessage).toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (trendingSelected || refreshSelected) {
            clipAdapter = ClipAdapter.create(this, mEntriesReference);
            mRecyclerView.setAdapter(clipAdapter);
        }

        return true;
    }
}