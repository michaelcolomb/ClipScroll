package com.example.michaelcolomb.clipscroller;

/**
 * This class stores a 'clip'. This means it stores the video address to find a video
 * file in Firebase Storage and the user-chosen title of the video. It also stores
 * creation time.
 * @author colomb2
 */

public class Clip {

    public String title;
    public String downloadUrl;
    public long time;

    public Clip() {}

    /**
     * This constructor allows the creation of a clip with defined title and downloadUrl.
     * It also sets the time to the current epoch time.
     * @param title user-chosen title of the video
     * @param downloadUrl address of the associated file in Firebase Storage
     */
    public Clip(String title, String downloadUrl) {
        this.title = title;
        this.downloadUrl = downloadUrl;
        time = System.currentTimeMillis();
    }

    public String toString() {
        return title + " " + downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
