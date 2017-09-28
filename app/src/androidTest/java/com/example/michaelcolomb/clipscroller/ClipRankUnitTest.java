package com.example.michaelcolomb.clipscroller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * This class contains unit tests to test the ability to rank clips in different ways.
 * @author colomb2
 */
public class ClipRankUnitTest {

    private static final String TEST_ADDRESS = "/test_rank";
    private static final String CONNECTION_ERROR = "Could not connect to test database";
    private static final String[] notTrendingHashes = {"user4", "user3", "user2", "user1"};
    private static final String[] trendingHashes = {"user1", "user2", "user4", "user3"};
    private static final String[] trendingHashesWithTime = {"user4", "user2", "user1", "user3"};
    private static final int M_MINUTE = 60000;
    private static final int S_10_SECONDS = 10;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mEntriesReference = mDatabase.getReference(TEST_ADDRESS);

    /**
     * Test the the ranking system when in 'non-trending' mode.
     * @throws Exception if failed test
     */
    @Test
    public void testRankNotTrending() throws Exception {
        ClipRank.setRankByTrending(false);
        final ArrayList<ClipRank> ranks = new ArrayList();

        final CountDownLatch writeSignal = new CountDownLatch(1);

        mEntriesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int timeCounter = 0;

                for (DataSnapshot likeSnapshot: dataSnapshot.getChildren()) {
                    ranks.add(ClipRank.create(likeSnapshot.getKey(), timeCounter, likeSnapshot));
                    timeCounter += M_MINUTE;
                }

                writeSignal.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail(CONNECTION_ERROR);
            }
        });

        writeSignal.await(S_10_SECONDS, TimeUnit.SECONDS);

        Collections.sort(ranks);

        String[] hashes = new String[ranks.size()];

        for (int i = 0; i < ranks.size(); i++) {
            hashes[i] = ranks.get(i).getHash();
        }

        assertArrayEquals(notTrendingHashes, hashes);
    }

    /**
     * Test the ranking system when in 'trending' mode. Tests for when time should
     * not affect ranking and for when time should affect ranking.
     * @throws Exception if failed test
     */
    @Test
    public void testRankTrending() throws Exception {
        ClipRank.setRankByTrending(true);
        final ArrayList<ClipRank> ranksSameTime = new ArrayList();
        final ArrayList<ClipRank> ranksDifferentTime = new ArrayList();

        final CountDownLatch writeSignal = new CountDownLatch(1);

        mEntriesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int timeCounter = 0;

                for (DataSnapshot likeSnapshot: dataSnapshot.getChildren()) {
                    ranksSameTime.add(ClipRank.create(likeSnapshot.getKey(),
                            0, likeSnapshot));
                    ranksDifferentTime.add(ClipRank.create(likeSnapshot.getKey(),
                            timeCounter, likeSnapshot));
                    timeCounter += 2 * M_MINUTE;
                }

                writeSignal.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail(CONNECTION_ERROR);
            }
        });

        writeSignal.await(S_10_SECONDS, TimeUnit.SECONDS);

        Collections.sort(ranksSameTime);
        Collections.sort(ranksDifferentTime);

        String[] hashesSameTime = new String[ranksSameTime.size()];
        String[] hashesDifferentTime = new String[ranksDifferentTime.size()];

        for (int i = 0; i < ranksSameTime.size(); i++) {
            hashesSameTime[i] = ranksSameTime.get(i).getHash();
            hashesDifferentTime[i] = ranksDifferentTime.get(i).getHash();
        }

        assertArrayEquals(trendingHashes, hashesSameTime);
        assertArrayEquals(trendingHashesWithTime, hashesDifferentTime);
    }
}
