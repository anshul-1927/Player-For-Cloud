package dev.datvt.cloudtracks.lyrics_tools_api;


import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;

import dev.datvt.cloudtracks.song_player.Lyrics;
import dev.datvt.cloudtracks.utils.Net;


public class LyricsMania {

    public static final String domain = "www.lyricsmania.com";
    private static final String baseURL = "http://www.lyricsmania.com/%s_lyrics_%s.html";

    public static Lyrics fromMetaData(String artist, String song) {
        String htmlArtist = Normalizer.normalize(artist.replaceAll("[\\s-]", "_"), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^A-Za-z0-9_]", "");
        String htmlSong = Normalizer.normalize(song.replaceAll("[\\s-]", "_"), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^A-Za-z0-9_]", "");

        if (artist.startsWith("The "))
            htmlArtist = htmlArtist.substring(4) + "_the";

        String urlString = String.format(
                baseURL,
                htmlSong.toLowerCase(Locale.getDefault()),
                htmlArtist.toLowerCase(Locale.getDefault()));
        return fromURL(urlString, artist, song);
    }

    public static Lyrics fromURL(String url, String artist, String title) {
        String text;
        try {
            Document document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get();
            Element lyricsBody = document.getElementsByClass("lyrics-body").get(0);
            // lyricsBody.select("div").last().remove();
            text = Jsoup.clean(lyricsBody.html(), "", Whitelist.basic().addTags("div"));
            text = text.substring(text.indexOf("</strong>") + 10, text.lastIndexOf("</div>"));

            String[] keywords =
                    document.getElementsByTag("meta").attr("name", "keywords").get(0).attr("content").split(",");

            if (artist == null)
                artist = document.getElementsByClass("lyrics-nav-menu").get(0)
                        .getElementsByTag("a").get(0).text();
            if (title == null)
                title = keywords[0];
        } catch (HttpStatusException | IndexOutOfBoundsException e) {
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (IOException e) {
            return new Lyrics(Lyrics.ERROR);
        }
        if (text.startsWith("Instrumental"))
            return new Lyrics(Lyrics.NEGATIVE_RESULT);
        Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        lyrics.setURL(url);
        lyrics.setSource(domain);
        lyrics.setText(text.trim());
        return lyrics;
    }
}
