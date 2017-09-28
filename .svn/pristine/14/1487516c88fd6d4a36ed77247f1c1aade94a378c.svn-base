package com.example.michaelcolomb.clipscroller;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * This class provides one main test, that relies on the process of a adding and then retrieving
 * a clip. It verfies that the clip is properly stored and then can be recieved. It also contains
 * a listener class for use in retrieving the stored clip.
 *
 * @author colomb2
 */
public class AddClipTest {

    private final String TEST_CLIP_TITLE = "Example Title";
    private final String TEST_CLIP_URL = "Download URL Example";

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mEntriesReference = mDatabase.getReference(MainActivity.CLIP_ADDRESS);

    /**
     * This test adds a clip to the database. A listener is added to the clip reference so that
     * after the attempt to add the clip is done, it can be recalled. By validating this returned
     * clip, the successful storage and retrieval of the clip are both tested.
     * @throws Exception if the clip is not stored or retrieved properly.
     */
    @Test
    public void testAddClip() throws Exception {

        mEntriesReference.limitToLast(1).addChildEventListener(new AddClipListener());

        final CountDownLatch writeSignal = new CountDownLatch(1);

        Clip testClip = new Clip(TEST_CLIP_TITLE, TEST_CLIP_URL);

        mEntriesReference.push().setValue(testClip)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });

        writeSignal.await(10, TimeUnit.SECONDS);
    }

    /**
     * This listener class validates the successful retrieval of the stored clip. It is called
     * when a child of the clip-level reference is manipulated.
     */
    public class AddClipListener implements ChildEventListener {
        /**
         * Validate that the added child is the one added during the push.
         * @param dataSnapshot      data container for the response, contains the clip
         * @param previousChildName of the child
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Clip retrievedClip = dataSnapshot.getValue(Clip.class);
            assertEquals(TEST_CLIP_TITLE, retrievedClip.getTitle());
            assertEquals(TEST_CLIP_URL, retrievedClip.getDownloadUrl());
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
        }

        @Override
        public void onCancelled(DatabaseError error) {
        }
    }
}