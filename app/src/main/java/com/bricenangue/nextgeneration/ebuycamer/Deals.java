package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 24/12/2016.
 */

public class Deals {
    private PrivateContent privateContent;
    private PublicContent publicContent;
    private DealsOffers offers;
    private CategoriesDeal categoriesDeal;


    public Deals() {
    }

    public PrivateContent getPrivateContent() {
        return privateContent;
    }

    public void setPrivateContent(PrivateContent privateContent) {
        this.privateContent = privateContent;
    }

    public PublicContent getPublicContent() {
        return publicContent;
    }

    public void setPublicContent(PublicContent publicContent) {
        this.publicContent = publicContent;
    }

    public DealsOffers getOffers() {
        return offers;
    }

    public void setOffers(DealsOffers offers) {
        this.offers = offers;
    }

    public CategoriesDeal getCategoriesDeal() {
        return categoriesDeal;
    }

    public void setCategoriesDeal(CategoriesDeal categoriesDeal) {
        this.categoriesDeal = categoriesDeal;
    }
}
