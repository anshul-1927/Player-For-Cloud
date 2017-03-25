package dev.datvt.cloudtracks.sound_cloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.alelak.soundroid.Soundroid;
import com.alelak.soundroid.models.Track;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.utils.ConstantHelper;
import dev.datvt.cloudtracks.utils.ExecuterU;
import dev.datvt.cloudtracks.utils.ToolsHelper;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by datvt on 7/31/2016.
 */
public class CloudSongFragment extends Fragment {

    private View viewFragment;
    private View viewProblemNetwork;
    private ImageView ivWifi;
    private Context ctx;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout ref;
    private SmoothProgressBar loading;
    private ArrayList<Track> tracksArrayList;
    private MyCloudTracksRecyclerViewAdapter adap;

    private int mColumnCount = 1;
    private String selectedGender = "Pop";
    private int selectedNo = 50;
    private boolean shown = false;

    private List<String> listNumTracks;
    private List<String> listGenres;
    private Spinner numberTracks;
    private Spinner genres;

    private CloudSongFragment.OnListFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewFragment = inflater.inflate(R.layout.fragment_cloudtracks_list, container, false);
        ctx = viewFragment.getContext();

        recyclerView = (RecyclerView) viewFragment.findViewById(R.id.list);
        ref = (SwipeRefreshLayout) viewFragment.findViewById(R.id.swipeRefreshLayout);
        ref.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary));
        numberTracks = (Spinner) viewFragment.findViewById(R.id.number);
        genres = (Spinner) viewFragment.findViewById(R.id.genres);


        loading = (SmoothProgressBar) viewFragment.findViewById(R.id.loading);
        loading.progressiveStop();
        loading.setVisibility(View.INVISIBLE);

        ivWifi = (ImageView) viewFragment.findViewById(R.id.ivWifi);
        viewProblemNetwork = viewFragment.findViewById(R.id.lnCheckNetwork);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(ctx, mColumnCount));
        }

        ExecuterU executerU = new ExecuterU(ctx, "") {

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx, R.layout.spinner_item, listGenres);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                genres.setAdapter(dataAdapter);


                ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(ctx, R.layout.spinner_item, listNumTracks);
                dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                numberTracks.setAdapter(dataAdapter2);

                adap = new MyCloudTracksRecyclerViewAdapter(tracksArrayList, mListener);
                recyclerView.setAdapter(adap);

                addEvents();
                getTop50();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                listGenres = new ArrayList<>();
                listGenres.add("All");
                listGenres.add("Alternative Rock");
                listGenres.add("Ambient");
                listGenres.add("Classical");
                listGenres.add("Country");
                listGenres.add("Dance");
                listGenres.add("Deep House");
                listGenres.add("Disco");
                listGenres.add("Drum & Bass");
                listGenres.add("Dubstep");
                listGenres.add("Electro");
                listGenres.add("Electronic");
                listGenres.add("Folk");
                listGenres.add("Hardcore Techno");
                listGenres.add("Hip hop");
                listGenres.add("House");
                listGenres.add("Indie Rock");
                listGenres.add("Jazz");
                listGenres.add("Latin");
                listGenres.add("Metal");
                listGenres.add("Minimal Techno");
                listGenres.add("Piano");
                listGenres.add("Pop");
                listGenres.add("Progressive House");
                listGenres.add("Punk");
                listGenres.add("R&B");
                listGenres.add("Rap");
                listGenres.add("Reggae");
                listGenres.add("Rock");
                listGenres.add("Soul");
                listGenres.add("Tech House");
                listGenres.add("Techno");
                listGenres.add("Trance");
                listGenres.add("Trap");
                listGenres.add("Trip Hop");
                listGenres.add("World");

                listNumTracks = new ArrayList<>();
                listNumTracks.add("Top 50");
                listNumTracks.add("Latest 50");

                tracksArrayList = new ArrayList<>();

                return super.doInBackground(voids);
            }
        };

        executerU.execute();


//        checkConn();

        return viewFragment;
    }

    @Override
    public void onDestroyView() {
        shown = false;
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void addEvents() {
        genres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                selectedGender = item;
                if (shown) {
//                    checkConn();
                    showList(selectedGender, selectedNo);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        numberTracks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (shown) {
                    if (item.contains(listNumTracks.get(0))) {
//                        checkConn();
                        getTop50();
                    } else {
//                        checkConn();
                        getLatest50();
                    }
                }
                selectedGender = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ref.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                checkConn();
                genres.setSelection(0);
                numberTracks.setSelection(0);
                getTop50();
            }
        });

        ivWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkConn();
                genres.setSelection(0);
                numberTracks.setSelection(0);
                getTop50();
            }
        });
    }

    private void getTop50() {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();

        String url = "https://api-v2.soundcloud.com/charts?kind=top&genre=" +
                "soundcloud%3Agenres%3Aall-music&limit=50" +
                "&client_id=" + ConstantHelper.CLIENT_ID + "&limit=30&offset=0";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("FAILED", "" + response);
                loading.progressiveStop();
                ref.setRefreshing(false);
                viewProblemNetwork.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                shown = true;

                if (tracksArrayList.size() > 0) {
                    tracksArrayList.clear();
                }

                Log.d("RESP", "" + response);
                Gson js = new Gson();
                try {
                    JSONObject par = new JSONObject(response);
                    JSONArray jar = par.getJSONArray("collection");
                    for (int i = 0; i < jar.length(); i++) {
                        JSONObject ch = jar.getJSONObject(i);
                        JSONObject tr = ch.getJSONObject("track");
                        Log.d("TRACK JSON", "" + tr.toString());
                        Track track = js.fromJson(tr.toString(), Track.class);
                        Log.d("GOTCHA", "" + track.title);
                        track.stream_url = "https://api.soundcloud.com/tracks/"
                                + track.id + "/stream?client_id="
                                + ConstantHelper.CLIENT_ID;
                        tracksArrayList.add(track);
                    }

                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.INVISIBLE);


                    if (tracksArrayList.size() <= 0) {
                        ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
                    }
                    adap.notifyDataSetChanged();

                } catch (Exception e) {
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        });


    }

    public void getLatest50() {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String url = "http://api.soundcloud.com/tracks?limit=50&" +
                "client_id=" + ConstantHelper.CLIENT_ID + "&created_at=" + (date);
        Log.d("LATEST URL", url);

        url = "http://api.soundcloud.com/tracks?limit=100&" +
                "client_id=" + ConstantHelper.CLIENT_ID + "&created_at=" + URLEncoder.encode(date);
        Log.d("LATEST URL", url);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("FAILeD", "" + response);
                loading.progressiveStop();
                ref.setRefreshing(false);
                viewProblemNetwork.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {

                if (tracksArrayList.size() > 0) {
                    tracksArrayList.clear();
                }

                Log.d("RESP", "" + response.substring(0, 10));
                Gson js = new Gson();
                try {
                    JSONArray jar = new JSONArray(response);
                    for (int i = 0; i < jar.length(); i++) {
                        JSONObject tr = jar.getJSONObject(i);
                        Log.d("TRACK JSON", "" + tr.toString());
                        Track te = js.fromJson(tr.toString(), Track.class);
                        Log.d("GOTCHA", "" + te.title);
                        te.stream_url = ("https://api.soundcloud.com/tracks/"
                                + te.id + "/stream?client_id="
                                + ConstantHelper.CLIENT_ID);
                        tracksArrayList.add(te);
                    }
                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.INVISIBLE);


                    if (tracksArrayList.size() > 0) {
//                        viewProblemNetwork.setVisibility(View.GONE);
                    } else {
//                        viewProblemNetwork.setVisibility(View.VISIBLE);
                        ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
                    }
                    adap.notifyDataSetChanged();

                } catch (Exception e) {
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkConn() {
        if (!ToolsHelper.hasConnection(getActivity())) {
            loading.setVisibility(View.INVISIBLE);
            loading.progressiveStop();
            ref.setRefreshing(false);
            viewProblemNetwork.setVisibility(View.VISIBLE);
        }
    }

    public void showList(String tag, int count) {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();

        Soundroid.init(getActivity().getApplicationContext(),
                ConstantHelper.CLIENT_ID);
        Call<List<Track>> call = Soundroid.getSoundcloudService().searchTracksByTags(tag, count);

        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Response<List<Track>> response) {
                if (response.isSuccess()) {
                    if (tracksArrayList.size() > 0) {
                        tracksArrayList.clear();
                    }
                    tracksArrayList.addAll(response.body());
                    Collections.shuffle(tracksArrayList);
                    Log.d("Found ", "No .of Tracks" + tracksArrayList.size());

                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.INVISIBLE);

                    if (tracksArrayList.size() > 0) {
//                        viewProblemNetwork.setVisibility(View.GONE);
                    } else {
//                        viewProblemNetwork.setVisibility(View.VISIBLE);
                        ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
                    }
                    adap.notifyDataSetChanged();
                } else {
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                loading.progressiveStop();
                ref.setRefreshing(false);
                viewProblemNetwork.setVisibility(View.VISIBLE);
            }
        });
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code);
    }
}