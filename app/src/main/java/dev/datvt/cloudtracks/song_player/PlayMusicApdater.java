package dev.datvt.cloudtracks.song_player;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by datvt on 7/26/2016.
 */
public class PlayMusicApdater extends FragmentStatePagerAdapter {

    final int mNumOfTabs = 3;

    public PlayMusicApdater(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new SongListFragment();
                break;
            case 1:
                fragment = new PlayFragment();
                break;
            case 2:
                fragment = new LyricsFragment();
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
