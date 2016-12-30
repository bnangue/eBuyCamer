package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 26/12/2016.
 */

public class Offer {
    private String offermade;
    private long time;

    public Offer() {
    }

    public Offer(String offermade, long time) {
        this.offermade = offermade;
        this.time = time;
    }

    public String getOffermade() {
        return offermade;
    }

    public void setOffermade(String offermade) {
        this.offermade = offermade;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
