package com.example.joanericacanada.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by joanericacanada on 10/7/15.
 */
public class CrimePagerActivity extends FragmentActivity {
    private ViewPager viewPager;
    private ArrayList<Crime> crimes;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        viewPager = new ViewPager(this);
        viewPager.setId(R.id.viewPager);
        setContentView(viewPager);

        crimes = CrimeLab.get(this).getCrimes();

        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount(){
                return crimes.size();
            }

            @Override
            public Fragment getItem(int pos){
                Crime crime = crimes.get(pos);
                return CrimeFragment.newInstance(crime.getId());
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            public void onPageScrollStateChanged(int state){

            }

            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels){

            }

            public void onPageSelected(int pos){
                Crime crime = crimes.get(pos);
                if(crime.getTitle() != null){
                    setTitle(crime.getTitle());
                }
            }
        });

        UUID crimeId = (UUID)getIntent().getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
        for(int i = 0; i <crimes.size(); i++){
            if(crimes.get(i).getId().equals(crimeId)){
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
