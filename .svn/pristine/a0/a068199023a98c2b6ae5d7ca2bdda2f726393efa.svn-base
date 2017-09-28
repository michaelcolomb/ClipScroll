package com.example.michaelcolomb.clipscroller;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * This class is used to configure and play videos within the clips.
 * @author colomb2
 */

public class ClipPlayer {

    private static final int CLICK_ACTION_THRESHHOLD = 200;
    protected static final String TEMP_VIDEO_NAME = "tempVideo";
    protected static final String MP4_FORMAT = "mp4";
    protected static final int SEEK_FRAME = 2500;

    /**
     * This method creates a temporary file to store the video file in. It then pulls
     * from firebase storage based on the downloadUrl. When the video has been successfully
     * downloaded, the play button is made visible and the touch player is set up.
     * progress bar: http://stackoverflow.com/questions/7731354/how-to-use-a-seekbar-in-android-
     * as-a-seekbar-as-well-as-a-progressbar-simultaneo
     * @param layout of the clip
     * @param downloadUrl of the video clip in storage
     */
    public static void configureVideo(final LinearLayout layout, String downloadUrl) {
        final Context context = layout.getContext();
        final File localFile;
        final VideoView clipView = (VideoView) layout.findViewById(R.id.clip);
        final ProgressBar progressView = (ProgressBar) layout.findViewById(R.id.progress);
        final ImageView playButton = (ImageView) layout.findViewById(R.id.play_button);
        final ProgressBar loadingView = (ProgressBar) layout.findViewById(R.id.loadingBar);

        try {
            localFile = File.createTempFile(TEMP_VIDEO_NAME, MP4_FORMAT);
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.failedToFindVideo),
                    Toast.LENGTH_LONG).show();
            return;
        }

        StorageReference specifiedVideoRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl(downloadUrl);

        specifiedVideoRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        clipView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                clipView.setOnTouchListener(
                                        new ClipTouchListener(progressView, playButton,
                                                loadingView, clipView));
                            }
                        });

                        clipView.setVideoURI(Uri.parse(localFile.getPath()));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, context.getString(R.string.failedToRetrieveVideo) +
                        "\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This subclass defines a touch listener to play clips. It is necessary as
     * opposed to a click listener, because VideoView does not support click listeners.
     */
    public static class ClipTouchListener implements View.OnTouchListener {

        private static long lastTouchDown = 0;
        private ProgressBar progressBar;
        private ImageView playButton;
        private boolean pastStart;

        /**
         * This constructor creates the touch listener with the progress bar and play button.
         * The video is not past the start, so past start is set to false;
         * @param progressBar of the clip
         * @param playButton of the clip
         */
        public ClipTouchListener (ProgressBar progressBar, ImageView playButton, ProgressBar loadingView, VideoView clipView) {
            this.progressBar = progressBar;
            this.playButton = playButton;
            pastStart = false;
            clipView.seekTo(SEEK_FRAME);
            loadingView.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
        }

        /**
         * This method is called as a user is touching the video. It ensures that it has
         * been at least a threshold number of milliseconds to simulate an onClick method.
         * If this is the first time the button is clicked, it will skip to the beginning
         * of the video.
         * @param clipView video clip view being touched
         * @param event type of motion done by user
         * @return true if successful
         */
        @Override
        public boolean onTouch(View clipView, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastTouchDown = System.currentTimeMillis();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {
                    if (!pastStart) {
                        ((VideoView) clipView).seekTo(0);
                        pastStart = true;
                    }
                    playPauseVideo((VideoView) clipView);
                }
            }

            return true;
        }

        /**
         * This helper method toggles the play/pause state of the video and updates
         * the appropriate UI elements. When the video starts playing, it starts the
         * progress bar syncing.
         * @param videoView view to play video on
         */
        private void playPauseVideo(VideoView videoView) {
            if (videoView.isPlaying()) {
                videoView.pause();
                playButton.setVisibility(View.VISIBLE);
            } else {
                videoView.requestFocus();
                videoView.start();
                configureProgress(progressBar, videoView);
                playButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * This helper method sets up the progress bar for the clip by setting the max position
     * and starting to update the progress.
     * @param progressBar ProgressBar to update
     * @param clipView VideoView to sync progressView with
     */
    private static void configureProgress(ProgressBar progressBar, VideoView clipView) {
        progressBar.setMax(clipView.getDuration());
        progressBar.postDelayed(new updateProgress(progressBar, clipView),
                updateProgress.UPDATE_RATE);
    }

    /**
     * This runnable class continuously updates the progress bar. It stops when the
     * video pauses.
     */
    private static class updateProgress implements Runnable {

        private ProgressBar progressBar;
        private VideoView clipView;

        private static int UPDATE_RATE = 10;

        /**
         * This constructor initializes the runnable instance variables.
         * @param progressBar progress bar to update
         * @param clipView VideoView to sync progress bar with
         */
        public updateProgress(ProgressBar progressBar, VideoView clipView) {
            this.progressBar = progressBar;
            this.clipView = clipView;
        }

        /**
         * This method defines what to do at each iteration of the run. It syncs the
         * progress bar to video position. Then, if the video is playing, it calls itself
         * in a set number of milliseconds.
         */
        @Override
        public void run() {
            int position = clipView.getCurrentPosition();
            progressBar.setProgress(position);

            if (clipView.isPlaying())
                progressBar.postDelayed(this, UPDATE_RATE);

        }
    }

}
