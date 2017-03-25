package dev.datvt.cloudtracks.sound_cloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

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
import dev.datvt.cloudtracks.utils.ToolsHelper;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchTracksFragment extends Fragment implements TextWatcher {

    private ArrayList<Track> trackEntries;
    private View viewProblemNetwork;
    private ImageView ivWifi;
    private RecyclerView recyclerView;
    private boolean shown = false;
    private SmoothProgressBar loading;
    private SwipeRefreshLayout ref;
    private View viewFragment;
    private Context ctx;
    private int mColumnCount = 1;
    private String query = null;
    private AutoCompleteTextView search_et;
    private MySearchTracksRecyclerViewAdapter adap;
    private ArrayList<String> autoList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private OnListFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewFragment = inflater.inflate(R.layout.fragment_searchtracks_list, container, false);
        ctx = viewFragment.getContext();

        recyclerView = (RecyclerView) viewFragment.findViewById(R.id.list);
        ref = (SwipeRefreshLayout) viewFragment.findViewById(R.id.swipeRefreshLayout);
        ref.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary));
        search_et = (AutoCompleteTextView) viewFragment.findViewById(R.id.search_et);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, autoList);
        search_et.setAdapter(arrayAdapter);
        search_et.addTextChangedListener(this);

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

        trackEntries = new ArrayList<>();
        adap = new MySearchTracksRecyclerViewAdapter(trackEntries, mListener);
        recyclerView.setAdapter(adap);

//        checkConn();
        addEvents();

        return viewFragment;
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
        search_et.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ToolsHelper.hideKeyBoard(getActivity());
                    try {
                        query = URLEncoder.encode("" + search_et.getText().toString());

                        if (query != null && !search_et.getText().toString().equals("")) {

                            if (!autoList.contains(search_et.getText().toString())) {
                                autoList.add(search_et.getText().toString());
                            }

                            arrayAdapter.notifyDataSetChanged();

                            getLatest50(query);
                        } else {
                            if (trackEntries.size() > 0) {
                                trackEntries.clear();
                                adap.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        if (query != null && !search_et.getText().toString().equals("")) {
            if (query.length() > 2) {
                getLatest50(query);
            }
        } else {
            if (trackEntries.size() > 0) {
                trackEntries.clear();
                adap.notifyDataSetChanged();
            }
        }

        ref.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (query != null && !search_et.getText().toString().equals("")) {
                    getLatest50(query);
                } else {
                    if (trackEntries.size() > 0) {
                        trackEntries.clear();
                        adap.notifyDataSetChanged();
                    }
                    ref.setRefreshing(false);
                }
            }
        });

        ivWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (query != null && !search_et.getText().toString().equals("")) {
                    getLatest50(query);
                } else {
                    if (trackEntries.size() > 0) {
                        trackEntries.clear();
                        adap.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public void getLatest50(String query) {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String url = "https://api.soundcloud.com/tracks?q=" + query + "&limit=100" +
                "&client_id=" + ConstantHelper.CLIENT_ID;
        Log.d("LATEST URL", url);

        final AsyncHttpClient client = new AsyncHttpClient();
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
//                ref.setRefreshing(false);
                if (trackEntries.size() > 0) {
                    trackEntries.clear();
                }
//                Log.d("RESP", "" + response.substring(0, 10));
                Gson js = new Gson();
                try {
                    JSONArray jar = new JSONArray(response);
                    for (int i = 0; i < jar.length(); i++) {
                        JSONObject tr = jar.getJSONObject(i);
                        Log.d("TRACK JSON", "" + tr.toString());
                        Track te = js.fromJson(tr.toString(), Track.class);
                        Log.d("GOTCHA", "" + te.title);
                        te.stream_url = ("https://api.soundcloud.com/tracks/" + te.id + "/stream?" +
                                "client_id=" + ConstantHelper.CLIENT_ID);
                        trackEntries.add(te);
                    }

                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();
                    ref.setRefreshing(false);
                    viewProblemNetwork.setVisibility(View.INVISIBLE);


                    if (trackEntries.size() > 1) {
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

    public void checkConn() {
        if (!ToolsHelper.hasConnection(getActivity())) {
            loading.setVisibility(View.INVISIBLE);
            loading.progressiveStop();
            ref.setRefreshing(false);
            viewProblemNetwork.setVisibility(View.VISIBLE);
            if (trackEntries.size() > 0) {
                trackEntries.clear();
            }
            adap.notifyDataSetChanged();
//            viewProblemNetwork.setVisibility(View.VISIBLE);
//            ToolsHelper.toast(ctx, "Not Connected to internet");
        }
    }

    public void getTop50() {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();
        ToolsHelper.log("STARTED");
        shown = true;

        String url = "https://api-v2.soundcloud.com/charts?kind=top&genre=soundcloud%3Agenres%3Aall-music" +
                "&client_id=" + ConstantHelper.CLIENT_ID + "&limit=30&offset=0";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("FAILeD", "" + response);
                loading.progressiveStop();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                ref.setRefreshing(false);
                if (trackEntries.size() > 0) {
                    trackEntries.clear();
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
                        Track te = js.fromJson(tr.toString(), Track.class);
                        Log.d("GOTCHA", "" + te.title);
                        te.stream_url = ("https://api.soundcloud.com/tracks/" + te.id + "/stream?client_id=" +
                                ConstantHelper.CLIENT_ID);
                        Log.d("GOTCHA STREAM Top", "https://api.soundcloud.com/tracks/" + te.id + "/stream?client_id=" +
                                ConstantHelper.CLIENT_ID);
                        trackEntries.add(te);
                    }

                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();

                    if (trackEntries.size() > 1) {
//                        viewProblemNetwork.setVisibility(View.GONE);
                    } else {
//                        viewProblemNetwork.setVisibility(View.VISIBLE);
                        ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
                    }
                    adap.notifyDataSetChanged();
                } catch (Exception e) {
                    loading.progressiveStop();
                    e.printStackTrace();
                }
            }
        });


    }

    public void showList(String tag, int count) {
        loading.setVisibility(View.VISIBLE);
        loading.progressiveStart();

        checkConn();

        Soundroid.init(getActivity().getApplicationContext(), ConstantHelper.CLIENT_ID);
        Call<List<Track>> call = Soundroid.getSoundcloudService().searchTracksByTags(tag, count);

        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Response<List<Track>> response) {
                if (response.isSuccess()) {
                    if (trackEntries.size() > 0) {
                        trackEntries.clear();
                    }

                    trackEntries.addAll(response.body());
                    Collections.shuffle(trackEntries);
                    Log.d("Found ", "No .of Tracks" + trackEntries.size());

                    loading.setVisibility(View.INVISIBLE);
                    loading.progressiveStop();

                    if (trackEntries.size() > 1) {
//                        viewProblemNetwork.setVisibility(View.GONE);
                    } else {
//                        viewProblemNetwork.setVisibility(View.VISIBLE);
                        ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
                    }
                    adap.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                loading.progressiveStop();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code);
    }
}
