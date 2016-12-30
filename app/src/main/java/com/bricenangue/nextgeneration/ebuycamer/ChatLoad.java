package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 28/12/2016.
 * this class create an instance of a chat in firebase realtime database
 */

public class ChatLoad {
    private String buyer_name;
    private String path_creator_uid;
    private String path_post_id;
    private String path_buyer;
    private String lastmessage;
    private long lastmessage_timestamp;
    private String post_first_image;
    private String post_title;
    private String is_deal;
    private boolean isNotseen;
    private String creator_name;


    public ChatLoad(String buyer_name
            , String path_creator_uid, String path_post_id
            , String path_buyer, String lastmessage, long lastmessage_timestamp
            , String post_first_image, String post_title, String is_deal,boolean isNotseen
    ,String creator_name) {
        this.buyer_name = buyer_name;
        this.path_creator_uid = path_creator_uid;
        this.path_post_id = path_post_id;
        this.path_buyer = path_buyer;
        this.lastmessage = lastmessage;
        this.lastmessage_timestamp = lastmessage_timestamp;
        this.post_first_image = post_first_image;
        this.post_title = post_title;
        this.is_deal = is_deal;
        this.isNotseen=isNotseen;
        this.creator_name=creator_name;
    }

    public ChatLoad() {
    }

    public String getBuyer_name() {
        return buyer_name;
    }

    public void setBuyer_name(String buyer_name) {
        this.buyer_name = buyer_name;
    }

    public String getPath_creator_uid() {
        return path_creator_uid;
    }

    public void setPath_creator_uid(String path_creator_uid) {
        this.path_creator_uid = path_creator_uid;
    }

    public String getPath_post_id() {
        return path_post_id;
    }

    public void setPath_post_id(String path_post_id) {
        this.path_post_id = path_post_id;
    }

    public String getPath_buyer() {
        return path_buyer;
    }

    public void setPath_buyer(String path_buyer) {
        this.path_buyer = path_buyer;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public long getLastmessage_timestamp() {
        return lastmessage_timestamp;
    }

    public void setLastmessage_timestamp(long lastmessage_timestamp) {
        this.lastmessage_timestamp = lastmessage_timestamp;
    }

    public String getPost_first_image() {
        return post_first_image;
    }

    public void setPost_first_image(String post_first_image) {
        this.post_first_image = post_first_image;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getIs_deal() {
        return is_deal;
    }

    public void setIs_deal(String is_deal) {
        this.is_deal = is_deal;
    }

    public boolean isNotseen() {
        return isNotseen;
    }

    public void setNotseen(boolean notseen) {
        this.isNotseen = notseen;
    }

    public String getCreator_name() {
        return creator_name;
    }

    public void setCreator_name(String creator_name) {
        this.creator_name = creator_name;
    }
}
