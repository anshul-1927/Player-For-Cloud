package dev.datvt.cloudtracks.sound_cloud;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by datvt on 7/31/2016.
 */
public class MyViewPagerAdapter extends FragmentStatePagerAdapter {

    final int TAB_COUNT = 3;
    private Fragment fr;

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fr = new CloudSongFragment();
                break;
            case 1:
                fr = new SearchTracksFragment();
                break;
            case 2:
                fr = new LocalTracksFragment();
                break;
            default:
                break;
        }
        return fr;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }
}
