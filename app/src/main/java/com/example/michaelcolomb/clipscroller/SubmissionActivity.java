package com.example.michaelcolomb.clipscroller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * This class defines the submission activity for submitting new clips to the application.
 * It forces the user to submit a title and select a video from their phone's gallery. After
 * A live preview of the clip submission is didplayed. After the necessary conditions are met,
 * the user may submit the clip to the application database and storage.
 * @author colomb2
 */
public class SubmissionActivity extends AppCompatActivity {


    private static final int MEDIA_CODE = 2;
    private static final int PLAY_WIDTH = 300;
    private static final int PLAY_HEIGHT = 300;
    private static final String VIDEO_FILTER = "video/*";
    private static final String VIDEO_DIRECTORY = "video/";

    private EditText mEditTitleView;
    private View clipFragmentView;
    private ClipFragment clipFragment;
    private TextView preview;
    private Button submit;
    private TextView previewBlock;
    private Uri clipData;

    /**
     * This method is called when this activity is first created. It saves references to the
     * activity views and sets up the listener for the clip preview title to update when
     * the user changes the text within the edit title view. The preview fragment is initially
     * hidden to prevent having to show an empty preview.
     * @param savedInstanceState past state of this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        mEditTitleView = (EditText) findViewById(R.id.title);
        clipFragmentView = findViewById(R.id.clip_list_fragment);
        clipFragment = (ClipFragment) getFragmentManager()
                .findFragmentById(R.id.clip_list_fragment);
        preview = (TextView) findViewById(R.id.preview);
        previewBlock = (TextView) findViewById(R.id.preview_block);
        submit = (Button) findViewById(R.id.final_submit);
        clipFragmentView.setVisibility(View.GONE);
        preview.setVisibility(View.GONE);
        previewBlock.setVisibility(View.VISIBLE);
        submit.setEnabled(false);


        mEditTitleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newTitle = mEditTitleView.getText().toString().trim();
                clipFragment.setTitle(newTitle);
            }

            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });
    }

    /**
     * This method prompts the user for a video to store. A new window will be launched for
     * the user to choose from videos in their media gallery or other location of their choosing.
     * http://stackoverflow.com/questions/4922037/android-let-user-pick-image-or-video-from-gallery
     * @param view of the upload button
     */
    public void retrieveMedia(View view) {
        Intent pickIntent = new Intent();
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        pickIntent.setType(VIDEO_FILTER);
        Intent videoIntent = Intent.createChooser(pickIntent, getString(R.string.pickVideo));
        startActivityForResult(videoIntent, MEDIA_CODE);
    }

    /**
     * This method will respond when the upload intent has finished. If the result was
     * successful, the preview fragment fields are populated and the fragment is made
     * visible.
     * @param requestCode code that identifies the activity result
     * @param resultCode code that describes the success of the acvtivity intent
     * @param data that contains the selected video
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MEDIA_CODE) {
            clipData = data.getData();
            clipFragment.setVideo(clipData);
            clipFragmentView.setVisibility(View.VISIBLE);
            preview.setVisibility(View.VISIBLE);
            previewBlock.setVisibility(View.GONE);
            submit.setEnabled(true);
        }
    }

    /**
     * This method is called when the user chooses to submit their clip. It checks to ensure
     * that the title field is not empty. Then, it stores the video in storage and pushes
     * a newly made clip into the database, which includes the video storage address. After the
     * clip is uploaded to the database/storage, the user is displayed a toast to inform them,
     * and the activity finishes.
     * @param view
     */
    public void submit(View view) {
        final String title = mEditTitleView.getText().toString().trim();
        final Context context = this;

        if (title.isEmpty()) {
            Toast.makeText(context, getString(R.string.emptyTitleOnSubmit), Toast.LENGTH_LONG).show();
            return;
        }

        StorageReference videoStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference videoRef = videoStorageRef.child(VIDEO_DIRECTORY + title);

        videoRef.putFile(clipData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        Clip addedClip = new Clip(title, downloadUrl);

                        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference mEntriesReference =
                                mDatabase.getReference(MainActivity.CLIP_ADDRESS);

                        mEntriesReference.push().setValue(addedClip);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, getString(R.string.failedToProcessVideo),
                                Toast.LENGTH_LONG).show();
                    }
                });

        Toast.makeText(context, getString(R.string.successfulUpload), Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * This subclass defines the fragment that contains the clip_list_item. Its contains
     * references to the contained views and allows the submission activity to update
     * them.
     */
    public static class ClipFragment extends Fragment {

        private TextView clipTitle;
        private VideoView clipVideo;
        private ProgressBar progressBar;
        private ImageView playButton;
        private ProgressBar loadingView;

        /**
         * This method is called when the fragment view is creates. It infaltes the fragment
         * with the clip_list_item and stores references to the contained views.
         * @param inflater for filling the fragment
         * @param container for views within fragment
         * @param savedInstanceState past state of this fragment
         * @return the inflated list item view
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.clip_list_item, container, false);

            clipTitle = (TextView) view.findViewById(R.id.clip_list_title);
            clipVideo = (VideoView) view.findViewById(R.id.clip);
            progressBar = (ProgressBar) view.findViewById(R.id.progress);
            playButton = (ImageView) view.findViewById(R.id.play_button);
            loadingView = (ProgressBar) view.findViewById(R.id.loadingBar);

            return view;
        }

        /**
         * This method sets the list item's title field.
         * @param title for updated title field.
         */
        public void setTitle(String title) {
            clipTitle.setText(title);
        }

        /**
         * This method sets the list item's video. It calls configuration methods
         * to ensure it is set up correctly.
         * @param uri of the video file
         */
        public void setVideo(Uri uri) {
            clipVideo.setVideoURI(uri);
            clipVideo.setOnTouchListener(new ClipPlayer.ClipTouchListener(progressBar,
                    playButton, loadingView, clipVideo));

            android.view.ViewGroup.LayoutParams layoutParams = playButton.getLayoutParams();
            layoutParams.width = PLAY_WIDTH;
            layoutParams.height = PLAY_HEIGHT;
            playButton.setLayoutParams(layoutParams);
        }
    }

    /**
     * This method cancels any uploads and finishes the activity.
     * @param view of the cancel button
     */
    public void cancel(View view) {
        finish();
    }
}
