package dev.datvt.cloudtracks.lyrics_tools_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import dev.datvt.cloudtracks.song_player.Lyrics;
import dev.datvt.cloudtracks.utils.Net;

public class Bollywood {

    public static final String domain = "quicklyric.azurewebsites.net/bollywood/";

    public static ArrayList<Lyrics> search(String query) {
        ArrayList<Lyrics> results = new ArrayList<>();
        String searchUrl = "http://quicklyric.azurewebsites.net/bollywood/search.php?q=%s";
        try {
            String jsonText;
            jsonText = Net.getUrlAsString(String.format(searchUrl, URLEncoder.encode(query, "utf-8")));
            JsonObject jsonResponse = new JsonParser().parse(jsonText).getAsJsonObject();
            JsonArray lyricsResults = jsonResponse.getAsJsonArray("lyrics");
            if (lyricsResults != null)
                for (int i = 0; i < lyricsResults.size(); ++i) {
                    JsonObject lyricsResult = lyricsResults.get(i).getAsJsonObject();
                    JsonArray tags = lyricsResult.get("tags").getAsJsonArray();
                    Lyrics lyrics = new Lyrics(Lyrics.SEARCH_ITEM);
                    lyrics.setTitle(lyricsResult.get("name").getAsString());
                    for (int j = 0; j < tags.size(); ++j) {
                        JsonObject tag = tags.get(j).getAsJsonObject();
                        if (tag.get("tag_type").getAsString().equals("Singer")) {
                            lyrics.setArtist(tag.get("name").getAsString().trim());
                            break;
                        }
                    }
                    lyrics.setURL("http://quicklyric.azurewebsites.net/bollywood/get.php?id=" + lyricsResult.get("id").getAsInt());
                    results.add(lyrics);
                }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static Lyrics fromMetaData(String artist, String title) {
        ArrayList<Lyrics> searchResults = search(artist + " " + title);
        for (Lyrics result : searchResults) {
            if (result.getArtist() != null && artist.contains(result.getArtist())
                    && result.getTrack() != null && title.equalsIgnoreCase(result.getTrack()))
                return fromAPI(result.getURL(), artist, result.getTrack());
        }
        return new Lyrics(Lyrics.NO_RESULT);
    }

    // TODO handle urls
    public static Lyrics fromURL(String url, String artist, String title) {
        return fromAPI(url, artist, title);
    }

    public static Lyrics fromAPI(String url, String artist, String title) {
        Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        // fixme no public url
        try {
            String jsonText = Net.getUrlAsString(url);
            JsonObject lyricsJSON = new JsonParser().parse(jsonText).getAsJsonObject();
            lyrics.setText(lyricsJSON.get("body").getAsString().trim());
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            return new Lyrics(Lyrics.ERROR);
        }
        lyrics.setSource(domain);
        return lyrics;
    }
}
