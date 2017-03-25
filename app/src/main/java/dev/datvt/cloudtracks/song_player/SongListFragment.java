package dev.datvt.cloudtracks.song_player;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alelak.soundroid.models.Track;

import java.util.ArrayList;

import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.utils.ToolsHelper;

/**
 * Created by datvt on 7/26/2016.
 */
public class SongListFragment extends Fragment {

    public static SongListFragment.OnListFragmentInteractionListener mListener;
    public static SongListAdapter songListAdapter;
    public static ArrayList<Track> localTracks = new ArrayList<>();
    public static Context ctx;
    public static RecyclerView recyclerView;
    public static SwipeRefreshLayout ref;
    public static int mColumnCount = 1;

    public static void setUpList(ArrayList<Track> tracks) {
        if (ctx != null) {
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(ctx, mColumnCount));
            }

            if (tracks.size() > 0) {
                songListAdapter = new SongListAdapter(tracks, mListener, PlayMusicActivity.curPos);
                recyclerView.setAdapter(songListAdapter);
                songListAdapter.notifyDataSetChanged();
                if (PlayMusicActivity.curPos < tracks.size() - 3) {
                    recyclerView.smoothScrollToPosition(PlayMusicActivity.curPos + 3);
                } else if (PlayMusicActivity.curPos == tracks.size() - 3) {
                    recyclerView.smoothScrollToPosition(PlayMusicActivity.curPos + 2);
                } else if (PlayMusicActivity.curPos == tracks.size() - 2) {
                    recyclerView.smoothScrollToPosition(PlayMusicActivity.curPos + 1);
                } else {
                    recyclerView.smoothScrollToPosition(PlayMusicActivity.curPos);
                }
            } else {
                ToolsHelper.toast(ctx, ctx.getString(R.string.empty_playlist));
            }
            ref.setRefreshing(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PlayMusicActivity.tracks != null) {
            localTracks = PlayMusicActivity.tracks;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ctx = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.lvSong);
        ref = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        ref.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary));
        ref.setRefreshing(true);
        setUpList(localTracks);

        ref.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    setUpList(localTracks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code);

    }

}
