package com.example.android.uda_popular_movies;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URL;
import java.util.Date;

/**
 * This class represents a single movie. We will use this class to hold the data that came with JSON.
 * We will pass instances of this class between fragments and activities as needed, so the class
 * implements Parcelable interface.
 *
 * Created by Ozgun Ulusoy on 22.08.2015.
 */

public class Movie implements Parcelable {

    private String id;
    private String title;
    private String originalTitle;
    private String overview;
    private String releaseDate;
    private String posterPath;
    private double popularity;
    private String videoStatus;
    private int voteCount;
    private double voteAverage;
    //private boolean isFavorite;

    // Default constructor to be used when creating a regular object
    public Movie(){}

    // Constructor to be used when building object from a PARCEL
    public Movie(Parcel in) {
        readFromParcel(in);
    }

    // Get Methods Below

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getOriginalTitle() {return originalTitle;}
    public String getOverview() {return overview;}
    public String getReleaseDate() {return releaseDate;}
    public String getPosterPath() {return posterPath;}
    public double getPopularity() {return popularity;}
    public String getVideoStatus() {return videoStatus;}
    public int getVoteCount() {return voteCount;}
    public double getVoteAverage() {return voteAverage;}
    //public boolean getIsFavorite() {return isFavorite;}

    // Set Methods Below

    public void setId(String id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setOriginalTitle(String originalTitle) {this.originalTitle = originalTitle;}
    public void setOverview(String overview) {this.overview = overview;}
    public void setReleaseDate(String releaseDate) {this.releaseDate = releaseDate;}
    public void setPosterPath(String posterPath) {this.posterPath = posterPath;}
    public void setPopularity(double popularity) {this.popularity = popularity;}
    public void setVoteCount(int voteCount) {this.voteCount = voteCount;}
    public void setVoteAverage(double voteAverage) {this.voteAverage = voteAverage;}
    public void setVideoStatus(String videoStatus) { this.videoStatus = videoStatus;}
    //public void setIsFavorite(Boolean status) { this.isFavorite = status;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(posterPath);
        dest.writeDouble(popularity);
        dest.writeInt(voteCount);
        dest.writeDouble(voteAverage);
        dest.writeString(videoStatus);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.originalTitle = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.popularity = in.readDouble();
        this.voteCount = in.readInt();
        this.voteAverage = in.readDouble();
        this.videoStatus = in.readString();
    }
}