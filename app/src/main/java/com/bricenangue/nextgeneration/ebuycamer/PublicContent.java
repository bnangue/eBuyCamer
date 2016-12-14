package com.bricenangue.nextgeneration.ebuycamer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bricenangue on 06/12/2016.
 */

public class PublicContent {
    private long numberofView;
    private HashMap<String, String> viewers;

    private long numberoflikes;
    private HashMap<String, String> likers;

    public PublicContent() {

    }

    public PublicContent(long numberofView, HashMap<String, String> viewers, long numberoflikes, HashMap<String, String> likers) {
        this.numberofView = numberofView;
        this.viewers = viewers;
        this.numberoflikes = numberoflikes;
        this.likers = likers;
    }

    public long getNumberofView() {
        return numberofView;
    }

    public void setNumberofView(long numberofView) {
        this.numberofView = numberofView;
    }

    public HashMap<String, String> getViewers() {
        return viewers;
    }

    public void setViewers(HashMap<String, String> viewers) {
        this.viewers = viewers;
    }

    public long getNumberoflikes() {
        return numberoflikes;
    }

    public void setNumberoflikes(long numberoflikes) {
        this.numberoflikes = numberoflikes;
    }

    public HashMap<String, String> getLikers() {
        return likers;
    }

    public void setLikers(HashMap<String, String> likers) {
        this.likers = likers;
    }
}
