package com.example.michaelcolomb.clipscroller;

import com.google.firebase.database.DataSnapshot;

/**
 * This class is used to rank clips. The entire clips with the videos do
 * not need to be loaded immediately.
 */

public class ClipRank implements Comparable<ClipRank> {

    private static boolean rankByTrending = false;

    private String hash;
    private double rankValue;

    /**
     * This constructor generates a ClipRank object, setting the instance class
     * variables to the inputted values.
     * @param hash to initialize this.hash to
     * @param rankValue to initialize this.rankValue to
     */
    private ClipRank(String hash, double rankValue) {
        this.hash = hash;
        this.rankValue = rankValue;
    }

    /**
     * This create method determines the rankValue that a clip should have
     * based on how long ago it was created and the vote count depending
     * on the value of rankByTrending. It creates ClipRank object based on
     * this information and returns it.
     * @param hash to initialize this.hash to
     * @param time epoch time that the clip was created
     * @param likesSnapshot DataSnapShot that refers to the '/likes' reference level
     * @return
     */
    public static ClipRank create(String hash, long time, DataSnapshot likesSnapshot) {
        int voteCount = VoteUtilities.getVoteCount(likesSnapshot);
        double rankValue;
        long timeSincePublished = System.currentTimeMillis() - time;

        if (rankByTrending)
            rankValue = voteCount - (timeSincePublished / 60000);
        else
            rankValue = -1 * timeSincePublished;

        return new ClipRank(hash, rankValue);
    }

    /**
     * This method compares this ClipRank to another ClipRank object.
     * @param other the other ClipRank object
     * @return positive if this has higher rank than other, 0 if same, negative otherwise
     */
    public int compareTo(ClipRank other) {
        return (int) (other.getRank() - this.rankValue);
    }

    public String getHash() {
        return hash;
    }

    public double getRank() {
        return rankValue;
    }

    public static void setRankByTrending(boolean rankByTrending) {
        ClipRank.rankByTrending = rankByTrending;
    }

}
