package dev.datvt.cloudtracks.sound_cloud;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alelak.soundroid.models.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import dev.datvt.cloudtracks.MainActivity;
import dev.datvt.cloudtracks.MyApplication;
import dev.datvt.cloudtracks.R;
import dev.datvt.cloudtracks.utils.ExecuterU;
import dev.datvt.cloudtracks.utils.ToolsHelper;

public class LocalTracksFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    public static LocalTracksFragment.OnListFragmentInteractionListener mListener;
    public View viewFragment;
    private EditText search_et;
    private ArrayList<Track> localTracks, baseLocal, trackArrayList;
    private Context ctx;
    private ImageView back;
    private View lnBack;
    private LinearLayout mm;
    private ArrayList<Menu> menus;
    private int PLAYLIST = 0, LOCAL = 1, RECENT = 2, DOWNLOAD = 3;
    private String ret = null;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout ref;
    private MyLocalTracksRecyclerViewAdapter adap;
    private onPlayListSelected onSelected;
    private int mColumnCount = 1;

    public LocalTracksFragment() {
    }

    public static LocalTracksFragment newInstance(int columnCount) {
        LocalTracksFragment fragment = new LocalTracksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewFragment = inflater.inflate(R.layout.fragment_localtracks_list, container, false);

        ctx = viewFragment.getContext();

        recyclerView = (RecyclerView) viewFragment.findViewById(R.id.listM);
        search_et = (EditText) viewFragment.findViewById(R.id.search_et);
        back = (ImageView) viewFragment.findViewById(R.id.backL);
        lnBack = viewFragment.findViewById(R.id.lnBack);
        lnBack.setVisibility(View.GONE);
        mm = (LinearLayout) viewFragment.findViewById(R.id.mm);
        ref = (SwipeRefreshLayout) viewFragment.findViewById(R.id.swipeRefreshLayout);
        ref.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary));
        ref.setEnabled(false);

        localTracks = new ArrayList<>();
        baseLocal = new ArrayList<>();
        trackArrayList = new ArrayList<>();

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(ctx, mColumnCount));
        }

        adap = new MyLocalTracksRecyclerViewAdapter(trackArrayList, mListener);
        recyclerView.setAdapter(adap);

        showMenus();
        addEvent();

        return viewFragment;
    }

    private void addEvent() {
        search_et.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ToolsHelper.hideKeyBoard(getActivity());
                    String query = search_et.getText().toString();
                    if (query != null) {
                        if (query.length() > 1 && baseLocal.size() > 0) {
                            ArrayList<Track> tmp = new ArrayList<>();
                            for (int z = 0; z < baseLocal.size(); z++) {
                                if (baseLocal.get(z).title.toLowerCase().contains(query.toLowerCase())) {
                                    tmp.add(baseLocal.get(z));
                                }
                            }

                            if (tmp.size() > 0) {
                                localTracks = tmp;
                                setUpList(localTracks);
                            } else {
                                ToolsHelper.toast(ctx, getString(R.string.nothing_found));
                            }
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final StringBuilder sb = new StringBuilder(charSequence.length());
                sb.append(charSequence);
                String query = sb.toString();

                if (query != null && baseLocal != null) {
                    if (query.length() > 0 && baseLocal.size() > 0) {
                        ArrayList<Track> tmp = new ArrayList<Track>();

                        for (int z = 0; z < baseLocal.size(); z++) {
                            if (baseLocal.get(z).title.toLowerCase().contains(query.toLowerCase())) {
                                tmp.add(baseLocal.get(z));
                            }
                        }

                        if (tmp.size() > 0) {
                            localTracks = tmp;
                            setUpList(localTracks);
                        }
//                        else {
//                            ToolsHelper.toast(ctx, getString(R.string.nothing_found));
//                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        ref.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getAllSongs();
                    setUpList(localTracks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.setVisibility(View.GONE);
                lnBack.setVisibility(View.GONE);
                mm.setVisibility(View.VISIBLE);
                showMenus();
            }
        });
    }

    public void showMenus() {
        menus = new ArrayList<>();
        mm.removeAllViews();

        LayoutInflater lf = LayoutInflater.from(ctx);

        for (int i = 0; i < 4; i++) {

            View con;
            con = lf.inflate(R.layout.fragment_local_main, null);

            ImageView art = (ImageView) con.findViewById(R.id.artM);
            ImageView shuf = (ImageView) con.findViewById(R.id.shuffleM);
            ImageView del = (ImageView) con.findViewById(R.id.delM);
            TextView mn = (TextView) con.findViewById(R.id.nameM);
            final TextView numberSong = (TextView) con.findViewById(R.id.noM);
            numberSong.setVisibility(View.VISIBLE);
            Menu m;

            m = new Menu(con, art, mn, numberSong);

            if (i == PLAYLIST) {
                art.setImageResource(R.drawable.custom_playlist);
                mn.setText(getString(R.string.playlist));

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            setUpListPlaylist();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lnBack.setVisibility(View.VISIBLE);
                    }
                });

                if (ToolsHelper.getListPlaylist() != null) {
                    numberSong.setText(ToolsHelper.getListPlaylist().size() + " " + getString(R.string.playlist));
                }
            } else if (i == RECENT) {
                art.setImageResource(R.drawable.custom_recent);
                mn.setText(getString(R.string.recent));
                shuf.setVisibility(View.VISIBLE);
                m.shuffle = shuf;

                m.shuffle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {
                            @Override
                            public void doIt() {
                                ArrayList<Track> tracks = ToolsHelper.getPlayList(ctx, "recent.lst");
                                localTracks = tracks;
                            }

                            @Override
                            public void doNe() {
                                ArrayList<Track> tracks = localTracks;
                                Collections.shuffle(tracks);
                                if (tracks.size() > 0) {
                                    mListener.onListFragmentInteraction(tracks, tracks.get(0), 0, ToolsHelper.IS_LOCAL);
                                } else {
                                    ToolsHelper.toast(ctx, getString(R.string.empty_playlist));
                                }
                            }
                        };
                        ex.execute();
                    }
                });

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mm.setVisibility(View.GONE);
                        lnBack.setVisibility(View.VISIBLE);
                        ref.setVisibility(View.VISIBLE);
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {

                            @Override
                            public void doIt() {
                                baseLocal = ToolsHelper.getPlayList(ctx, "recent.lst");
                            }

                            @Override
                            public void doNe() {
                                setUpList(baseLocal);
                            }
                        };
                        ex.execute();
                    }
                });

                if (ToolsHelper.getPlayList(ctx, "recent.lst") != null) {
                    numberSong.setText(ToolsHelper.getPlayList(ctx, "recent.lst").size() + " " + getString(R.string.songs));
                }

            } else if (i == DOWNLOAD) {
                art.setImageResource(R.drawable.custom_download);
                mn.setText(getString(R.string.download));
                shuf.setVisibility(View.VISIBLE);
                m.shuffle = shuf;

                m.shuffle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {
                            @Override
                            public void doIt() {
                                ArrayList<Track> tracks = ToolsHelper.getPlayList(ctx, "download.lst");
                                localTracks = tracks;
                            }

                            @Override
                            public void doNe() {
                                ArrayList<Track> tracks = localTracks;
                                Collections.shuffle(tracks);
                                if (tracks.size() > 0) {
                                    mListener.onListFragmentInteraction(tracks, tracks.get(0), 0, ToolsHelper.IS_LOCAL);
                                } else {
                                    ToolsHelper.toast(ctx, getString(R.string.empty_playlist));
                                }
                            }
                        };
                        ex.execute();
                    }
                });

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mm.setVisibility(View.GONE);
                        lnBack.setVisibility(View.VISIBLE);
                        ref.setVisibility(View.VISIBLE);
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {

                            @Override
                            public void doIt() {
                                baseLocal = ToolsHelper.getPlayList(ctx, "download.lst");
                            }

                            @Override
                            public void doNe() {
                                setUpList(baseLocal);
                            }
                        };
                        ex.execute();
                    }
                });

                if (ToolsHelper.getPlayList(ctx, "download.lst") != null) {
                    numberSong.setText(ToolsHelper.getPlayList(ctx, "download.lst").size() + " " + getString(R.string.songs));
                }

            } else if (i == LOCAL) {
                art.setImageResource(R.drawable.custom_local);
                mn.setText(getString(R.string.local));
                shuf.setVisibility(View.VISIBLE);
                m.shuffle = shuf;

                m.shuffle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {

                            @Override
                            public void doIt() {
                                if (localTracks.size() > 0) {
                                    localTracks.clear();
                                }
                                getAllSongs();
                            }

                            @Override
                            public void doNe() {
                                ArrayList<Track> tracks = localTracks;
                                Collections.shuffle(tracks);
                                if (tracks.size() > 0) {
                                    mListener.onListFragmentInteraction(tracks, tracks.get(0), 0, ToolsHelper.IS_LOCAL);
                                } else {
                                    ToolsHelper.toast(ctx, getString(R.string.empty_playlist));
                                }
                            }

                        };
                        ex.execute();
                    }
                });

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mm.setVisibility(View.GONE);
                        lnBack.setVisibility(View.VISIBLE);
                        ref.setVisibility(View.VISIBLE);

                        try {
                            ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {
                                @Override
                                public void doIt() {
                                    getAllSongs();
                                }

                                @Override
                                public void doNe() {
                                    setUpList(localTracks);
                                }
                            };
                            ex.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                numberSong.setText(getCountSong() + " " + getString(R.string.songs));
            }

            if (MyApplication.CAN_DOWNLOAD) {
                menus.add(m);
                mm.addView(con);
            } else {
                if (i != DOWNLOAD) {
                    menus.add(m);
                    mm.addView(con);
                }
            }
        }
    }

    public void setUpListPlaylist() {
        menus = new ArrayList<>();
        mm.removeAllViews();
        LayoutInflater lf = LayoutInflater.from(ctx);

        if (true) {
            LinearLayout con;
            con = (LinearLayout) lf.inflate(R.layout.fragment_local_main, null);
            ImageView art = (ImageView) con.findViewById(R.id.artM);
            ImageView shuf = (ImageView) con.findViewById(R.id.shuffleM);
            ImageView del = (ImageView) con.findViewById(R.id.delM);
            TextView mn = (TextView) con.findViewById(R.id.nameM);
            TextView sn = (TextView) con.findViewById(R.id.noM);

            con.setGravity(Gravity.CENTER);
            art.setImageResource(R.drawable.icon_add_press);
            mn.setText(getString(R.string.create_new));

            art.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showChangeLangDialog();
                }
            });
            mm.addView(con);
        }


        final ArrayList<String> names = ToolsHelper.getListPlaylist();

        if (names.size() > 0) {
            for (int i = 0; i < ToolsHelper.getListPlaylist().size(); i++) {
                View con;

                con = lf.inflate(R.layout.fragment_local_main, null);
                ImageView art = (ImageView) con.findViewById(R.id.artM);
                ImageView shuf = (ImageView) con.findViewById(R.id.shuffleM);
                ImageView del = (ImageView) con.findViewById(R.id.delM);
                TextView mn = (TextView) con.findViewById(R.id.nameM);
                TextView sn = (TextView) con.findViewById(R.id.noM);

                Menu m;

                art.setImageResource(R.drawable.default_nhaccuatui);
                shuf.setImageResource(R.drawable.ic_shuffle);
                shuf.setVisibility(View.VISIBLE);
                del.setImageResource(R.drawable.ic_delete);
                del.setVisibility(View.VISIBLE);
                mn.setText(names.get(i).replace(".lst", ""));
                sn.setText("" + ToolsHelper.getPlayList(ctx, names.get(i)).size() + " " + getString(R.string.songs));
                sn.setVisibility(View.VISIBLE);

                final String playlistname = names.get(i);

                con.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mm.setVisibility(View.GONE);
                        lnBack.setVisibility(View.VISIBLE);
                        ref.setVisibility(View.VISIBLE);
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {

                            @Override
                            public void doIt() {
                                baseLocal = ToolsHelper.getPlayList(ctx, playlistname);
                            }

                            @Override
                            public void doNe() {
                                setUpList(baseLocal);
                            }
                        };
                        ex.execute();
                    }
                });

                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File f = new File(MainActivity.folder + "/Playlist/" + playlistname);
                        f.delete();
                        setUpListPlaylist();
                    }
                });

                shuf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ExecuterU ex = new ExecuterU(ctx, getString(R.string.scanning)) {
                            @Override
                            public void doIt() {
                                ArrayList<Track> tracks = ToolsHelper.getPlayList(ctx, playlistname);
                                localTracks = tracks;
                            }

                            @Override
                            public void doNe() {
                                ArrayList<Track> tracks = localTracks;
                                Collections.shuffle(tracks);
                                if (tracks.size() > 0) {
                                    if (tracks.get(0).bpm == ToolsHelper.IS_LOCAL)
                                        mListener.onListFragmentInteraction(tracks, tracks.get(0), 0, ToolsHelper.IS_LOCAL);
                                    else {
                                        mListener.onListFragmentInteraction(tracks, tracks.get(0), 0, ToolsHelper.DOWNLOAD_CODE);
                                    }
                                } else {
                                    ToolsHelper.toast(ctx, getString(R.string.empty_playlist));
                                }
                            }
                        };
                        ex.execute();
                    }
                });

                m = new Menu(con, art, mn, sn);
                menus.add(m);
                mm.addView(con);
            }
        }
    }

    public void showChangeLangDialog() {
        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_create_playlist);

        final EditText edt = (EditText) dialog.findViewById(R.id.edtInput);
        final TextView btnCreate = (TextView) dialog.findViewById(R.id.btnCreate);
        final TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);
        final ImageView btnDel = (ImageView) dialog.findViewById(R.id.btnDel);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ret = edt.getText().toString();
                if (ret != null && !ret.isEmpty()) {
                    try {
                        ToolsHelper.createPlaylist(ctx, ret);
                        setUpListPlaylist();
                        Log.d("CREATE_2", "COMPLETE");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ToolsHelper.toast(ctx, getString(R.string.info_not_name_playlist));
                    showChangeLangDialog();
                }
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ret = null;
                dialog.cancel();
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt.setText("");
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private int getCountSong() {
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getActivity().getContentResolver().query(allsongsuri, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int count_song = 0;
        if (cursor != null) {
            count_song = cursor.getCount();
        }
        Log.d("SONG_NUMBER", count_song + "");
        return count_song;
    }

    public void getAllSongs() {
        localTracks = new ArrayList<>();
        baseLocal = new ArrayList<>();
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getActivity().getContentResolver().query(allsongsuri, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("SONG_NUMBER", cursor.getCount() + "");

            do {
                String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String song_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String gender = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                Log.d("SONG_", song_title + " - " + song_name);

                Track ct = new Track();
                ct.title = song_title;
                ct.stream_url = fullpath;
                ct.download_url = fullpath;
                ct.artwork_url = getAlbumArt(song_id).toString();
                ct.id = song_id;
                ct.bpm = ToolsHelper.IS_LOCAL;
                ct.genre = gender;
                ct.license = artist;
                ct.duration = duration;
                localTracks.add(ct);
                baseLocal.add(ct);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public Uri getAlbumArt(int songId) {
        Uri albumArtUri = null;
        try {
            String selection = MediaStore.Audio.Media._ID + " = " + songId + "";
            Cursor cursor = ctx.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                    selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            }
            cursor.close();
        } catch (Exception e) {
        }
        return albumArtUri;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            onSelected = (onPlayListSelected) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setUpList(ArrayList<Track> tracks) {
        ref.setRefreshing(false);
        Context context = ctx;
        if (trackArrayList.size() > 0) {
            trackArrayList.clear();
        }
        trackArrayList.addAll(tracks);
        adap.notifyDataSetChanged();

        if (trackArrayList.size() > 0) {

            Log.d("COUNT", "" + adap.getItemCount());
        } else {
            ToolsHelper.toast(getContext(), getString(R.string.no_tracks_found));
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ArrayList<Track> items, Track item, int pos, int code);

    }

    public interface onPlayListSelected {
        public void clickedPlayList(ArrayList<Track> items, Track item, int pos, int code);
    }

    public class Menu {
        public View parent;
        public ImageView thumb, shuffle, delete;
        public TextView name, subname;

        public Menu(View par, View im, View nm, View number) {
            parent = par;
            thumb = (ImageView) im;
            name = (TextView) nm;
            number = (TextView) subname;
        }
    }

}
