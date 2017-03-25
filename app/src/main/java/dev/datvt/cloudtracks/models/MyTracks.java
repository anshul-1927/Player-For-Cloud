package dev.datvt.cloudtracks.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by datvt on 8/15/2016.
 */
public class MyTracks {
    @SerializedName("name")
    public String name;
    @SerializedName("articleUrl")
    public String articleUrl;
    @SerializedName("lyrics")
    public String lyrics;
    @SerializedName("artist")
    public MyArtist artist;
}
