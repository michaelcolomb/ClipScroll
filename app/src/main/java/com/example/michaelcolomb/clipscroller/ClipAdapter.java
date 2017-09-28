package com.example.michaelcolomb.clipscroller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.iid.InstanceID;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a RecyclerAdapter. It sets up the recycler view such that
 * each list item displays the title of the clip and video. Upon clicking the item, the
 * video will start.
 */
public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipViewHolder> {

    private Context mContext;
    private DatabaseReference mEntriesReference;
    private ArrayList<String> clipHashes;

    private static Drawable upSelected;
    private static Drawable upUnselected;
    private static Drawable downSelected;
    private static Drawable downUnselected;

    private static final String SELECTED = "selected";
    private static final String UNSELECTED = "unselected";
    private static final String LIKES_ADDRESS = "/likes";
    private static final String TIME_ADDRESS = "/time";
    private static final int MAX_CLIP_RESULTS = 100;

    /**
     * This private constructor initializes instance class variables, including
     * setting the vote icons, database reference, context, and empty clipHashes.
     * @param context to use for android interaction
     * @param ref Query to use as DatabaseReference to access clips
     */
    private ClipAdapter(Context context, Query ref) {
        upSelected = context.getDrawable(R.drawable.ic_up_selected);
        upUnselected = context.getDrawable(R.drawable.ic_up_unselected);
        downSelected = context.getDrawable(R.drawable.ic_down_selected);
        downUnselected = context.getDrawable(R.drawable.ic_down_unselected);

        this.mEntriesReference = (DatabaseReference) ref;
        this.mContext = context;
        this.clipHashes = new ArrayList();
    }

    /**
     * This method creates a new ClipAdapter by generating an empty instance and
     * loading clip data from the database.
     * @param context to use for android interaction
     * @param ref Query to use as DatabaseReference to access clips
     * @return a new, populated ClipAdapter
     */
    public static ClipAdapter create(Context context, Query ref) {
        ClipAdapter clipAdapter = new ClipAdapter(context, ref);
        clipAdapter.loadList();
        return clipAdapter;
    }

    /**
     * This helper method first clears the application's cache to prevent memory overflow
     * from previous adapters. It then adds a single value event listener to the clips
     * database reference to get all of the clips stored in the database. It uses ClipRanks
     * to order the list, which is then stored in the instance 'clipHashes' list.
     */
    private void loadList() {
        clearCache();

        mEntriesReference.limitToFirst(MAX_CLIP_RESULTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ClipRank> rankedHashes = new ArrayList();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String clipHash = postSnapshot.getKey();

                    DataSnapshot timeSnapshot = postSnapshot.child(TIME_ADDRESS);
                    long time = timeSnapshot.getValue(long.class);

                    DataSnapshot likeSnapshot = postSnapshot.child(LIKES_ADDRESS);
                    ClipRank clipRank = ClipRank.create(clipHash, time, likeSnapshot);
                    rankedHashes.add(clipRank);
                }

                Collections.sort(rankedHashes);
                clipHashes.clear();

                for (int i = 0; i < rankedHashes.size(); i++) {
                    clipHashes.add(rankedHashes.get(i).getHash());
                }

                notifyItemRangeChanged(0, rankedHashes.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    /**
     * This helper method clears this applications cache to ensure that there is
     * no memory overflow after using the application for an extended period of time.
     */
    private void clearCache() {
        File cacheDir = mContext.getCacheDir();
        File[] files = cacheDir.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * This method is called when a view holder is created. It inflates the holder
     * with a clip list item.
     * @param viewGroup of parent view
     * @param position of the holder within the recycler view
     * @return the inflated view holder
     */
    @Override
    public ClipViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.clip_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup,
                shouldAttachToParentImmediately);
        ClipViewHolder viewHolder = new ClipViewHolder(view);

        return viewHolder;
    }

    /**
     * This method is called when a view holder is bound to a clip. It sets the appropriate
     * UI elements to the corresponding clip's values.
     * @param viewHolder to bind the clip to
     * @param position of clip hashed key in the list
     */
    @Override
    public void onBindViewHolder(ClipViewHolder viewHolder, int position) {
        viewHolder.bind(clipHashes.get(position));
    }

    /**
     * This method is used to get the lenght of the clip hash key list within the
     * recycler view
     * @return length of clipHashes
     */
    @Override
    public int getItemCount() {
        return clipHashes.size();
    }

    /**
     * This class defines a recycler ViewHolder for a clip. It stores references
     * to the necessary views and sets up the proper listeners when the view
     * holder is bound to a clip.
     */
    public class ClipViewHolder extends RecyclerView.ViewHolder {

        public View mContainerView;
        public TextView mTitleView;
        public VideoView mClipView;
        public ImageView mUpvote;
        public ImageView mDownvote;
        public TextView mVoteCount;

        /**
         * This constructor initializes the view references for the holder.
         * @param itemView containing view of the holder
         */
        public ClipViewHolder(View itemView) {
            super(itemView);

            mContainerView = itemView;
            mTitleView = (TextView) itemView.findViewById(R.id.clip_list_title);
            mClipView = (VideoView) itemView.findViewById(R.id.clip);
            mUpvote = (ImageView) itemView.findViewById(R.id.upvote);
            mDownvote = (ImageView) itemView.findViewById(R.id.downvote);
            mVoteCount = (TextView) itemView.findViewById(R.id.vote_count);
        }

        /**
         * This method calls on the databse at the inputted clip reference. It parses
         * the returned data to configure the current view holder. The clip video and
         * vote functionality are configured with the appropriate listeners.
         * @param referenceId hash key for specific clip
         */
        protected void bind(final String referenceId) {
            mEntriesReference.child(referenceId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Clip clip = dataSnapshot.getValue(Clip.class);

                    mTitleView.setText(clip.getTitle());
                    ClipPlayer.configureVideo((LinearLayout) mContainerView, clip.getDownloadUrl());

                    mUpvote.setOnClickListener(new voteClicked(ClipViewHolder.this, referenceId));
                    mDownvote.setOnClickListener(new voteClicked(ClipViewHolder.this, referenceId));

                    mEntriesReference.child(referenceId).child(LIKES_ADDRESS)
                            .addValueEventListener(new voteManager(ClipViewHolder.this));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
    }

    /**
     * This class defines an OnClickListener to be called when a vote button is
     * clicked.
     */
    private class voteClicked implements View.OnClickListener {

        private ImageView upvote;
        private ImageView downvote;
        private String referenceId;

        /**
         * This constructor stores references to the vote buttons and the clips hash key
         * @param viewHolder of parent containing vote buttons
         * @param referenceId clip hash key
         */
        public voteClicked(ClipViewHolder viewHolder, String referenceId) {
            this.upvote = viewHolder.mUpvote;
            this.downvote = viewHolder.mDownvote;
            this.referenceId = referenceId;
        }

        /**
         * This method is called when a vote button is clicked. It takes the appropriate
         * action by updating the UI according to which button the user selected and
         * whether that button was already clicked.
         * @param view view of the vote button that was clicked
         */
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.upvote) {
                if (upvote.getTag().equals(UNSELECTED)) {
                    upvote.setImageDrawable(upSelected);
                    upvote.setTag(SELECTED);
                    downvote.setImageDrawable(downUnselected);
                    downvote.setTag(UNSELECTED);
                    vote(true);
                } else {
                    upvote.setImageDrawable(upUnselected);
                    upvote.setTag(UNSELECTED);
                    removeVote();
                }
            } else if (view.getId() == R.id.downvote) {
                if (downvote.getTag().equals(UNSELECTED)) {
                    downvote.setImageDrawable(downSelected);
                    downvote.setTag(SELECTED);
                    upvote.setImageDrawable(upUnselected);
                    upvote.setTag(UNSELECTED);
                    vote(false);
                } else {
                    downvote.setImageDrawable(downUnselected);
                    downvote.setTag(UNSELECTED);
                    removeVote();
                }
            }
        }

        /**
         * This method stores a vote in the database by updating the likes reference
         * using a map.
         * @param voteDirection true if upvote, false otherwise
         */
        private void vote(boolean voteDirection) {
            DatabaseReference likeReference =
                    mEntriesReference.child(referenceId).child(LIKES_ADDRESS);
            String instanceId = InstanceID
                    .getInstance(upvote.getContext()).getId();

            Map<String, Object> updatedLikes = new HashMap();
            updatedLikes.put(instanceId, voteDirection);

            likeReference.updateChildren(updatedLikes);
        }

        /**
         * This method removes the vote of the current user.
         */
        private void removeVote() {
            DatabaseReference likeReference = mEntriesReference
                    .child(referenceId).child(LIKES_ADDRESS);
            String instanceId = InstanceID
                    .getInstance(upvote.getContext()).getId();

            likeReference.child(instanceId).removeValue();
        }
    }

    /**
     * This class defines a ValueEventListener that listens for vote count changes.
     */
    private class voteManager implements ValueEventListener {

        private ClipViewHolder mViewHolder;

        /**
         * This constructor stores a reference to the list item's view holder.
         * @param viewHolder
         */
        public voteManager(ClipViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        /**
         * This method uses VoteUtilities to count the votes and determine which, if either,
         * vote button should be selected for the user with the retrieved instance id.
         * @param dataSnapshot DataSnapshot for the likes database reference
         */
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String instanceId = InstanceID
                    .getInstance(mViewHolder.mContainerView.getContext()).getId();
            int likeCount = VoteUtilities.getVoteCount(dataSnapshot);

            mViewHolder.mVoteCount.setText(String.valueOf(likeCount));

            int voteDirection = VoteUtilities.getVoteDirection(dataSnapshot, instanceId);

            if (voteDirection == 1) {
                mViewHolder.mUpvote.setImageDrawable(upSelected);
                mViewHolder.mDownvote.setImageDrawable(downUnselected);
                mViewHolder.mUpvote.setTag(SELECTED);
                mViewHolder.mDownvote.setTag(UNSELECTED);
            } else if (voteDirection == -1) {
                mViewHolder.mDownvote.setImageDrawable(downSelected);
                mViewHolder.mUpvote.setImageDrawable(upUnselected);
                mViewHolder.mDownvote.setTag(SELECTED);
                mViewHolder.mUpvote.setTag(UNSELECTED);
            } else {
                mViewHolder.mDownvote.setImageDrawable(downUnselected);
                mViewHolder.mUpvote.setImageDrawable(upUnselected);
                mViewHolder.mDownvote.setTag(SELECTED);
                mViewHolder.mUpvote.setTag(UNSELECTED);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) { }
    }
}



