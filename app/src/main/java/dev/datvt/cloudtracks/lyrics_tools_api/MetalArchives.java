package dev.datvt.cloudtracks.lyrics_tools_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import dev.datvt.cloudtracks.song_player.Lyrics;
import dev.datvt.cloudtracks.utils.Net;


public class MetalArchives {

    public static final String domain = "metal-archives.com";

    public static Lyrics fromMetaData(String artist, String title) {
        String baseURL = "http://www.metal-archives.com/search/ajax-advanced/searching/songs/?bandName=%s&songTitle=%s&releaseType[]=1&exactSongMatch=1&exactBandMatch=1";
        String urlArtist = artist.replaceAll("\\s", "+");
        String urlTitle = title.replaceAll("\\s", "+");
        String url;
        String text;
        try {
            String response = Net.getUrlAsString(String.format(baseURL, urlArtist, urlTitle));
            JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();
            JsonArray track = jsonResponse.getAsJsonArray("aaData").get(0).getAsJsonArray();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < track.size(); i++)
                builder.append(track.get(i).getAsString());
            Document trackDocument = Jsoup.parse(builder.toString());
            url = trackDocument.getElementsByTag("a").get(1).attr("href");
            String id = trackDocument.getElementsByClass("viewLyrics").get(0).id().substring(11);
            text = Jsoup.connect("http://www.metal-archives.com/release/ajax-view-lyrics/id/" + id)
                    .get().body().html();
        } catch (JsonParseException | IndexOutOfBoundsException e) {
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (Exception e) {
            return new Lyrics(Lyrics.ERROR);
        }
        Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        lyrics.setText(text);
        lyrics.setSource(domain);
        lyrics.setURL(url);

        return lyrics;
    }

    public static Lyrics fromURL(String url, String artist, String title) {
        // TODO: support metal-archives URL
        return new Lyrics(Lyrics.NO_RESULT);
    }

}
