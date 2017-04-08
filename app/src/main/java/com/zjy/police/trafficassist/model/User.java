package com.zjy.police.trafficassist.model;

/**
 * Created by ZJY on 2016/10/27.
 */
import com.amap.api.maps.model.LatLng;

public class User {

    private String username;
    private String password;
    private String nickname;
    private LatLng location;

    public User(){}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.nickname = username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
