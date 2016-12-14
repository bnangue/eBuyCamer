package com.bricenangue.nextgeneration.ebuycamer;

import java.util.Map;

/**
 * Created by bricenangue on 29/11/2016.
 */

public class Categories {
    private String name;
    private Map<String,String> subcategories;
    private int catNumber;

    public Categories() {
    }

    public  Categories(String name, Map<String,String> subcategories,int catNumber){
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
