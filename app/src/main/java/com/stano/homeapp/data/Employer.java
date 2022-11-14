package com.stano.homeapp.data;

import java.util.List;

import androidx.annotation.NonNull;

public class Employer extends User {
    public Employer() {
    }

    public Employer(@NonNull String name, @NonNull String phone, @NonNull String email,
                    @NonNull String id, @NonNull String ratings) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.id = id;
        this.ratings = ratings;

    }

    public @NonNull
    String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public @NonNull
    String getRatings() {
        return ratings;
    }

    public void setRatings(@NonNull String ratings) {
        this.ratings = ratings;
    }
}
