package com.bricenangue.nextgeneration.ebuycamer;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class Publication {
    PrivateContent privateContent;
    PublicContent publicContent;
    private CategoriesDeal categoriesDeal;


    public Publication() {
    }

    public Publication(PrivateContent privateContent, PublicContent publicContent) {
        this.privateContent = privateContent;
        this.publicContent = publicContent;
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

    public CategoriesDeal getCategoriesDeal() {
        return categoriesDeal;
    }

    public void setCategoriesDeal(CategoriesDeal categoriesDeal) {
        this.categoriesDeal = categoriesDeal;
    }
}
