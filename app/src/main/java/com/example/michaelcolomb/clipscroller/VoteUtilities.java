package com.example.michaelcolomb.clipscroller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

/**
 * This class defines methods that are useful for handling vote operations.
 * @author colomb2
 */

public class VoteUtilities {

    /**
     * This method reads through a clip's votes to find the total count. Upvotes count one
     * positive and downvotes count one negative.
     * @param likesSnapshot DataSnapShot that refers to the '/likes' reference level
     * @return total sum of votes
     */
    public static int getVoteCount(DataSnapshot likesSnapshot) {
        int voteCount = 0;

        for (DataSnapshot postSnapshot: likesSnapshot.getChildren()) {
            boolean voteDirection = postSnapshot.getValue(boolean.class);

            if (voteDirection)
                voteCount++;
            else
                voteCount--;
        }

        return voteCount;
    }

    /**
     * This method returns the direction of the specified user's vote.
     * @param likesSnapshot DataSnapShot that refers to the '/likes' reference level
     * @param instanceId user's instance id
     * @return 1 if upvote, 0 if no vote, -1 if downvote
     */
    public static int getVoteDirection(DataSnapshot likesSnapshot, String instanceId) {
        for (DataSnapshot postSnapshot: likesSnapshot.getChildren()) {
            if (postSnapshot.getKey().equals(instanceId)) {
                boolean voteDirection = postSnapshot.getValue(boolean.class);

                if (voteDirection)
                    return 1;

                return -1;
            }
        }

        return 0;
    }
}
