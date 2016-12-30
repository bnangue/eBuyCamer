package com.bricenangue.nextgeneration.ebuycamer;

import java.util.Map;

/**
 * Created by bricenangue on 30/12/2016.
 */

public class CategoriesDeal {
    private String name;
    private Map<String,String> subcategories;
    private int catNumber;

    public CategoriesDeal() {
    }

    public  CategoriesDeal(String name, Map<String,String> subcategories,int catNumber){
        this.name=name;
        this.subcategories=subcategories;
        this.catNumber=catNumber;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(Map<String, String> subcategories) {
        this.subcategories = subcategories;
    }

    public int getCatNumber() {
        return catNumber;
    }

    public void setCatNumber(int catNumber) {
        this.catNumber = catNumber;
    }
}
