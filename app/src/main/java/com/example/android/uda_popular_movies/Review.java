package com.example.android.uda_popular_movies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a single movie review.
 * We will use this class to hold the data that came with JSON.
 *
 * Created by Ozgun Ulusoy on 22.08.2015.
 */

public class Review implements Parcelable{

    private String id;
    private String author;
    private String content;
    private String url;

    // Constructor
    public Review() {}

    // Constructor to be used when building object from a PARCEL
    public Review(Parcel in) {
        readFromParcel(in);
    }

    // Get methods below
    public String getId() {return id;}
    public String getAuthor() {return author;}
    public String getContent() {return content;}
    public String getUrl() {return url;}

    // Set methods below
    public void setId(String id) {this.id = id;}
    public void setAuthor(String author) {this.author = author;}
    public void setContent(String content) {this.content = content;}
    public void setUrl(String url) {this.url = url;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readString();
        this.author = in.readString();
        this.content = in.readString();
        this.url = in.readString();
    }
}