package dev.datvt.cloudtracks.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by datvt on 8/15/2016.
 */
public class MySong {

    @SerializedName("context")
    public String context;
    @SerializedName("instrumental")
    public String instrumental;
    @SerializedName("snippet")
    public String snippet;
    @SerializedName("title")
    public String title;
    @SerializedName("url")
    public String url;
    @SerializedName("viewable")
    public String viewable;
    @SerializedName("artist")
    public MyArtist artist;
}
