package com.bricenangue.nextgeneration.ebuycamer;

/**
 * Created by bricenangue on 28/12/2016.
 */

public class PhoneNumber {
    private String code;
    private String phoneNumber;

    public PhoneNumber() {
    }

    public PhoneNumber(String code, String phonenumber) {
        this.code=code;
        this.phoneNumber=phonenumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
