package com.example.android.uda_popular_movies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a trailer object.
 *
 * Created by Ozgun Ulusoy on 02.09.2015.
 */

public class Trailer implements Parcelable {

    private String videoId;
    private String urlKey;
    private String name;
    private String site;
    private String size;
    private String url;

    // Constructor
    public Trailer() {}

    // Constructor to be used when building object from a PARCEL
    public Trailer(Parcel in) {
        readFromParcel(in);
    }

    // Get methods below
    public String getVideoId() {return videoId;}
    public String getUrlKey() {return urlKey;}
    public String getName() {return name;}
    public String getSize() {return size;}
    public String getSite() {return site;}


    // Set methods below
    public void setVideoId(String id) {this.videoId = videoId;}
    public void setUrlKey(String urlKey) {this.urlKey = urlKey;}
    public void setName(String name) {this.name = name;}
    public void setSize(String size) {this.size = size;}
    public void setSite(String site) {this.site = site;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(url);
        dest.writeString(urlKey);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(size);
    }

    public void readFromParcel(Parcel in) {
        this.videoId = in.readString();
        this.url = in.readString();
        this.urlKey = in.readString();
        this.name = in.readString();
        this.site = in.readString();
        this.size = in.readString();
    }

}
