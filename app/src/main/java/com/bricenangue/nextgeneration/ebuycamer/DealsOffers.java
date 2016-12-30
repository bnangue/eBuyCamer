package com.bricenangue.nextgeneration.ebuycamer;

import java.util.HashMap;

/**
 * Created by bricenangue on 24/12/2016.
 */

public class DealsOffers {
    private HashMap<String, Offer> offers=new HashMap<>();
    private long numberOfoffers;


    public DealsOffers() {

    }

    public HashMap<String, Offer> getOffers() {
        return offers;
    }

    public void setOffers(HashMap<String, Offer> offers) {
        this.offers = offers;
    }

    public long getNumberOfoffers() {
        return numberOfoffers;
    }

    public void setNumberOfoffers(long numberOfoffers) {
        this.numberOfoffers = numberOfoffers;
    }
}
