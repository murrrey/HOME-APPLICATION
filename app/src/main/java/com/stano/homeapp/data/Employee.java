package com.stano.homeapp.data;

import com.google.android.gms.maps.model.Marker;

import androidx.annotation.NonNull;

public class Employee extends User {
    private String service, skill;

    public Employee() {
    }


    public Employee(@NonNull String name, @NonNull String phone, @NonNull String service, @NonNull String skill,
                    @NonNull String profileImageUrl,
                    @NonNull String id, @NonNull String ratings) {
        this.name = name;
        this.phone = phone;
        this.service = service;
        this.skill = skill;
        this.profileImageUrl = profileImageUrl;
        this.id = id;
        this.ratings = ratings;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public @NonNull
    String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public @NonNull
    String getRatings() {
        return ratings;
    }

    public void setRatings(@NonNull String ratings) {
        this.ratings = ratings;
    }

    public @NonNull
    String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public @NonNull
    String getPhone() {
        return phone;
    }

    public void setPhone(@NonNull String phone) {
        this.phone = phone;
    }

    public @NonNull
    String getService() {
        return service;
    }

    public void setService(@NonNull String service) {
        this.service = service;
    }


    public @NonNull
    String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(@NonNull String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
