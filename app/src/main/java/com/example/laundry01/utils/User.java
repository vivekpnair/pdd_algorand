package com.example.laundry01.utils;

import com.google.gson.JsonObject;

public class User {

    private static User uniqueInstance;


    private JsonObject user;
    private Boolean loggedOn = false;

    private User()
    {
        // user is only one instance for entire app...so make it a singleton
    }

    public static User getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new User();
        return uniqueInstance;
    }
    public void setUserInfo(JsonObject user, Boolean loggedOn) {
        this.user = user;
        this.loggedOn = loggedOn;
    }

    public JsonObject getUser() {
        return user;
    }

    public Boolean isAdmin() {
        return user.get("user_info").getAsJsonObject().get("is_admin").getAsBoolean();
    }

    public String getUserName() {
        return user.get("user_info").getAsJsonObject().get("name").getAsString();
    }
    public String getUserInst() {
        return user.get("user_info").getAsJsonObject().get("inst").getAsString();
    }

    public void setUser(JsonObject user) {
        this.user = user;
    }

    public Boolean getLoggedOn() {
        return loggedOn;
    }

    public void setLoggedOn(Boolean loggedOn) {
        this.loggedOn = loggedOn;
    }
}
