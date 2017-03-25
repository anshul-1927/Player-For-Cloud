package dev.datvt.cloudtracks.song_player;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SearchResult extends Activity {
    LinearLayout NoNetwork;
    LinearLayout Processing;
    ArrayList<String> artist;
    int color;
    View footer;
    ArrayList<String> link;
    ListView list;
    String next;
    String nextLink;
    ProgressBar pb;
    ProgressBar progress;
    ArrayList<String> song;
    TextView t1;
    TextView t2;
    String nameSong = "";
    String url = "http://www.lyricsfreak.com/search.php?a=search&type=song&q=" + nameSong.trim().replace(" ", "+");

    public SearchResult() {
        this.nextLink = "";
        this.next = "";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url = getIntent().getStringExtra("link");
        this.song = new ArrayList();
        this.artist = new ArrayList();
        this.link = new ArrayList();
        new Data().execute(new Void[0]);
        this.list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                if (SearchResult.this.getIntent().getStringExtra("type").equals("song")) {
                    intent.putExtra("link", (String) SearchResult.this.link.get(position));
                    intent.putExtra("title", new StringBuilder(String.valueOf((String) SearchResult.this.artist.get(position))).append("-").append((String) SearchResult.this.song.get(position)).toString());
                    intent.setClass(SearchResult.this, Lyrics.class);
                } else if (SearchResult.this.getIntent().getStringExtra("type").equals("band")) {
                    intent.putExtra("link", (String) SearchResult.this.link.get(position));
                    Log.e("Artist_link", (String) SearchResult.this.link.get(position));
                    intent.putExtra("type", "band");
                } else {
                    intent.putExtra("link", (String) SearchResult.this.link.get(position));
                    intent.putExtra("type", "album");
                    intent.putExtra("title", (String) SearchResult.this.artist.get(position));
                    intent.setClass(SearchResult.this, Lyrics.class);
                }
                SearchResult.this.startActivity(intent);
            }
        });
    }

    class Data extends AsyncTask<Void, Void, Void> {
        Document doc;

        Data() {
        }

        protected Void doInBackground(Void... params) {
            try {
                this.doc = Jsoup.connect(SearchResult.this.url).referrer(SearchResult.this.url).userAgent("opera").timeout(3000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (this.doc != null) {
                SearchResult.this.nextLink = null;
                Iterator it = this.doc.select("div[style=margin-left:20px]").select("table[cellspacing=0]").select("tbody").select("tr").iterator();
                while (it.hasNext()) {
                    Element e = (Element) it.next();
                    if (SearchResult.this.nextLink == null) {
                        if (SearchResult.this.getIntent().getStringExtra("type").equals("song") || SearchResult.this.getIntent().getStringExtra("type").equals("album")) {
                            SearchResult.this.song.add(e.select("td").select("a[class=song]").text());
                            SearchResult.this.link.add("http://www.lyricsfreak.com" + e.select("td").select("a[class=song]").attr("href"));
                            SearchResult.this.artist.add(e.select("td").first().select("a").text().substring(6));
                        } else if (SearchResult.this.getIntent().getStringExtra("type").equals("band")) {
                            SearchResult.this.link.add("http://www.lyricsfreak.com" + e.select("td").first().select("a").attr("href"));
                            SearchResult.this.artist.add(e.select("td").first().select("a").text().substring(6));
                            Log.e("song+artist", e.select("td").first().select("a").text().substring(6));
                        }
                    }
                }
                if (this.doc.select("table").select("tr").select("td").hasClass("paging")) {
                    SearchResult.this.nextLink = this.doc.select("table").select("tr").select("td[class=paging]").select("a").last().attr("href");
                    SearchResult.this.next = this.doc.select("table").select("tr").select("td[class=paging]").select("a").last().text();
                    Log.e("next_link", SearchResult.this.next);
                }
                if (SearchResult.this.nextLink != null && SearchResult.this.next.contains("Next") && SearchResult.this.list.getFooterViewsCount() == 0) {
                    SearchResult.this.list.addFooterView(SearchResult.this.footer);
                }

                SearchResult.this.footer.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SearchResult.this.url = "http://www.lyricsfreak.com/search.php" + SearchResult.this.nextLink;
                        new Data().execute(new Void[0]);
                    }
                });
                if (SearchResult.this.artist.size() == 0) {
                }
            } else {
            }
            super.onPostExecute(result);
        }
    }
}
