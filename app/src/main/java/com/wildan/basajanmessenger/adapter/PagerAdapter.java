package com.wildan.basajanmessenger.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wildan.basajanmessenger.fragment.Frag1_Teman;
import com.wildan.basajanmessenger.fragment.Frag2_Pesan;
import com.wildan.basajanmessenger.fragment.Frag3_Profile;

public class PagerAdapter extends FragmentStatePagerAdapter{

    private int number_tabs;

    public PagerAdapter(FragmentManager fm, int number_tabs) {
        super(fm);
        this.number_tabs = number_tabs;
    }

    //Mengembalikan Fragment yang terkait dengan posisi tertentu
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Frag1_Teman();
            case 1:
                return new Frag2_Pesan();
            case 2:
                return new Frag3_Profile();
            default:
                return null;
        }
    }

    //Mengembalikan jumlah tampilan yang tersedia.
    @Override
    public int getCount() {
        return number_tabs;
    }
}